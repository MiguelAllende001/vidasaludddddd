terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}


resource "aws_vpc" "vidasalud_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "vidasalud-vpc"
  }
}


resource "aws_subnet" "vidasalud_public_subnet" {
  vpc_id                  = aws_vpc.vidasalud_vpc.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = "${var.aws_region}a"

  tags = {
    Name = "vidasalud-public-subnet"
  }
}


resource "aws_internet_gateway" "vidasalud_igw" {
  vpc_id = aws_vpc.vidasalud_vpc.id

  tags = {
    Name = "vidasalud-igw"
  }
}


resource "aws_route_table" "vidasalud_public_rt" {
  vpc_id = aws_vpc.vidasalud_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.vidasalud_igw.id
  }

  tags = {
    Name = "vidasalud-public-rt"
  }
}


resource "aws_route_table_association" "vidasalud_rta" {
  subnet_id      = aws_subnet.vidasalud_public_subnet.id
  route_table_id = aws_route_table.vidasalud_public_rt.id
}


resource "aws_security_group" "vidasalud_sg" {
  name        = "vidasalud-security-group"
  description = "Security group para Apps, Mensajeria y Base de Datos"
  vpc_id      = aws_vpc.vidasalud_vpc.id


  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }


  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

 
  ingress {
    from_port   = 4200
    to_port     = 4200
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }


  ingress {
    from_port   = 15672
    to_port     = 15672
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

 
  ingress {
    from_port   = 8081
    to_port     = 8081
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }


  ingress {
    from_port   = 1521
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

 
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "vidasalud-sg"
  }
}

# React Frontend
  ingress {
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

resource "aws_key_pair" "deployer" {
  key_name   = "vidasalud-key"
  public_key = var.public_key
}


data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"]

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
}


resource "aws_instance" "vidasalud_server" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.large"
  key_name               = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.vidasalud_sg.id]
  subnet_id              = aws_subnet.vidasalud_public_subnet.id

  root_block_device {
    volume_size = 30
  }

  user_data = <<-EOF
            
              apt-get update -y
              apt-get install -y ca-certificates curl gnupg lsb-release git
              mkdir -p /etc/apt/keyrings
              curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
              echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
              apt-get update -y
              apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
              systemctl start docker
              systemctl enable docker
              usermod -aG docker ubuntu
              EOF

  tags = {
    Name = "vidasalud-ec2-apps"
  }
}


resource "aws_instance" "vidasalud_db_server" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.medium"
  key_name               = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.vidasalud_sg.id]
  subnet_id              = aws_subnet.vidasalud_public_subnet.id

  root_block_device {
    volume_size = 20
  }

  tags = {
    Name = "vidasalud-ec2-db"
  }
}