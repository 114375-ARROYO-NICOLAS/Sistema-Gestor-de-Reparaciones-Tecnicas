import { Component, signal, computed, OnInit, OnDestroy, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd, RouterLink, RouterLinkActive } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DrawerModule } from 'primeng/drawer';
import { AvatarModule } from 'primeng/avatar';
import { BadgeModule } from 'primeng/badge';
import { MenuModule } from 'primeng/menu';
import { PopoverModule } from 'primeng/popover';
import { NotificationBellComponent } from '../notification-bell/notification-bell';
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

interface MenuGroup {
  label: string;
  icon: string;
  children: MenuLink[];
}

type SidebarItem = MenuLink | MenuGroup;

function isGroup(item: SidebarItem): item is MenuGroup {
  return 'children' in item;
}

@Component({
  selector: 'app-main-layout',
  changeDetection: ChangeDetectionStrategy.OnPush,
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
    PopoverModule,
    NotificationBellComponent
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

  // Estado de grupos expandidos
  protected readonly expandedGroups = signal<Set<string>>(new Set());

  // Menú dinámico según rol
  protected readonly menuItems = computed<SidebarItem[]>(() => {
    const rol = this.user()?.rol;
    const esPropietario = rol === 'PROPIETARIO';
    const esAdminOPropietario = rol === 'PROPIETARIO' || rol === 'ADMINISTRATIVO';

    const items: SidebarItem[] = [];

    if (esPropietario) {
      items.push({ label: 'Dashboard', icon: 'pi pi-chart-bar', routerLink: '/dashboard' });
    }

    const tableroChildren: MenuLink[] = [
      { label: 'Servicios', icon: 'pi pi-list', routerLink: '/servicios' },
      { label: 'Garantías', icon: 'pi pi-shield', routerLink: '/garantias' },
    ];
    if (esAdminOPropietario) {
      tableroChildren.push({ label: 'Presupuestos', icon: 'pi pi-dollar', routerLink: '/presupuestos' });
    }
    tableroChildren.push({ label: 'Órdenes de Trabajo', icon: 'pi pi-wrench', routerLink: '/ordenes-trabajo' });
    items.push({ label: 'Tableros', icon: 'pi pi-th-large', children: tableroChildren });

    const adminChildren: MenuLink[] = [
      { label: 'Clientes', icon: 'pi pi-users', routerLink: '/clientes' },
      { label: 'Equipos', icon: 'pi pi-desktop', routerLink: '/equipos' },
    ];
    if (esAdminOPropietario) {
      adminChildren.push({ label: 'Capital Humano', icon: 'pi pi-id-card', routerLink: '/empleados' });
      adminChildren.push({ label: 'Usuarios', icon: 'pi pi-user', routerLink: '/usuarios' });
    }
    items.push({ label: 'Administración', icon: 'pi pi-briefcase', children: adminChildren });

    if (esPropietario) {
      items.push({
        label: 'Configuración',
        icon: 'pi pi-cog',
        children: [
          { label: 'Tipos de Equipo', icon: 'pi pi-tag', routerLink: '/configuracion/tipos-equipo' },
          { label: 'Marcas', icon: 'pi pi-bookmark', routerLink: '/configuracion/marcas' },
          { label: 'Modelos', icon: 'pi pi-database', routerLink: '/configuracion/modelos' },
          { label: 'Repuestos', icon: 'pi pi-box', routerLink: '/configuracion/repuestos' }
        ]
      });
    }

    items.push({ label: 'Ayuda', icon: 'pi pi-question-circle', routerLink: '/ayuda' });

    return items;
  });

  protected readonly isGroup = isGroup;

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
        this.expandGroupForRoute(url);
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
    for (const item of this.menuItems()) {
      if (isGroup(item)) {
        const child = item.children.find(c => c.routerLink === url);
        if (child) return child.label;
      } else if (item.routerLink === url) {
        return item.label;
      }
    }
    return null;
  }

  private formatRouteName(route: string): string {
    const routeNames: { [key: string]: string } = {
      'dashboard': 'Dashboard',
      'servicios': 'Servicios',
      'garantias': 'Garantías',
      'clientes': 'Clientes',
      'empleados': 'Capital Humano',
      'equipos': 'Equipos',
      'usuarios': 'Usuarios',
      'configuracion': 'Configuración',
      'tipos-equipo': 'Tipos de Equipo',
      'marcas': 'Marcas',
      'modelos': 'Modelos',
      'repuestos': 'Repuestos',
      'profile': 'Perfil',
      'ayuda': 'Ayuda'
    };

    return routeNames[route] || route.charAt(0).toUpperCase() + route.slice(1);
  }

  protected toggleGroup(label: string): void {
    this.expandedGroups.update(groups => {
      const next = new Set(groups);
      if (next.has(label)) {
        next.delete(label);
      } else {
        next.add(label);
      }
      return next;
    });
  }

  protected isGroupExpanded(label: string): boolean {
    return this.expandedGroups().has(label);
  }

  private expandGroupForRoute(url: string): void {
    for (const item of this.menuItems()) {
      if (isGroup(item) && item.children.some(c => url.startsWith(c.routerLink))) {
        this.expandedGroups.update(groups => {
          const next = new Set(groups);
          next.add(item.label);
          return next;
        });
      }
    }
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