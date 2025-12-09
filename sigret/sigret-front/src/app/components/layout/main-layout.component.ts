import { Component, signal, computed, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd, RouterLink, RouterLinkActive } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DrawerModule } from 'primeng/drawer';
import { AvatarModule } from 'primeng/avatar';
import { BadgeModule } from 'primeng/badge';
import { MenuModule } from 'primeng/menu';
import { PopoverModule } from 'primeng/popover';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { MenuItem } from 'primeng/api';
import { filter, map } from 'rxjs/operators';
import { Subscription } from 'rxjs';

interface MenuLink {
  label: string;
  icon: string;
  routerLink: string;
  badge?: string;
}

@Component({
  selector: 'app-main-layout',
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    ButtonModule,
    DrawerModule,
    AvatarModule,
    BadgeModule,
    MenuModule,
    PopoverModule
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  // Services
  private readonly authService = inject(AuthService);
  private readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);

  // Reactive state
  protected readonly currentRoute = signal('Dashboard');
  protected isSidebarVisible = signal(false);

  // Computed properties
  protected readonly user = computed(() => this.authService.user());
  protected readonly isAuthenticated = computed(() => this.authService.isAuthenticated());
  protected readonly isDarkMode = computed(() => this.themeService.isDarkMode());

  // Subscriptions
  private routerSubscription?: Subscription;

  // Configuración del menú - Lista plana de opciones
  protected readonly menuItems: MenuLink[] = [
    {
      label: 'Dashboard',
      icon: 'pi pi-chart-bar',
      routerLink: '/dashboard'
    },
    {
      label: 'Servicios',
      icon: 'pi pi-list',
      routerLink: '/servicios'
    },
    {
      label: 'Garantías',
      icon: 'pi pi-shield',
      routerLink: '/garantias'
    },
    {
      label: 'Presupuestos',
      icon: 'pi pi-dollar',
      routerLink: '/presupuestos'
    },
    {
      label: 'Órdenes de Trabajo',
      icon: 'pi pi-wrench',
      routerLink: '/ordenes-trabajo'
    },
    {
      label: 'Clientes',
      icon: 'pi pi-users',
      routerLink: '/clientes'
    },
    {
      label: 'Empleados',
      icon: 'pi pi-id-card',
      routerLink: '/empleados'
    },
    {
      label: 'Equipos',
      icon: 'pi pi-desktop',
      routerLink: '/equipos'
    },
    {
      label: 'Usuarios',
      icon: 'pi pi-user',
      routerLink: '/usuarios'
    },
    {
      label: 'Tipos de Equipo',
      icon: 'pi pi-tag',
      routerLink: '/configuracion/tipos-equipo'
    },
    {
      label: 'Marcas',
      icon: 'pi pi-bookmark',
      routerLink: '/configuracion/marcas'
    },
    {
      label: 'Modelos',
      icon: 'pi pi-database',
      routerLink: '/configuracion/modelos'
    }
  ];

  protected readonly userMenuItems: MenuItem[] = [
    {
      label: 'Mi Perfil',
      icon: 'pi pi-user',
      command: () => this.goToProfile()
    },
    {
      separator: true
    },
    {
      label: 'Cerrar Sesión',
      icon: 'pi pi-sign-out',
      command: () => this.logout()
    }
  ];

  constructor() {
    this.initializeRouteTracking();
  }

  ngOnInit(): void {
    // Component initialization
  }

  ngOnDestroy(): void {
    this.routerSubscription?.unsubscribe();
  }

  private initializeRouteTracking(): void {
    this.routerSubscription = this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        map((event: NavigationEnd) => event.urlAfterRedirects)
      )
      .subscribe(url => {
        this.updateCurrentRoute(url);
        // Auto-close drawer on navigation (works for all screen sizes)
        this.isSidebarVisible.set(false);
      });
  }

  private updateCurrentRoute(url: string): void {
    const segments = url.split('/').filter(segment => segment);
    if (segments.length === 0) {
      this.currentRoute.set('Dashboard');
      return;
    }

    // Buscar la ruta en el menú para obtener el nombre legible
    const routeName = this.findRouteLabel(url) || this.formatRouteName(segments[segments.length - 1]);
    this.currentRoute.set(routeName);
  }

  private findRouteLabel(url: string): string | null {
    const menuItem = this.menuItems.find(item => item.routerLink === url);
    return menuItem ? menuItem.label : null;
  }

  private formatRouteName(route: string): string {
    const routeNames: { [key: string]: string } = {
      'dashboard': 'Dashboard',
      'servicios': 'Servicios',
      'clientes': 'Clientes',
      'empleados': 'Empleados',
      'equipos': 'Equipos',
      'usuarios': 'Usuarios',
      'configuracion': 'Configuración',
      'tipos-equipo': 'Tipos de Equipo',
      'marcas': 'Marcas',
      'modelos': 'Modelos',
      'profile': 'Perfil'
    };

    return routeNames[route] || route.charAt(0).toUpperCase() + route.slice(1);
  }

  protected onMenuToggle(): void {
    this.isSidebarVisible.update(visible => !visible);
  }

  protected toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  protected logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: () => {
        // Even if it fails, redirect to login
        this.router.navigate(['/login']);
      }
    });
  }

  private goToProfile(): void {
    this.router.navigate(['/profile']);
  }
}