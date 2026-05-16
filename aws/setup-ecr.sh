#!/bin/bash
# =============================================================
# AWS ECR SETUP SCRIPT
# Run this ONCE to create your ECR repository
# =============================================================

set -e

# ── Configuration ──────────────────────────────────────────
AWS_REGION="us-east-1"
REPO_NAME="attendance-management-system"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

echo "============================================"
echo " Setting up ECR for: $REPO_NAME"
echo " Account: $AWS_ACCOUNT_ID"
echo " Region:  $AWS_REGION"
echo "============================================"

# Create ECR repository
echo "Creating ECR repository..."
aws ecr create-repository \
    --repository-name "$REPO_NAME" \
    --region "$AWS_REGION" \
    --image-scanning-configuration scanOnPush=true \
    --image-tag-mutability MUTABLE \
    2>/dev/null || echo "Repository already exists - skipping creation"

# Get repository URI
REPO_URI=$(aws ecr describe-repositories \
    --repository-names "$REPO_NAME" \
    --region "$AWS_REGION" \
    --query 'repositories[0].repositoryUri' \
    --output text)

echo ""
echo "✅ ECR Repository ready!"
echo "   URI: $REPO_URI"
echo ""

# Set lifecycle policy (keep last 10 images)
echo "Setting lifecycle policy (keep last 10 images)..."
aws ecr put-lifecycle-policy \
    --repository-name "$REPO_NAME" \
    --region "$AWS_REGION" \
    --lifecycle-policy-text '{
        "rules": [
            {
                "rulePriority": 1,
                "description": "Keep last 10 images",
                "selection": {
                    "tagStatus": "tagged",
                    "tagPrefixList": [""],
                    "countType": "imageCountMoreThan",
                    "countNumber": 10
                },
                "action": { "type": "expire" }
            }
        ]
    }'

echo "✅ Lifecycle policy applied"
echo ""
echo "============================================"
echo " Add this to Jenkins environment variables:"
echo " ECR_REGISTRY = $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"
echo " ECR_REPO_NAME = $REPO_NAME"
echo "============================================"
