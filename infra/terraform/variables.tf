variable "aws_region" {
  description = "Región de AWS"
  type        = string
  default     = "us-west-2"
}

variable "azure_tenant_id" {
  description = "ID del Tenant de Azure AD"
  type        = string
  default     = "cd89ce2b-8f61-42ab-9a9a-bf532acace5d"
}

variable "azure_client_id" {
  description = "ID del Cliente/Aplicación de Azure AD"
  type        = string
  default     = "2460c449-b914-47fb-8a1c-58da535aba2d"
}

variable "public_key" {
  description = "Llave pública SSH para las instancias EC2"
  type        = string
  default     = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIOMqqnkVzRm0SdG60O0fG578Qm93NdPZ6fA83n111111 vidasalud-key"
}