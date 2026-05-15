# ✅ Attendance Management System — Complete CI/CD Setup Guide
### End-to-End: Spring Boot → Jenkins → ECR → EC2 with ALB

---

## 📁 Project Structure

```
attendance-management-system/
├── src/
│   ├── main/java/com/attendance/
│   │   ├── AttendanceManagementApplication.java   ← Spring Boot entry point
│   │   ├── controller/AttendanceController.java   ← REST endpoints
│   │   ├── service/AttendanceService.java         ← Business logic
│   │   └── model/
│   │       ├── CheckIn.java                       ← Response model
│   │       └── CheckInRequest.java                ← Request model
│   ├── main/resources/application.properties
│   └── test/java/com/attendance/
│       └── AttendanceControllerTest.java          ← JUnit tests
├── jenkins/
│   ├── Jenkinsfile.ci    ← Phase 1: Build & Push to ECR
│   └── Jenkinsfile.cd    ← Phase 2+3: Deploy to EC2 with ALB
├── aws/
│   ├── setup-ecr.sh      ← One-time ECR repository creation
│   └── ec2-userdata.sh   ← EC2 bootstrap script
├── Dockerfile            ← Multi-stage Docker build
├── pom.xml               ← Maven build config
└── .gitignore
```

---

## PHASE 0 — Prerequisites

### 1. GitHub Repository
```bash
git init
git remote add origin https://github.com/YOUR_USERNAME/attendance-management-system.git
git add .
git commit -m "Initial commit: Attendance Management System"
git push -u origin main
```

### 2. AWS Setup (do these in AWS Console)

**A. Create IAM User for Jenkins**
- Go to IAM → Users → Create User → name: `jenkins-cicd`
- Attach policies:
  - `AmazonEC2ContainerRegistryFullAccess`
  - `AmazonEC2FullAccess`
  - `ElasticLoadBalancingFullAccess`
- Create Access Key → save the **Access Key ID** and **Secret Access Key**

**B. Create ECR Repository**
```bash
# Run this on your local machine with AWS CLI configured
bash aws/setup-ecr.sh
```
Note the output URI: `YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/attendance-management-system`

**C. Launch 2 EC2 Instances**
- Go to EC2 → Launch Instance (do this TWICE)
- AMI: Amazon Linux 2023
- Instance type: t2.micro (free tier)
- Key Pair: create/use existing → **save the .pem file**
- Security Group rules:
  - SSH (port 22) from Jenkins server IP
  - HTTP (port 80) from ALB Security Group
- User Data: paste contents of `aws/ec2-userdata.sh`
- **Note down both Private IP addresses**

**D. Create Application Load Balancer**
1. EC2 → Load Balancers → Create Load Balancer → Application Load Balancer
2. Name: `attendance-prod-alb`
3. Scheme: Internet-facing
4. Listeners: HTTP port 80
5. Create Target Group:
   - Name: `attendance-prod-tg`
   - Target type: Instances
   - Protocol: HTTP, Port: 80
   - Health check path: `/attendance/status`
   - Health check interval: 30s
6. Register both EC2 instances in the Target Group
7. **Note the ALB DNS name** (e.g., `attendance-prod-alb-123456.us-east-1.elb.amazonaws.com`)

Do the same for Staging: `attendance-staging-alb` and `attendance-staging-tg`

---

## PHASE 1 — Jenkins Setup

### Install Jenkins (if not already installed)
```bash
# On your Jenkins server (can be another EC2):
sudo yum update -y
sudo yum install java-17-amazon-corretto -y
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
sudo yum install jenkins -y
sudo systemctl start jenkins
sudo systemctl enable jenkins
# Access at http://JENKINS_IP:8080
```

### Required Jenkins Plugins
Install via: Manage Jenkins → Plugins → Available:
- ✅ **GitHub Integration Plugin** (for webhook)
- ✅ **Pipeline** (already installed)
- ✅ **AWS Steps** (for withAWS)
- ✅ **Email Extension Plugin** (for emailext)
- ✅ **SSH Agent Plugin** (for sshagent)
- ✅ **JUnit Plugin** (for test results)
- ✅ **Docker Pipeline**

### Configure Jenkins Credentials
Go to: Manage Jenkins → Credentials → System → Global → Add Credential

| ID | Type | Value |
|----|------|-------|
| `aws-credentials` | AWS Credentials | Jenkins IAM Access Key + Secret |
| `AWS_ACCOUNT_ID` | Secret Text | Your 12-digit AWS account ID |
| `ec2-ssh-key` | SSH Username with private key | ec2-user + .pem file contents |

### Configure Email (SMTP)
Manage Jenkins → System → E-mail Notification:
- SMTP server: `smtp.gmail.com`
- Port: 465 / 587
- Use SSL: ✅
- Username: your Gmail
- Password: Gmail App Password (not regular password)

---

## PHASE 2 — Create Jenkins Pipelines

### Pipeline 1: CI (Build) Pipeline

1. New Item → Name: `attendance-ci` → Pipeline
2. Under **Build Triggers**:
   - ✅ Check **"GitHub hook trigger for GITScm polling"**
3. Under **Pipeline**:
   - Definition: Pipeline script from SCM
   - SCM: Git
   - Repository URL: `https://github.com/YOUR_USERNAME/attendance-management-system`
   - Script Path: `jenkins/Jenkinsfile.ci`
4. Save

### Pipeline 2: CD (Deploy) Pipeline

1. New Item → Name: `attendance-cd` → Pipeline
2. ✅ Check **"This project is parameterized"** (parameters are defined in the Jenkinsfile)
3. Under **Pipeline**:
   - Definition: Pipeline script from SCM
   - SCM: Git
   - Repository URL: `https://github.com/YOUR_USERNAME/attendance-management-system`
   - Script Path: `jenkins/Jenkinsfile.cd`
4. Save

---

## PHASE 3 — GitHub Webhook Setup

1. Go to your GitHub repo → Settings → Webhooks → Add webhook
2. Payload URL: `http://YOUR_JENKINS_IP:8080/github-webhook/`
3. Content type: `application/json`
4. Events: ✅ **Just the push event**
5. Active: ✅
6. Click **Add webhook**

**Test it:**
```bash
git commit --allow-empty -m "test: trigger webhook"
git push origin main
# Watch Jenkins CI pipeline start automatically!
```

---

## PHASE 4 — Running the Pipelines

### Run CI Pipeline (auto or manual)
- **Auto**: Push any code to GitHub → webhook fires → CI runs
- **Manual**: Click "Build Now" on `attendance-ci`

**What it does:**
1. Checkout code from GitHub
2. `mvn test` — runs 5 JUnit tests
3. `mvn package` — builds the .jar
4. `docker build` — creates container image
5. `docker push` to ECR with tag = build number
6. Email sent with ECR image URI and build number

### Run CD Pipeline (manual)
1. Go to `attendance-cd` → Build with Parameters
2. Select: **ENVIRONMENT** = `Production`
3. Enter: **BUILD_NUMBER_TO_DEPLOY** = `5` (or whatever CI build number)
4. Click Build

**What it does:**
1. Validates parameters
2. Fetches EC2 IPs from ALB Target Group (auto-discovery!)
3. SSH into EC2-1 → pulls image from ECR → runs container → health check
4. SSH into EC2-2 → pulls image from ECR → runs container → health check
5. Verifies ALB routes traffic to both instances
6. Email sent with ALB DNS link

---

## API Endpoints Reference

| Method | Endpoint | Description | Sample Response |
|--------|----------|-------------|-----------------|
| GET | `/attendance/status` | Service health | `{"status":"UP","totalCheckedIn":3}` |
| POST | `/attendance/checkin` | Employee check-in | `{"status":"SUCCESS","employeeId":"EMP001"}` |
| GET | `/attendance/records` | All records | `{"totalRecords":3,"records":[...]}` |
| GET | `/actuator/health` | Spring health | `{"status":"UP"}` |

### Test via cURL:
```bash
# Health check
curl http://ALB_DNS/attendance/status

# Check in an employee
curl -X POST http://ALB_DNS/attendance/checkin \
  -H "Content-Type: application/json" \
  -d '{"employeeId":"EMP001","employeeName":"Alice","department":"Engineering"}'

# View all records
curl http://ALB_DNS/attendance/records
```

---

## Submission Checklist

| Item | How to get it |
|------|---------------|
| ✅ GitHub repo link | Your repo URL |
| ✅ ECR screenshot | AWS Console → ECR → attendance-management-system → Images |
| ✅ EC2 instances screenshot | AWS Console → EC2 → Instances (both Running) |
| ✅ Target Group screenshot | EC2 → Target Groups → both instances Healthy |
| ✅ ALB screenshot | EC2 → Load Balancers → attendance-prod-alb |
| ✅ CI pipeline console log | Jenkins → attendance-ci → last build → Console Output |
| ✅ Webhook trigger proof | Console log shows "Started by GitHub push" |
| ✅ Email notification | Forward the build email |
| ✅ CD pipeline console log | Jenkins → attendance-cd → last build → Console Output |

---

## Troubleshooting

**Docker permission denied on EC2:**
```bash
sudo usermod -aG docker ec2-user
newgrp docker
```

**ECR login fails:**
```bash
aws configure  # ensure correct region and credentials
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ACCOUNT.dkr.ecr.us-east-1.amazonaws.com
```

**SSH connection refused:**
- Ensure port 22 is open in EC2 security group for Jenkins server IP
- Verify the .pem key is correctly added to Jenkins credentials

**Health check fails on ALB:**
- Check ALB Target Group health check path is `/attendance/status`
- Ensure port 80 is open in EC2 security group from ALB security group
- Wait 1-2 minutes for health checks to pass after container starts

**Webhook not triggering:**
- Verify Jenkins URL is publicly accessible (not localhost)
- Check webhook delivery in GitHub → Settings → Webhooks → Recent Deliveries
- Ensure "GitHub hook trigger for GITScm polling" is checked in Jenkins job

---

*All files are production-ready. Push to GitHub, configure Jenkins credentials, and the pipeline will work end-to-end.*
