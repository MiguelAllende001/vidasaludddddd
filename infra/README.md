# Infraestructura VidaSalud (Docker Compose)

Corresponde al punto 7 del Caso VidaSalud: 3 "instancias" (en Docker Compose
local, 3 stacks; en AWS real, 3 EC2 separadas) con un `compose.yml` cada una:

| Carpeta         | Representa | Contiene |
|-----------------|------------|----------|
| `infra/apps`    | `ec2-apps`  | Los 6 microservicios Spring Boot (`bff`, `appointments`, `catalog`, `notify`, `audit`, `report`). Oracle se conecta desde afuera (RDS real, o un contenedor local opcional — ver más abajo) |
| `infra/mq`      | `ec2-mq`    | Cluster RabbitMQ de 2 nodos + Management UI (topología del punto 8) |
| `infra/kafka`   | `ec2-kafka` | Zookeeper (3 nodos) + Kafka (3 brokers) + Kafka UI (topología del punto 9) |

## Sobre la base de datos: RDS, no un contenedor

El Caso VidaSalud pide desplegar en **AWS EC2**, y la lista de `ec2-apps`
(punto 7) solo incluye los microservicios — no una base de datos. La lectura
correcta es que Oracle vive en una instancia **RDS** aparte (gestionada por
AWS), no como un contenedor más de este compose. Por eso `infra/apps/compose.yml`
**no levanta Oracle por defecto**: los microservicios se conectan a donde le
digas vía `DB_HOST`/`DB_PORT`/`DB_SERVICE`/`DB_PASSWORD`.

Para pruebas rápidas en tu laptop sin pagar/crear un RDS, el compose incluye
igual un contenedor Oracle XE, pero apagado por defecto (Docker Compose
"profile" `local-db`). Instrucciones de ambos modos, dentro del propio
`infra/apps/compose.yml`.

Antes de levantar los microservicios contra un RDS real, corre una vez
`infra/apps/init-db/01-create-users.sql` a mano contra tu endpoint de RDS
(RDS no soporta la convención de "correr scripts al arrancar el contenedor"
que sí usa el contenedor local) — y ojo con la nota dentro de ese archivo
sobre el nombre de tu PDB/servicio, que casi seguro no se llama `XEPDB1` en tu RDS.

## Orden recomendado para levantarlo todo en una sola máquina (demo/local)

Los 3 stacks son independientes entre sí (como lo serían 3 EC2 reales), pero
para probarlos juntos en tu laptop necesitan compartir una red Docker:

```bash
# 1) Una sola vez: crea la red compartida
docker network create vidasalud-net

# 2) Kafka primero (tarda más en levantar: 3 Zookeeper + 3 brokers)
cd infra/kafka
docker compose up -d
docker compose ps   # espera a que kafka1/2/3 y zookeeper1/2/3 esten "healthy"/"Up"

# 3) RabbitMQ
cd ../mq
cp .env.example .env   # y cambia RABBITMQ_ERLANG_COOKIE por una clave propia
docker compose up -d
docker compose logs -f rabbitmq2   # deberias ver "Nodo unido al cluster."

# 4a) Las apps, CONTRA UN RDS REAL (mas fiel al caso):
cd ../apps
export DB_HOST=tu-endpoint.rds.amazonaws.com DB_PASSWORD=tu-clave
docker compose up -d --build

# 4b) Las apps, SIN RDS (solo para probar rapido, con Oracle en un contenedor local):
cd ../apps
docker compose --profile local-db up -d oracle-db
docker compose ps oracle-db   # espera a que quede "healthy"
docker compose --profile local-db up -d --build
```

## Verificación rápida

- BFF: `http://localhost:8080/actuator/health` (o cualquier endpoint `/api/...` con un JWT válido).
- RabbitMQ Management UI: `http://localhost:15672` (usuario/clave: `vidasalud`/`vidasalud`, definidos en `infra/mq/definitions.json`). Deberías ver los exchanges `cmd.direct`/`cmd.topic`/`cmd.dead.dlx` y las 6 colas del punto 8, y en la pestaña "Cluster" los 2 nodos.
- Kafka UI: `http://localhost:9000`. Deberías ver los tópicos `appointments.events` y `audit.timeline`, cada uno con 3 particiones.
- Oracle (solo si usaste `--profile local-db`): puerto `1521` publicado solo para debug local con un cliente SQL (usuario `vidasalud_appointments` / `vidasalud_catalog` / `vidasalud_audit` / `vidasalud_report`, clave `changeme` salvo que la hayas cambiado). Si usas RDS, verifica ahí directamente.

## Variables de entorno que probablemente quieras cambiar

Todas tienen un valor por defecto que funciona "out of the box" para probar localmente, pero en un despliegue real deberías sobreescribirlas (por ejemplo con un archivo `.env` junto a cada `compose.yml`, o como variables del sistema en la EC2):

- `infra/apps/compose.yml`: `AZURE_TENANT_ID`, `AZURE_API_CLIENT_ID` (los del App Registration real de Azure AD); `DB_HOST`/`DB_PORT`/`DB_SERVICE`/`DB_PASSWORD` (tu RDS real, o déjalos con su valor por defecto si usas el contenedor local con `--profile local-db`; ⚠️ si cambias `DB_PASSWORD`, también debes actualizar `infra/apps/init-db/01-create-users.sql`, que no se puede parametrizar con variables de entorno); `ORACLE_SYSTEM_PASSWORD` (solo aplica al contenedor local); `RABBITMQ_HOST`/`KAFKA_BOOTSTRAP_SERVERS` (si `ec2-mq`/`ec2-kafka` están en otra máquina, aquí van sus IP/DNS reales en vez del nombre del contenedor).
- `infra/mq/compose.yml`: `RABBITMQ_ERLANG_COOKIE` (debe ser igual en ambos nodos; sin esto, no logran clusterizar).

## Limitaciones conocidas de esta primera versión

Para ser transparentes sobre qué quedó simplificado a propósito:

- **Balanceo de clientes entre nodos de RabbitMQ**: los microservicios se conectan siempre a `rabbitmq1`. El cluster de 2 nodos da redundancia de datos/metadata, pero un balanceador (HAProxy, o el propio Service Discovery de AWS) sería el siguiente paso para que un cliente pueda seguir publicando si `rabbitmq1` específicamente cae.
- **Tópicos `*.DLT` por consumidor** (mencionados en el punto 9 del caso): `audit` y `report` hoy manejan un mensaje mal formado con un `try/catch` que solo registra el error (no bloquea al resto del tópico), pero no republican el mensaje fallido a un tópico `*.DLT`. Conectar `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` de Spring Kafka sería la extensión natural.
- **Frontend**: el caso pide Angular + MSAL; este proyecto usa React. Si no lo has conversado con el docente, es el punto que más nota arriesga de todo lo demás construido aquí.
