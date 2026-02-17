# SIGRET - Frontend

Aplicacion web progresiva (PWA) desarrollada con Angular para la gestion integral de reparaciones tecnicas.

## Tecnologias principales

| Tecnologia | Version | Descripcion |
|---|---|---|
| Angular | 20.x | Framework principal |
| TypeScript | 5.9 | Lenguaje de programacion |
| PrimeNG | 20.x | Libreria de componentes UI |
| PrimeFlex | 4.x | Utilidades CSS (flexbox/grid) |
| PrimeIcons | 7.x | Iconografia |
| RxJS | 7.8 | Programacion reactiva |
| SCSS | - | Preprocesador CSS |

## Dependencias destacadas

| Libreria | Version | Uso |
|---|---|---|
| `@angular/service-worker` | 20.x | Soporte PWA (cache, offline) |
| `@stomp/stompjs` | 7.x | Cliente WebSocket (notificaciones en tiempo real) |
| `sockjs-client` | 1.6 | Fallback para WebSocket |
| `signature_pad` | 5.0 | Captura de firmas digitales en canvas |
| `chart.js` | 4.5 | Graficos y estadisticas del dashboard |
| `@googlemaps/js-api-loader` | 2.x | Integracion con Google Maps |
| `@primeng/themes` | 20.x | Sistema de temas personalizables |

## Dependencias de desarrollo

| Libreria | Uso |
|---|---|
| `@angular/cli` | Herramientas de desarrollo y build |
| `karma` + `jasmine` | Testing unitario |
| `@types/jasmine` | Tipado para tests |
| `@types/sockjs-client` | Tipado para WebSocket |

## Arquitectura

```
src/
├── app/
│   ├── guards/              # Guards de autenticacion (auth, login)
│   ├── interceptors/        # Interceptores HTTP (JWT, sesion)
│   ├── models/              # Interfaces y DTOs tipados
│   ├── services/            # Servicios inyectables (20+)
│   ├── pages/               # Componentes de pagina por feature
│   │   ├── dashboard/
│   │   ├── clientes/
│   │   ├── empleados/
│   │   ├── equipos/
│   │   ├── servicios/
│   │   ├── garantias/
│   │   ├── presupuestos/
│   │   ├── ordenes-trabajo/
│   │   ├── configuracion/
│   │   └── ...
│   ├── app.routes.ts        # Rutas con lazy loading
│   └── app.config.ts        # Configuracion de la app
├── environments/            # Variables de entorno
├── assets/                  # Recursos estaticos
└── styles.scss              # Estilos globales
```

## Patrones y decisiones tecnicas

- **Standalone components**: Sin NgModules, todos los componentes son standalone
- **Signals**: Estado local gestionado con Angular Signals y `computed()`
- **Lazy loading**: Todas las rutas cargan componentes de forma diferida via `loadComponent`
- **OnPush**: Estrategia de deteccion de cambios optimizada
- **Control flow nativo**: `@if`, `@for`, `@switch` en vez de directivas estructurales
- **Formularios reactivos**: Se utiliza `ReactiveFormsModule` en todos los formularios
- **Inyeccion con `inject()`**: Se prefiere `inject()` sobre inyeccion por constructor

## Funcionalidades PWA

- Service Worker habilitado en produccion (`ngsw-config.json`)
- Estrategia de cache: **prefetch** para app shell, **lazy** para assets
- Soporte offline para recursos criticos
- Manifest para instalacion como app nativa

## Servicios principales

| Servicio | Responsabilidad |
|---|---|
| `AuthService` | Login, logout, gestion de tokens JWT |
| `ServicioService` | CRUD y gestion de estados de reparaciones |
| `PresupuestoService` | Gestion de presupuestos |
| `OrdenTrabajoService` | Gestion de ordenes de trabajo |
| `WebSocketService` | Notificaciones en tiempo real via STOMP |
| `DashboardService` | Agregacion de datos para metricas |
| `ThemeService` | Tema claro/oscuro |
| `SecureStorageService` | Almacenamiento local seguro |
| `TokenRefreshService` | Renovacion automatica de JWT |

## Seguridad

- **AuthGuard**: Protege rutas autenticadas
- **AuthInterceptor**: Inyecta token JWT en cada request
- **SessionExpiryInterceptor**: Maneja expiracion de sesion
- Rutas publicas: `/p/:token/:accion` (vista publica de presupuestos)

## Tema personalizado

- Basado en el preset **Lara** de PrimeNG
- Colores corporativos: Azul (`#2171a5`) y Bordo (`#7b1a2d`)
- Soporte para modo oscuro (`.app-dark`)

## Requisitos previos

- **Node.js** >= 18
- **npm** >= 9

## Instalacion y ejecucion

```bash
# Instalar dependencias
npm install

# Servidor de desarrollo (http://localhost:4200)
ng serve

# Build de produccion
ng build

# Build de desarrollo
ng build --configuration=development

# Tests unitarios
ng test
```

## Variables de entorno

El archivo `src/environments/environment.ts` contiene:

- `apiUrl`: URL base del backend (default: `http://localhost:8080/api`)
- `googleMapsApiKey`: Clave de API de Google Maps
