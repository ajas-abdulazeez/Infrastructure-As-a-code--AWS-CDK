# AWS CDK Project: Multi-Region Infrastructure with VPC, EC2, RDS, Lambda, and Budget Alerts

## Project Overview

This project uses the AWS Cloud Development Kit (CDK) to provision a multi-region infrastructure on AWS. It consists of the following components:

- **VPC**: A Virtual Private Cloud with one public subnet and one private subnet.
- **EC2 Instance**: An EC2 instance located in the public subnet.
- **RDS Database**: A PostgreSQL database instance located in the private subnet.
- **Lambda Functions**: A Lambda function to handle API requests and another to shut down resources when budget limits are exceeded.
- **API Gateway**: An API Gateway to connect with the Lambda function.
- **Budget Alerts**: CloudWatch and SNS for monitoring costs and alerting when spending approaches a defined budget.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Usage](#usage)
4. [Architecture](#architecture)
5. [Configuration](#configuration)
6. [Budget Management](#budget-management)
7. [Cleanup](#cleanup)
8. [License](#license)
9. [Contributing](#contributing)

## Prerequisites

Before you begin, ensure you have the following:

- **AWS Account**: Create an AWS account if you do not have one.
- **AWS CLI**: Install and configure the AWS CLI with your AWS credentials.
- **Node.js**: Install Node.js (version 14.x or later).
- **AWS CDK**: Install the AWS CDK globally using npm:
  ```bash
  npm install -g aws-cdk

## Installation

Clone the repository:
  
  ```bash
git clone https://github.com/yourusername/aws-cdk-multi-region-infrastructure.git

Navigate to the project folder
  ```bash
cd aws-cdk-multi-region-infrastructure


Install dependencies:

  ```bash
npm install```
Bootstrap your environment (if you haven't already):

  ```bash
cdk bootstrap
Usage
To deploy the infrastructure, use the following command:

  ```bash
cdk deploy
To specify a particular region for deployment, set the AWS_DEFAULT_REGION environment variable:

  ```bash
export AWS_DEFAULT_REGION=us-west-1
cdk deploy
Accessing the Application
After deployment, you will receive the API endpoint URL. You can use this URL to send GET requests to the API:

  ```bash
curl https://{api-id}.execute-api.{region}.amazonaws.com/prod/hello

## Architecture
This project includes the following components:

VPC: A Virtual Private Cloud with one public subnet and one private subnet.
Public Subnet: Hosts the EC2 instance and Lambda function.
Private Subnet: Hosts the PostgreSQL RDS database.
API Gateway: Routes HTTP requests to the Lambda function.
SNS Topic: Sends notifications for budget alerts.
Architecture Diagram

## Configuration
Environment Variables
The shutdown Lambda function uses the following environment variables:

EC2_INSTANCE_ID: The ID of the EC2 instance.
RDS_INSTANCE_IDENTIFIER: The identifier of the RDS instance.
Email Notifications
To receive budget alerts via email, replace "your-email@example.com" in the MultistackStack.java file with your actual email address.


## Budget Management
This project implements a budget alert mechanism using AWS Budgets. It will notify you when your daily spending reaches 90% of the defined budget (£10). If the budget limit is exceeded, a Lambda function will be triggered to shut down both the EC2 instance and the RDS database.

Monitoring Costs
You can monitor costs directly in the AWS Billing Dashboard. Look for the budgets you’ve created to get insights on your spending.

## Cleanup
To delete all resources created by this stack, run:

  ```bash
cdk destroy

## License
This project is licensed under the MIT License. See the LICENSE file for details.

## Contributing
Contributions are welcome! Please feel free to submit issues, pull requests, or suggestions.

## Contact
For questions or feedback, please contact me at [your-email@example.com].

Happy Coding!

markdown
Copy code

### Customization Notes:
- **Repository URL**: Replace `https://github.com/yourusername/aws-cdk-multi-region-infrastructure.git` with your actual repository URL.
- **Architecture Diagram**: will update later.
- **Contact Information**: Connect me via Linkedin.

Feel free to modify any sections based on your specific requirements or preferences!



