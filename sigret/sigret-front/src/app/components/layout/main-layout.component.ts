import { Component, signal, computed, effect, HostListener, OnInit } from '@angular/core';
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
export class MainLayoutComponent implements OnInit {
  // Estados reactivos
  protected readonly sidebarExpanded = signal(false);
  protected readonly currentRoute = signal('Dashboard');
  protected readonly screenWidth = signal(window.innerWidth);
  protected readonly openSubmenus = signal<Set<string>>(new Set());
  protected readonly notificationCount = signal(3);
  
  // Computed properties
  protected readonly user = computed(() => this.authService.user());
  protected readonly isAuthenticated = computed(() => this.authService.isAuthenticated());
  protected readonly isDarkMode = computed(() => this.themeService.isDarkMode());
  protected readonly isMobile = computed(() => this.screenWidth() < 768);

  // Configuración del menú actualizada con soporte para submenús
  protected readonly menuItems: MenuSection[] = [
    {
      label: 'Principal',
      icon: 'pi pi-home',
      items: [
        {
          label: 'Dashboard',
          icon: 'pi pi-chart-bar',
          routerLink: '/dashboard'
        },
        {
          label: 'Reparaciones',
          icon: 'pi pi-wrench',
          routerLink: '/reparaciones',
          badge: '5'
        },
        {
          label: 'Equipos',
          icon: 'pi pi-desktop',
          routerLink: '/equipos'
        },
        {
          label: 'Mensajes',
          icon: 'pi pi-envelope',
          badge: '2',
          routerLink: '/mensajes'
        },
        {
          label: 'Calendario',
          icon: 'pi pi-calendar',
          routerLink: '/calendario'
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
          children: [
            {
              label: 'Lista de Clientes',
              icon: 'pi pi-list',
              routerLink: '/clientes/lista'
            },
            {
              label: 'Nuevo Cliente',
              icon: 'pi pi-user-plus',
              routerLink: '/clientes/nuevo'
            },
            {
              label: 'Historial',
              icon: 'pi pi-history',
              routerLink: '/clientes/historial'
            }
          ]
        },
        {
          label: 'Técnicos',
          icon: 'pi pi-id-card',
          children: [
            {
              label: 'Lista de Técnicos',
              icon: 'pi pi-list',
              routerLink: '/tecnicos/lista'
            },
            {
              label: 'Nuevo Técnico',
              icon: 'pi pi-user-plus',
              routerLink: '/tecnicos/nuevo'
            },
            {
              label: 'Horarios',
              icon: 'pi pi-clock',
              routerLink: '/tecnicos/horarios'
            }
          ]
        },
        {
          label: 'Empleados',
          icon: 'pi pi-users',
          routerLink: '/empleados'
        },
        {
          label: 'Usuarios',
          icon: 'pi pi-user-check',
          routerLink: '/usuarios'
        },
        {
          label: 'Inventario',
          icon: 'pi pi-box',
          routerLink: '/inventario'
        }
      ]
    },
    {
      label: 'Reportes',
      icon: 'pi pi-file-text',
      items: [
        {
          label: 'Reportes Generales',
          icon: 'pi pi-file',
          routerLink: '/reportes/generales'
        },
        {
          label: 'Estadísticas',
          icon: 'pi pi-chart-line',
          routerLink: '/reportes/estadisticas'
        },
        {
          label: 'Exportar',
          icon: 'pi pi-download',
          routerLink: '/reportes/exportar'
        }
      ]
    },
    {
      label: 'Sistema',
      icon: 'pi pi-cog',
      items: [
        {
          label: 'Configuración',
          icon: 'pi pi-sliders-h',
          children: [
            {
              label: 'General',
              icon: 'pi pi-cog',
              routerLink: '/config/general'
            },
            {
              label: 'Usuarios',
              icon: 'pi pi-users',
              routerLink: '/config/usuarios'
            },
            {
              label: 'Seguridad',
              icon: 'pi pi-shield',
              routerLink: '/config/seguridad'
            }
          ]
        },
        {
          label: 'Logs',
          icon: 'pi pi-list',
          routerLink: '/sistema/logs',
          badge: '24'
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
      label: 'Configuraciones',
      icon: 'pi pi-cog',
      command: () => this.goToSettings()
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

  constructor(
    private authService: AuthService,
    private themeService: ThemeService,
    private router: Router
  ) {
    this.initializeRouteTracking();
  }

  ngOnInit(): void {
    this.checkInitialScreenSize();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any): void {
    this.screenWidth.set(event.target.innerWidth);
    
    // Cerrar sidebar automáticamente en móvil cuando se redimensiona
    if (this.isMobile() && this.sidebarExpanded()) {
      this.sidebarExpanded.set(false);
    }
  }

  private initializeRouteTracking(): void {
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        map((event: NavigationEnd) => event.urlAfterRedirects)
      )
      .subscribe(url => {
        this.updateCurrentRoute(url);
      });
  }

  private checkInitialScreenSize(): void {
    this.screenWidth.set(window.innerWidth);
    if (!this.isMobile()) {
      this.sidebarExpanded.set(false); // Empezar con sidebar cerrado por defecto
    }
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
      'reparaciones': 'Reparaciones',
      'equipos': 'Equipos',
      'mensajes': 'Mensajes',
      'calendario': 'Calendario',
      'clientes': 'Clientes',
      'tecnicos': 'Técnicos',
      'inventario': 'Inventario',
      'reportes': 'Reportes',
      'profile': 'Perfil',
      'settings': 'Configuración',
      'config': 'Configuración',
      'sistema': 'Sistema'
    };
    
    return routeNames[route] || route.charAt(0).toUpperCase() + route.slice(1);
  }

  protected getBreadcrumb(): string {
    return this.currentRoute();
  }

  protected toggleSidebar(): void {
    this.sidebarExpanded.update(current => !current);
  }

  protected closeSidebar(): void {
    this.sidebarExpanded.set(false);
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
    // Cerrar sidebar en móvil cuando se hace clic en un elemento del menú
    if (this.isMobile()) {
      this.closeSidebar();
    }
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
        // Incluso si falla, redirigir al login
        this.router.navigate(['/login']);
      }
    });
  }

  private goToProfile(): void {
    this.router.navigate(['/profile']);
  }

  private goToSettings(): void {
    this.router.navigate(['/settings']);
  }

  // Método mantenido para compatibilidad con tu template actual
  protected onSidebarHide(): void {
    if (this.isMobile()) {
      this.sidebarExpanded.set(false);
    }
  }
}