# SiGReT - Sistema Gestor de Reparaciones Tecnicas

Sistema web integral para la gestion de servicios de reparacion tecnica, pensado para talleres y empresas de servicio tecnico que necesitan organizar, seguir y controlar todo el ciclo de vida de una reparacion.

## Que problema resuelve

Los talleres de reparacion tecnica manejan diariamente multiples equipos, clientes, presupuestos y ordenes de trabajo. Sin un sistema centralizado, es comun perder el seguimiento de reparaciones, demorar la comunicacion con el cliente o no tener visibilidad sobre el estado general del negocio.

**SiGReT** digitaliza y ordena todo este proceso, desde que el cliente ingresa un equipo hasta que lo retira reparado.

## Funcionalidades principales

### Gestion de servicios
- Registro de ingreso de equipos con captura de **firma digital** del cliente
- Seguimiento del estado de cada reparacion en tiempo real (recibido, presupuestado, aprobado, en reparacion, terminado, finalizado)
- **Tablero visual** (estilo Kanban) para ver el estado de todos los servicios de un vistazo
- Busqueda y filtrado avanzado de servicios

### Presupuestos
- Creacion de presupuestos detallados con repuestos y mano de obra
- **Enlace publico** para que el cliente apruebe o rechace el presupuesto sin necesidad de cuenta
- Generacion de presupuestos en formato **PDF**

### Ordenes de trabajo
- Creacion de ordenes de trabajo vinculadas a servicios aprobados
- Detalle de tareas, repuestos utilizados y tiempos
- Tablero visual para seguimiento

### Garantias
- Gestion de reclamos de garantia sobre servicios finalizados
- Flujo de evaluacion y resolucion de garantias

### Clientes y equipos
- Base de datos de clientes con historial completo de servicios
- Registro de equipos con marca, modelo y tipo
- Vinculacion cliente-equipo para trazabilidad

### Empleados y usuarios
- Gestion de empleados del taller
- Sistema de usuarios con **roles y permisos** (propietario, administrativo, tecnico)
- Control de acceso segun el rol del usuario

### Dashboard
- Panel de metricas y estadisticas del negocio
- Graficos sobre estados de servicios, volumenes de trabajo e indicadores clave

### Notificaciones
- **Notificaciones en tiempo real** dentro de la aplicacion
- Notificaciones por **correo electronico** al cliente

### Configuracion
- Catalogo de tipos de equipo, marcas, modelos y repuestos
- Personalizacion del sistema segun las necesidades del taller

## Caracteristicas tecnicas destacadas

- **Aplicacion web progresiva (PWA)**: Se puede instalar en el dispositivo y funcionar offline
- **Responsive**: Se adapta a celulares, tablets y computadoras de escritorio
- **Tiempo real**: Los cambios se reflejan instantaneamente para todos los usuarios conectados
- **Firma digital**: Captura de firma del cliente en pantalla tactil o con mouse
- **Generacion de PDF**: Comprobantes y presupuestos listos para imprimir o enviar
- **Tema claro/oscuro**: Interfaz adaptable a la preferencia del usuario

## Estructura del proyecto

```
Sistema-Gestor-de-Reparaciones-Tecnicas/
└── sigret/
    ├── sigret-front/       # Aplicacion web (Angular + PrimeNG)
    └── sigret-backend/     # API REST (Spring Boot + MySQL)
```

Consultar el README de cada proyecto para detalles tecnicos, dependencias y configuracion:

- [README del Frontend](sigret/sigret-front/README.md)
- [README del Backend](sigret/sigret-backend/README.md)

## Requisitos para ejecutar

| Componente | Requisito |
|---|---|
| Backend | Java 21, MySQL 8.x, Maven 3.9+ |
| Frontend | Node.js 18+, npm 9+ |

## Inicio rapido

```bash
# 1. Base de datos
# Crear la base de datos 'sigret_db' en MySQL

# 2. Backend
cd sigret/sigret-backend
mvn spring-boot:run
# API disponible en http://localhost:8080

# 3. Frontend
cd sigret/sigret-front
npm install
ng serve
# App disponible en http://localhost:4200
```
