#!/bin/bash
# =============================================================
# EC2 USER DATA SCRIPT
# Paste this in EC2 Launch → Advanced → User Data
# Run on BOTH EC2 instances in your Target Group
# =============================================================

set -e

# Update system
yum update -y

# Install Docker
yum install -y docker
systemctl start docker
systemctl enable docker

# Allow ec2-user to run docker without sudo
usermod -aG docker ec2-user

# Install AWS CLI v2
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install
rm -rf awscliv2.zip aws/

# Verify installations
docker --version
aws --version

echo "✅ EC2 setup complete - Docker and AWS CLI installed"
