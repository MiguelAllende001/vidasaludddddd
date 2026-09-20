output "api_gateway_url" {
  value       = aws_apigatewayv2_api.vidasalud_api.api_endpoint
  description = "URL publica del API Gateway"
}