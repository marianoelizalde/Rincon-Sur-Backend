# Rincón Sur — Microservicios

Backend de la aplicación Rincón Sur: microservicios Spring Boot con descubrimiento en Eureka y un API Gateway como único punto de entrada para la app Android.

Curso DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

App móvil: repositorio Rincon-Sur-App del mismo autor.

## Integrantes

- Mariano Elizalde
- Lukas LLanten

## Tecnologías

- Java 17, Spring Boot 3.2.5, Spring Cloud 2023.0.1
- Spring Data JPA y MySQL
- Eureka Server y Spring Cloud Gateway
- OpenFeign para la comunicación entre servicios
- Swagger (springdoc) en cada servicio
- Pruebas con JUnit 5, Mockito y JaCoCo

## Módulos

| Módulo | Puerto | Base de datos | Función |
|---|---|---|---|
| eureka-server | 8761 | — | Registro de servicios |
| api-gateway | 9090 | — | Punto de entrada, rutas `lb://` hacia cada servicio |
| perfiles-service | 8081 | rincon_perfiles | Perfiles Supervisor y Observador |
| usuarios-service | 8082 | rincon_usuarios | Cuentas, inicio de sesión, recuperación y foto de perfil |
| sesiones-service | 8083 | rincon_sesiones | Sesiones de observación, accesos y estados |
| registros-service | 8084 | rincon_registros | Registros de personas observadas y estadísticas |

Cada servicio sigue las capas `model`, `dto`, `repository`, `service` y `controller`. Las bases de datos se crean solas al iniciar (`createDatabaseIfNotExist=true`).

Comunicación entre servicios con OpenFeign: usuarios consulta a perfiles; sesiones consulta a usuarios y registros; registros consulta a sesiones.

## Pasos para ejecutar

Requisitos: JDK 17 y MySQL en el puerto 3306 con usuario `root` sin contraseña (XAMPP sirve).

1. Encender MySQL.
2. Ejecutar `iniciar-todo.cmd` (Windows). Abre una ventana por módulo y los inicia en orden: Eureka, perfiles, usuarios, sesiones, registros y gateway.
3. Esperar unos 30 segundos y abrir `http://localhost:8761`: deben verse los servicios registrados.
4. También se puede iniciar un solo módulo con `iniciar-modulo.cmd nombre-de-la-carpeta`, o con `mvnw.cmd spring-boot:run` dentro de su carpeta.

Documentación Swagger de cada servicio: `http://localhost:PUERTO/swagger-ui.html`.

Usuarios de prueba creados la primera vez:

| Rol | Correo | Contraseña |
|---|---|---|
| Supervisor | supervisor@rinconsur.cl | Admin123! |
| Observador | observador@rinconsur.cl | Obser123! |

## Endpoints

La app los consume todos por el gateway: `http://localhost:9090`. Las acciones sensibles llevan el encabezado `X-Usuario-Id` con el id del usuario que las ejecuta.

Los errores responden con `{"message": "..."}` y el código HTTP correspondiente (400, 401, 403, 404 o 409).

### perfiles-service

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/perfiles` | Lista los perfiles |
| GET | `/api/perfiles/{id}` | Trae un perfil |
| POST | `/api/perfiles` | Crea un perfil |
| PUT | `/api/perfiles/{id}` | Modifica un perfil |

### usuarios-service

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Lista con filtros `buscar`, `perfilId`, `activo` |
| GET | `/api/usuarios/{id}` | Trae un usuario |
| POST | `/api/usuarios` | Crea una cuenta (solo supervisor) |
| POST | `/api/usuarios/login` | Inicia sesión |
| POST | `/api/usuarios/recuperar` | Cambia la contraseña con correo y RUT |
| PUT | `/api/usuarios/{id}` | Actualiza datos (supervisor o el mismo usuario) |
| PUT | `/api/usuarios/{id}/contrasena` | Cambia la contraseña con la actual |
| PATCH | `/api/usuarios/{id}/activo?valor=` | Activa o desactiva (solo supervisor) |
| PATCH | `/api/usuarios/{id}/perfil?perfilId=` | Cambia el rol (solo supervisor) |
| POST | `/api/usuarios/{id}/avatar` | Sube la foto de perfil |
| GET | `/api/usuarios/{id}/avatar` | Descarga la foto de perfil |
| DELETE | `/api/usuarios/{id}` | Elimina un usuario (solo supervisor) |

### sesiones-service

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/accesos` | Lista los accesos |
| GET | `/api/accesos/{id}` | Trae un acceso |
| POST | `/api/accesos` | Crea un acceso |
| PUT | `/api/accesos/{id}` | Modifica un acceso |
| GET | `/api/estados-sesion` | Lista los estados de sesión |
| GET | `/api/sesiones` | Lista con filtros de fecha, turno, estado, observador y acceso |
| GET | `/api/sesiones/{id}?conteo=` | Trae una sesión, con conteo de registros válidos opcional |
| POST | `/api/sesiones` | Programa una sesión |
| PATCH | `/api/sesiones/{id}/iniciar` | Pasa a EN_CURSO |
| PATCH | `/api/sesiones/{id}/finalizar` | Pasa a FINALIZADA |
| PATCH | `/api/sesiones/{id}/cancelar` | Cancela una sesión programada |
| PATCH | `/api/sesiones/{id}/validar?idValidador=` | Valida una sesión finalizada |
| PATCH | `/api/sesiones/{id}/rechazar` | Rechaza con motivo |

Estados de una sesión: PROGRAMADA, EN_CURSO, FINALIZADA, VALIDADA, RECHAZADA y CANCELADA. Una sesión se valida solo con al menos 30 registros válidos.

### registros-service

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/registros` | Registra una persona observada |
| PATCH | `/api/registros/{id}/anular` | Anula un registro |
| GET | `/api/registros/sesion/{sesionId}` | Lista los registros de una sesión |
| GET | `/api/registros/sesion/{sesionId}/validos/count` | Cuenta los registros válidos |
| GET | `/api/registros/sesion/{sesionId}/estadisticas` | Totales y porcentaje de tránsito |
| POST | `/api/registros/sesion/{sesionId}/deshacer-ultimo` | Anula el último registro válido |

## Pruebas

Cada servicio incluye pruebas unitarias con JUnit 5 y Mockito, y JaCoCo para la cobertura. Para ejecutarlas dentro de la carpeta de un servicio: `mvnw.cmd test`.

## Organización del equipo

La planificación de tareas está en el tablero de Trello del equipo, y el trabajo de cada integrante se ve en los commits de este repositorio y del repositorio de la app.
