# SIGRET - Backend

API REST desarrollada con Spring Boot para la gestion integral de reparaciones tecnicas.

## Tecnologias principales

| Tecnologia | Version | Descripcion |
|---|---|---|
| Java | 21 | Lenguaje de programacion |
| Spring Boot | 3.5.6 | Framework principal |
| Spring Security | - | Autenticacion y autorizacion |
| Spring Data JPA | - | Acceso a datos y ORM |
| MySQL | - | Base de datos relacional |
| Maven | - | Gestion de dependencias y build |

## Dependencias

| Libreria | Version | Uso |
|---|---|---|
| `spring-boot-starter-web` | 3.5.6 | API REST |
| `spring-boot-starter-security` | 3.5.6 | Seguridad (autenticacion/autorizacion) |
| `spring-boot-starter-data-jpa` | 3.5.6 | ORM con Hibernate |
| `spring-boot-starter-validation` | 3.5.6 | Validacion de DTOs y entidades |
| `spring-boot-starter-websocket` | 3.5.6 | Notificaciones en tiempo real |
| `spring-boot-starter-mail` | 3.5.6 | Envio de correos electronicos |
| `spring-boot-starter-actuator` | 3.5.6 | Monitoreo y health checks |
| `spring-boot-devtools` | 3.5.6 | Hot reload en desarrollo |
| `mysql-connector-j` | - | Driver JDBC para MySQL |
| `lombok` | - | Reduccion de boilerplate (getters, setters, builders) |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | 0.12.6 | Generacion y validacion de tokens JWT |
| `springdoc-openapi-starter-webmvc-ui` | 2.7.0 | Documentacion Swagger/OpenAPI |
| `itext-kernel` / `itext-layout` / `itext-io` | 7.2.5 | Generacion de documentos PDF |

## Dependencias de testing

| Libreria | Uso |
|---|---|
| `spring-boot-starter-test` | Framework de testing (JUnit 5, Mockito) |
| `spring-security-test` | Testing de seguridad |

## Arquitectura

```
com.sigret/
├── config/                  # Configuracion (Security, CORS, WebSocket, OpenAPI)
├── controllers/             # Controladores REST organizados por dominio
│   ├── cliente/
│   ├── dashboard/
│   ├── empleado/
│   ├── equipo/
│   ├── servicio/
│   ├── presupuesto/
│   ├── ordenTrabajo/
│   ├── usuario/
│   ├── publico/             # Endpoints publicos (sin autenticacion)
│   ├── marca/
│   ├── modelo/
│   ├── repuesto/
│   ├── tipoEquipo/
│   └── ...
├── dtos/                    # Data Transfer Objects por dominio
├── entities/                # Entidades JPA
├── enums/                   # Enumeraciones (estados, roles, tipos)
├── exception/               # Manejo global de excepciones
├── repositories/            # Interfaces JPA Repository
├── security/                # Filtros JWT, UserDetails, EntryPoint
├── services/                # Logica de negocio
│   └── impl/                # Implementaciones de servicios
└── utilities/               # Utilidades auxiliares
```

## Entidades principales

| Entidad | Descripcion |
|---|---|
| `Servicio` | Reparacion/servicio tecnico (entidad central) |
| `Presupuesto` | Presupuesto asociado a un servicio |
| `OrdenTrabajo` | Orden de trabajo para reparacion |
| `Cliente` | Datos del cliente |
| `Empleado` | Empleados del taller |
| `Equipo` | Equipos/dispositivos a reparar |
| `Usuario` | Usuarios del sistema con roles |
| `Notificacion` | Notificaciones del sistema |
| `Repuesto` | Repuestos/piezas utilizadas |
| `Marca` / `Modelo` | Catalogo de marcas y modelos |
| `TipoEquipo` | Tipos de equipos configurables |

## Estados del servicio (flujo principal)

```
RECIBIDO → PRESUPUESTADO → APROBADO → EN_REPARACION → TERMINADO → FINALIZADO
                         ↘ RECHAZADO
```

Estados adicionales para garantias: `ESPERANDO_EVALUACION_GARANTIA`, `GARANTIA_SIN_REPARACION`, `GARANTIA_RECHAZADA`

## Seguridad

### Autenticacion
- **JWT** con tokens de acceso (8 horas) y refresh tokens (7 dias)
- Filtro `JwtAuthenticationFilter` valida tokens en cada request
- Renovacion automatica via endpoint de refresh

### Autorizacion por roles
| Rol | Permisos |
|---|---|
| `PROPIETARIO` | Acceso total (admin) |
| `ADMINISTRATIVO` | Gestion de usuarios y operaciones |
| Otros roles | Acceso a funcionalidades operativas |

### Endpoints publicos (sin autenticacion)
- `/auth/login`, `/auth/refresh`, `/auth/logout`
- `/public/**`, `/api/public/**`
- Datos de catalogo (tipos, marcas, modelos)
- WebSocket: `/ws-servicios/**`

## Funcionalidades clave

- **WebSocket (STOMP)**: Notificaciones en tiempo real sobre cambios en servicios
- **Generacion de PDF**: Comprobantes y presupuestos con iTextPDF
- **Envio de emails**: Notificaciones por correo via Gmail SMTP
- **Swagger UI**: Documentacion interactiva en `/swagger-ui.html`
- **Actuator**: Health checks y metricas en `/actuator`
- **DataLoader**: Carga de datos iniciales al arrancar la app

## Requisitos previos

- **Java** 21 (JDK)
- **MySQL** 8.x
- **Maven** 3.9+

## Configuracion

La configuracion se encuentra en `src/main/resources/application.yml`:

| Propiedad | Valor por defecto |
|---|---|
| Puerto del servidor | `8080` |
| Base de datos | `localhost:3306/sigret_db` |
| JWT expiracion (access) | 8 horas |
| JWT expiracion (refresh) | 7 dias |
| SMTP | Gmail (`arroyo.service0@gmail.com`) |
| Swagger | Habilitado |

## Instalacion y ejecucion

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar la aplicacion
mvn spring-boot:run

# Ejecutar tests
mvn test

# Generar JAR ejecutable
mvn clean package
```

La API estara disponible en `http://localhost:8080/api`.

## Documentacion de la API

Con el servidor corriendo, acceder a:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
