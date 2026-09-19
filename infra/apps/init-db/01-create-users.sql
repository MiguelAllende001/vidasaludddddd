-- Crea un usuario/schema por microservicio (appointments, catalog, audit,
-- report) dentro del PDB por defecto (XEPDB1), que es el mismo que usan las
-- application-oracle.properties de cada servicio.
--
-- Cuando usas el contenedor local (profile "local-db" de infra/apps/compose.yml):
-- se ejecuta solo, la primera vez que arranca el contenedor (gvenzl/oracle-xe
-- corre todo lo que encuentre en /container-entrypoint-initdb.d/*.sql).
--
-- Cuando usas una instancia RDS Oracle real (lo que pide el Caso VidaSalud,
-- punto 7: despliegue en EC2, con Oracle como base de datos gestionada):
-- RDS no soporta la convencion de "initdb.d", asi que debes correr este
-- mismo script UNA VEZ a mano contra tu endpoint de RDS (por ejemplo con
-- SQL Developer, sqlplus, o el Query Editor de la consola de RDS), antes de
-- levantar los microservicios.
--
-- IMPORTANTE: este archivo es SQL estatico (no lee variables de entorno), asi
-- que si cambias DB_PASSWORD en infra/apps/compose.yml (o usas una clave
-- distinta en tu RDS), tambien debes cambiar la clave "changeme" aca abajo
-- por la misma, o los microservicios no van a poder autenticarse.
--
-- NOTA sobre RDS: la linea "ALTER SESSION SET CONTAINER = XEPDB1" es propia
-- de la arquitectura multitenant de Oracle XE (nuestro contenedor local). Tu
-- instancia RDS puede o no usar PDBs, y casi seguro NO se llama "XEPDB1" (es
-- el nombre/SID que le hayas puesto al crearla). Si tu RDS no es multitenant,
-- borra esa linea; si lo es, cambia "XEPDB1" por el nombre real de tu PDB.
-- En ambos casos, actualiza tambien DB_SERVICE en infra/apps/compose.yml
-- (o la variable de entorno DB_SERVICE) para que coincida.

ALTER SESSION SET CONTAINER = XEPDB1;

CREATE USER vidasalud_appointments IDENTIFIED BY changeme;
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO vidasalud_appointments;

CREATE USER vidasalud_catalog IDENTIFIED BY changeme;
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO vidasalud_catalog;

CREATE USER vidasalud_audit IDENTIFIED BY changeme;
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO vidasalud_audit;

CREATE USER vidasalud_report IDENTIFIED BY changeme;
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO vidasalud_report;
