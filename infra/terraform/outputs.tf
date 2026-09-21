output "ec2_apps_public_ip" {
  value       = aws_instance.vidasalud_server.public_ip
  description = "IP publica del servidor de aplicaciones"
}

output "ec2_db_public_ip" {
  value       = aws_instance.vidasalud_db_server.public_ip
  description = "IP publica del servidor de base de datos"
}