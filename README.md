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
