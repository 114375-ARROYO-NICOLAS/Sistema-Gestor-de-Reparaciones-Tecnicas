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
        path: 'reparaciones',
        loadComponent: () => import('./components/placeholder/placeholder.component').then(m => m.PlaceholderComponent)
      },
      {
        path: 'equipos',
        loadComponent: () => import('./components/placeholder/placeholder.component').then(m => m.PlaceholderComponent)
      },
      {
        path: 'mensajes',
        loadComponent: () => import('./components/placeholder/placeholder.component').then(m => m.PlaceholderComponent)
      },
      {
        path: 'calendario',
        loadComponent: () => import('./components/placeholder/placeholder.component').then(m => m.PlaceholderComponent)
      },
      {
        path: 'clientes',
        loadComponent: () => import('./components/placeholder/placeholder.component').then(m => m.PlaceholderComponent)
      },
      {
        path: 'tecnicos',
        loadComponent: () => import('./components/placeholder/placeholder.component').then(m => m.PlaceholderComponent)
      },
      {
        path: 'inventario',
        loadComponent: () => import('./components/placeholder/placeholder.component').then(m => m.PlaceholderComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./components/placeholder/placeholder.component').then(m => m.PlaceholderComponent)
      },
      {
        path: 'settings',
        loadComponent: () => import('./components/placeholder/placeholder.component').then(m => m.PlaceholderComponent)
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./components/user-management/user-management.component').then(m => m.UserManagementComponent)
      },
      {
        path: 'empleados',
        loadComponent: () => import('./components/employee-management/employee-management.component').then(m => m.EmployeeManagementComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
