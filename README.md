# 🏥 Plataforma VidaSalud - Architecture Cloud Native & IDaaS

[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-v2.0%2B-blue?logo=docker)](https://www.docker.com/)
[![AWS EC2](https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazon-aws)](https://aws.amazon.com/)
[![Azure AD](https://img.shields.io/badge/Identity-Microsoft%20Entra%20ID-0089D6?logo=microsoft-azure)](https://azure.microsoft.com/)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot-6DB33F?logo=springboot)](https://spring.io/)

Sistema web para la gestión de servicios de salud desarrollado como arquitectura **Cloud Native**. El proyecto integra un Frontend SPA, microservicios en Spring Boot y un API Manager centralizado sobre **AWS EC2**, respaldado por autenticación corporativa con **Microsoft Entra ID (Azure AD)** mediante el protocolo **OAuth 2.0 / OIDC con PKCE**.

---

## 📐 Arquitectura del Sistema

```text
[ Cliente / Navegador ]
       │
       ▼ (OAuth 2.0 / PKCE)
[ Microsoft Entra ID (Azure AD) ] ── (Emisión JWT) ──┐
       │                                             │
       ▼ (Request + Bearer JWT)                      │
[ AWS API Gateway / API Manager ] ◄──────────────────┘
       │  (Validación de Claims: Issuer & Audience)
       ├───────────────────────┬───────────────────────┐
       ▼                       ▼                       ▼
[ Microservicio 1 ]     [ Microservicio 2 ]     [ Microservicio N ]
  (Spring Boot)           (Spring Boot)           (Spring Boot)
       │                       │                       │
       └───────────────────────┴───────────────────────┘
                               │
                       [ Base de Datos ]
🛠️ Tecnologías UtilizadasFrontend: Framework SPA (React/Angular) + Microsoft Authentication Library (MSAL).Backend: Spring Boot (Microservicios REST API).API Gateway / Router: AWS API Gateway / Nginx Proxy Inverso.IDaaS (Identity as a Service): Microsoft Entra ID (Azure AD).Infraestructura: AWS EC2 (Ubuntu Server), Docker & Docker Compose.Seguridad: OAuth 2.0, OpenID Connect (OIDC), PKCE, JSON Web Tokens (JWT), HTTPS/SSH Tunneling.🔐 Seguridad y Autenticación (Azure AD)El inicio de sesión y la autorización de recursos utilizan las siguientes especificaciones:Flujo OIDC con PKCE: La SPA genera dinámicamente un code_verifier y code_challenge (SHA-256) para solicitar el código de autorización a Azure AD sin exponer credenciales.Registro de Aplicación:App / Client ID: 2460c449-b914-47fb-8a1c-58da535aba2dScope de la API: api://2460c449-b914-47fb-8a1c-58da535aba2d/access_as_userValidación JWT en API Manager:Issuer: https://login.microsoftonline.com/<tenant-id>/v2.0Audience: Client ID del Frontend / API.Respuestas de Control: 200 OK (Petición válida), 401 Unauthorized (Token ausente/inválido), 403 Forbidden (Permisos insuficientes).🚀 Despliegue con Docker ComposePrerrequisitosDocker Engine v20.10+ y Docker Compose v2+.Acceso SSH a la instancia AWS EC2.1. Clonar el RepositorioBashgit clone [https://github.com/tu-usuario/vidasalud-backend.git](https://github.com/tu-usuario/vidasalud-backend.git)
cd vidasalud-backend
2. Configurar Variables de Entorno (.env)Crea un archivo .env en la raíz del proyecto con la configuración de Azure AD y los puertos:Fragmento de códigoPORT=3000
AZURE_TENANT_ID=cd89ce2b-8f61-42ab-9a9a-bf532acace5
AZURE_CLIENT_ID=2460c449-b914-47fb-8a1c-58da535aba2d
SPRING_PROFILES_ACTIVE=prod
3. Iniciar los ServiciosBash# Construir y levantar contenedores en segundo plano
docker compose up -d --build

# Verificar estado de los contenedores
docker compose ps
🌐 Conexión y Acceso Local (Túnel SSH)Dado que las políticas de seguridad de Microsoft Azure AD exigen conexiones HTTPS o el origen http://localhost para entornos de desarrollo/pruebas, se redirige el tráfico de la EC2 mediante un túnel SSH:Bash# Ejecutar en la terminal de tu máquina local
ssh -i "/ruta/a/tu-clave.pem" -L 3000:localhost:3000 ubuntu@34.220.78.56
Una vez abierto el túnel, accede a la aplicación desde el navegador en:http://localhost:3000📊 Matriz de Verificación de EndpointsMétodoEndpointCabeceraRespuesta EsperadaDescripciónGET/api/v1/healthNinguna200 OKVerificación de estado del API Gateway.GET/api/v1/recursosNinguna401 UnauthorizedRechazo por falta de Token JWT.GET/api/v1/recursosBearer <JWT_Invalido>401 UnauthorizedRechazo por firma alterada o token expirado.GET/api/v1/recursosBearer <JWT_Valido>200 OKRetorno de datos JSON desde el backend.
