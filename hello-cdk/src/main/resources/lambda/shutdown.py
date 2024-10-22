import boto3
import os

ec2_client = boto3.client('ec2')
rds_client = boto3.client('rds')

def handler(event, context):
    ec2_instance_id = os.environ['EC2_INSTANCE_ID']
    rds_instance_identifier = os.environ['RDS_INSTANCE_IDENTIFIER']

    # Stop EC2 instance
    try:
        print(f"Stopping EC2 instance: {ec2_instance_id}")
        ec2_client.stop_instances(InstanceIds=[ec2_instance
