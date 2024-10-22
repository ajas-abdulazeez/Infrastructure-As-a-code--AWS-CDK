package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.sns.*;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.amazon.awscdk.services.budgets.CfnBudget;
import software.amazon.awscdk.services.events.*;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.ssm.StringParameter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MultistackStack extends Stack {

    public MultistackStack(final Construct scope, final String id, final StackProps props, boolean isProd) {
        super(scope, id, props);

        // Create a VPC with 1 public and 1 private subnet
        Vpc vpc = Vpc.Builder.create(this, "MyVpc")
                .maxAzs(2)
                .subnetConfiguration(Arrays.asList(
                        SubnetConfiguration.builder()
                                .name("PublicSubnet")
                                .subnetType(SubnetType.PUBLIC)
                                .build(),
                        SubnetConfiguration.builder()
                                .name("PrivateSubnet")
                                .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                                .build()
                ))
                .build();

        // Allocate Elastic IP for EC2
        CfnEIP elasticIp = CfnEIP.Builder.create(this, "MyElasticIP").build();

        // Create a security group for EC2 instance
        SecurityGroup ec2SecurityGroup = SecurityGroup.Builder.create(this, "EC2SecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();
        ec2SecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(22), "Allow SSH Access");

        // Create an EC2 instance with specific configuration for prod or dev
        InstanceType ec2InstanceType;
        if (isProd) {
            ec2InstanceType = InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO);  // Use 'T3' instance for production
        } else {
            ec2InstanceType = InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO);  // Use 'T2' instance for development
        }

        // Creating EC2 instance in public subnet
        Instance ec2Instance = Instance.Builder.create(this, "MyEc2Instance")
                .instanceType(ec2InstanceType)
                .machineImage(MachineImage.latestAmazonLinux())
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PUBLIC)
                        .build())
                .securityGroup(ec2SecurityGroup) // Attach security group to EC2 instance
                .build();

        // Associate the Elastic IP with the EC2 instance
        CfnEIPAssociation.Builder.create(this, "EIPAssociation")
                .eip(elasticIp.getRef())
                .instanceId(ec2Instance.getInstanceId())
                .build();

        // Create a Lambda function for API in public subnet
        Role lambdaRole = Role.Builder.create(this, "LambdaExecutionRole")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .managedPolicies(Arrays.asList(
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole")
                ))
                .build();

        Function helloWorldLambda = Function.Builder.create(this, "HelloWorldLambda")
                .runtime(Runtime.NODEJS_20_X)
                .handler("hello.handler")
                .code(Code.fromAsset("src/main/resources/lambda"))
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PUBLIC)
                        .build())
                .role(lambdaRole)
                .securityGroups(Arrays.asList(ec2SecurityGroup)) // Attach security group to Lambda
                .build();

        // Create security group for RDS instance
        SecurityGroup rdsSecurityGroup = SecurityGroup.Builder.create(this, "RDSSecurityGroup")
                .vpc(vpc)
                .build();
        rdsSecurityGroup.addIngressRule(ec2SecurityGroup, Port.tcp(5432), "Allow EC2 to access RDS");

        // Create PostgreSQL RDS instance in private subnet with specific configurations for prod or dev
        DatabaseInstance rdsInstance = DatabaseInstance.Builder.create(this, "MyRdsInstance")
                .engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_13_4)
                        .build()))
                .instanceType(isProd ? InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO) : InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO)) // Use different instance types for prod and dev
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .securityGroups((List<? extends ISecurityGroup>) rdsSecurityGroup) // Attach security group to RDS instance
                .credentials(Credentials.fromGeneratedSecret("admin"))
                .multiAz(false)
                .allocatedStorage(20)
                .maxAllocatedStorage(100)
                .build();

        // Store RDS endpoint in SSM Parameter
        StringParameter rdsEndpoint = StringParameter.Builder.create(this, "RdsEndpoint")
                .parameterName("/rds/endpoint")
                .stringValue(rdsInstance.getDbInstanceEndpointAddress())
                .build();

        // API Gateway connected to Lambda function
        LambdaRestApi api = LambdaRestApi.Builder.create(this, "HelloWorldApi")
                .handler(helloWorldLambda)
                .build();

        // Budget SNS Topic for Billing Alerts
        Topic billingTopic = Topic.Builder.create(this, "BillingAlertTopic")
                .displayName("Billing Alert Topic")
                .build();

        billingTopic.addSubscription(EmailSubscription.Builder.create("your-email@example.com").build());

        // Budget for daily spend of £10 using CfnBudget (CloudFormation)
        CfnBudget dailyBudget = CfnBudget.Builder.create(this, "DailyBudget")
                .budget(CfnBudget.BudgetDataProperty.builder()
                        .budgetName("DailyBudget")
                        .budgetType("COST")
                        .timeUnit("DAILY")
                        .budgetLimit(CfnBudget.SpendProperty.builder()
                                .amount(10.0)
                                .unit("GBP")
                                .build())
                        .build())
                .notificationsWithSubscribers(Arrays.asList(
                        CfnBudget.NotificationWithSubscribersProperty.builder()
                                .notification(CfnBudget.NotificationProperty.builder()
                                        .notificationType("ACTUAL")
                                        .comparisonOperator("GREATER_THAN")
                                        .threshold(90.0)  // Trigger when usage reaches 90% of the budget
                                        .build())
                                .subscribers(Arrays.asList(
                                        CfnBudget.SubscriberProperty.builder()
                                                .subscriptionType("SNS")
                                                .address(billingTopic.getTopicArn())
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // Lambda function to stop EC2 and RDS when the budget is exceeded
        Function shutdownLambda = Function.Builder.create(this, "ShutdownLambda")
                .runtime(Runtime.PYTHON_3_9)
                .handler("shutdown.handler")
                .code(Code.fromAsset("src/main/resources/shutdown_lambda"))
                .timeout(Duration.seconds(300))
                .role(lambdaRole)
                .environment(Map.of(
                        "EC2_INSTANCE_ID", ec2Instance.getInstanceId(),
                        "RDS_INSTANCE_IDENTIFIER", rdsInstance.getInstanceIdentifier()
                ))
                .build();

        // CloudWatch Event Rule for budget notifications to trigger shutdown
        Rule budgetExceededRule = Rule.Builder.create(this, "BudgetExceededRule")
                .eventPattern(EventPattern.builder()
                        .source(Arrays.asList("aws.budgets"))
                        .detailType(Arrays.asList("Budget Threshold Breached"))
                        .build())
                .targets(Arrays.asList(new LambdaFunction(shutdownLambda)))
                .build();
    }
}
