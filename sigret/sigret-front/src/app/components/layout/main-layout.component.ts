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

interface MenuSection {
  label: string;
  icon: string;
  items?: MenuItemWithChildren[];
}

interface MenuItemWithChildren {
  label: string;
  icon: string;
  routerLink?: string;
  badge?: string;
  children?: MenuItemWithChildren[];
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
  protected readonly openSubmenus = signal<Set<string>>(new Set());
  protected isSidebarVisible = signal(false);

  // Computed properties
  protected readonly user = computed(() => this.authService.user());
  protected readonly isAuthenticated = computed(() => this.authService.isAuthenticated());
  protected readonly isDarkMode = computed(() => this.themeService.isDarkMode());

  // Subscriptions
  private routerSubscription?: Subscription;

  // Configuración del menú - Solo opciones implementadas
  protected readonly menuItems: MenuSection[] = [
    {
      label: 'Principal',
      icon: 'pi pi-home',
      items: [
        {
          label: 'Dashboard',
          icon: 'pi pi-chart-bar',
          routerLink: '/dashboard'
        }
      ]
    },
    {
      label: 'Operaciones',
      icon: 'pi pi-wrench',
      items: [
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
          label: 'Buscar Servicios',
          icon: 'pi pi-search',
          routerLink: '/servicios/buscar'
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
        }
      ]
    },
    {
      label: 'Gestión',
      icon: 'pi pi-cog',
      items: [
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
        }
      ]
    },
    {
      label: 'Configuración',
      icon: 'pi pi-sliders-h',
      items: [
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
      ]
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
    for (const section of this.menuItems) {
      if (section.items) {
        for (const item of section.items) {
          if (item.routerLink === url) {
            return item.label;
          }
          if (item.children) {
            for (const child of item.children) {
              if (child.routerLink === url) {
                return `${item.label} > ${child.label}`;
              }
            }
          }
        }
      }
    }
    return null;
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

  protected toggleSubmenu(label: string): void {
    const openSubmenus = this.openSubmenus();
    const newSet = new Set(openSubmenus);
    
    if (newSet.has(label)) {
      newSet.delete(label);
    } else {
      newSet.add(label);
    }
    
    this.openSubmenus.set(newSet);
  }

  protected isSubmenuOpen(label: string): boolean {
    return this.openSubmenus().has(label);
  }

  protected onMenuItemClick(): void {
    // Close sidebar when clicking a menu item (handled by dismissable=true and navigation)
    // No need for manual logic here
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