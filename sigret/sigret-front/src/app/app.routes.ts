import { Routes } from '@angular/router';
import { authGuard, loginGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent),
    canActivate: [loginGuard]
  },
  {
    path: '',
    loadComponent: () => import('./components/layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'clientes',
        children: [
          {
            path: '',
            loadComponent: () => import('./components/client-management/client-management.component').then(m => m.ClientManagementComponent)
          },
          {
            path: 'eliminados',
            loadComponent: () => import('./components/clientes-eliminados/clientes-eliminados').then(m => m.ClientesEliminadosComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./components/client-detail/client-detail.component').then(m => m.ClientDetailComponent)
          }
        ]
      },
      {
        path: 'empleados',
        children: [
          {
            path: '',
            loadComponent: () => import('./components/employee-management/employee-management.component').then(m => m.EmployeeManagementComponent)
          },
          {
            path: 'nuevo',
            loadComponent: () => import('./components/employee-create/employee-create').then(m => m.EmployeeCreateComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./components/employee-detail/employee-detail.component').then(m => m.EmployeeDetailComponent)
          }
        ]
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./components/user-management/user-management.component').then(m => m.UserManagementComponent)
      },
      {
        path: 'equipos',
        children: [
          {
            path: '',
            loadComponent: () => import('./components/equipo-management/equipo-management.component').then(m => m.EquipoManagementComponent)
          },
          {
            path: 'nuevo',
            loadComponent: () => import('./components/equipo-create/equipo-create.component').then(m => m.EquipoCreateComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./components/equipo-detail/equipo-detail.component').then(m => m.EquipoDetailComponent)
          },
          {
            path: ':id/editar',
            loadComponent: () => import('./components/equipo-create/equipo-create.component').then(m => m.EquipoCreateComponent)
          }
        ]
      },
      {
        path: 'servicios',
        children: [
          {
            path: '',
            loadComponent: () => import('./components/tablero-servicios/tablero-servicios.component').then(m => m.TableroServiciosComponent)
          },
          {
            path: 'buscar',
            loadComponent: () => import('./components/servicio-search/servicio-search').then(m => m.ServicioSearch)
          },
          {
            path: 'nuevo',
            loadComponent: () => import('./components/servicio-create/servicio-create').then(m => m.ServicioCreateComponent)
          },
          {
            path: 'eliminados',
            loadComponent: () => import('./components/servicios-eliminados/servicios-eliminados').then(m => m.ServiciosEliminadosComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./components/servicio-detail/servicio-detail').then(m => m.ServicioDetail)
          }
        ]
      },
      {
        path: 'garantias',
        loadComponent: () => import('./components/tablero-garantias/tablero-garantias').then(m => m.TableroGarantiasComponent)
      },
      {
        path: 'presupuestos',
        children: [
          {
            path: '',
            loadComponent: () => import('./components/budget-board/budget-board').then(m => m.BudgetBoardComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./components/presupuesto-detail/presupuesto-detail').then(m => m.PresupuestoDetail)
          }
        ]
      },
      {
        path: 'ordenes-trabajo',
        children: [
          {
            path: '',
            loadComponent: () => import('./components/work-order-board/work-order-board').then(m => m.WorkOrderBoardComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./components/orden-trabajo-detail/orden-trabajo-detail').then(m => m.OrdenTrabajoDetailComponent)
          }
        ]
      },
      {
        path: 'configuracion',
        children: [
          {
            path: 'tipos-equipo',
            loadComponent: () => import('./components/configuracion/tipo-equipo-config/tipo-equipo-config').then(m => m.TipoEquipoConfigComponent)
          },
          {
            path: 'marcas',
            loadComponent: () => import('./components/configuracion/marca-config/marca-config').then(m => m.MarcaConfigComponent)
          },
          {
            path: 'modelos',
            loadComponent: () => import('./components/configuracion/modelo-config/modelo-config').then(m => m.ModeloConfigComponent)
          },
          {
            path: 'repuestos',
            loadComponent: () => import('./components/configuracion/repuesto-config/repuesto-config').then(m => m.RepuestoConfigComponent)
          }
        ]
      },
      {
        path: 'profile',
        loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent)
      },
      {
        path: 'ayuda',
        loadComponent: () => import('./components/faq/faq').then(m => m.FaqComponent)
      }
    ]
  },
  {
    path: 'p/:token/:accion',
    loadComponent: () => import('./components/presupuesto-publico/presupuesto-publico.component').then(m => m.PresupuestoPublicoComponent)
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
