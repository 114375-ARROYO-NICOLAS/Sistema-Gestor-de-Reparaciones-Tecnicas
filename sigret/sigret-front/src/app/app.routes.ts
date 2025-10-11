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
        path: 'profile',
        loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
