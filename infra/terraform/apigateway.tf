# AWS API GATEWAY (HTTP API)

resource "aws_apigatewayv2_api" "vidasalud_api" {
  name          = "vidasalud-http-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = [
      "http://localhost:3000",
      "http://localhost:5173",
      "http://${aws_instance.vidasalud_server.public_ip}:3000"
    ]
    allow_methods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_headers = ["Authorization", "Content-Type"]
    max_age       = 300
  }
}

# Authorizer JWT con Azure AD (IDaaS)
resource "aws_apigatewayv2_authorizer" "azure_ad_auth" {
  api_id           = aws_apigatewayv2_api.vidasalud_api.id
  authorizer_type  = "JWT"
  identity_sources = ["$request.header.Authorization"]
  name             = "AzureAD-Authorizer"

  jwt_configuration {
    issuer   = "https://login.microsoftonline.com/${var.azure_tenant_id}/v2.0"
    audience = ["api://${var.azure_client_id}"]
  }
}

# Integración HTTP Proxy apuntando al puerto 8080 del BFF en la EC2
resource "aws_apigatewayv2_integration" "bff_integration" {
  api_id                 = aws_apigatewayv2_api.vidasalud_api.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = "http://${aws_instance.vidasalud_server.public_ip}:8080/{proxy}"
  payload_format_version = "1.0"
}

# Rutas del sistema conectadas al BFF y protegidas con JWT
resource "aws_apigatewayv2_route" "appointments_route" {
  api_id             = aws_apigatewayv2_api.vidasalud_api.id
  route_key          = "ANY /api/appointments/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.bff_integration.id}"
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.azure_ad_auth.id
}

resource "aws_apigatewayv2_route" "catalog_route" {
  api_id             = aws_apigatewayv2_api.vidasalud_api.id
  route_key          = "ANY /api/catalog/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.bff_integration.id}"
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.azure_ad_auth.id
}

resource "aws_apigatewayv2_route" "report_route" {
  api_id             = aws_apigatewayv2_api.vidasalud_api.id
  route_key          = "ANY /api/report/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.bff_integration.id}"
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.azure_ad_auth.id
}

resource "aws_apigatewayv2_route" "audit_route" {
  api_id             = aws_apigatewayv2_api.vidasalud_api.id
  route_key          = "ANY /api/audit/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.bff_integration.id}"
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.azure_ad_auth.id
}

# Despliegue automático del API Gateway
resource "aws_apigatewayv2_stage" "default_stage" {
  api_id      = aws_apigatewayv2_api.vidasalud_api.id
  name        = "$default"
  auto_deploy = true
}