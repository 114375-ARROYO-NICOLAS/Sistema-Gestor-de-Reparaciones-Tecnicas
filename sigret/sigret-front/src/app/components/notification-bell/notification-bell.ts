import { Component, ChangeDetectionStrategy, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { PopoverModule } from 'primeng/popover';
import { Popover } from 'primeng/popover';
import { NotificacionService } from '../../services/notificacion.service';
import { Notificacion } from '../../models/notificacion.model';

@Component({
  selector: 'app-notification-bell',
  imports: [
    ButtonModule,
    BadgeModule,
    PopoverModule
  ],
  template: `
    <div class="relative">
      <p-button
        icon="pi pi-bell"
        [text]="true"
        [rounded]="true"
        (click)="onBellClick($event, notifPanel)"
        title="Notificaciones">
      </p-button>
      @if (unreadCount() > 0) {
        <p-badge
          [value]="unreadCount() > 99 ? '99+' : unreadCount().toString()"
          severity="danger"
          class="notification-badge">
        </p-badge>
      }
    </div>

    <p-popover #notifPanel styleClass="notification-popover">
      <div class="notification-panel">
        <!-- Header -->
        <div class="flex align-items-center justify-content-between px-3 py-2 border-bottom-1 surface-border">
          <span class="font-semibold text-lg">Notificaciones</span>
          @if (unreadCount() > 0) {
            <p-button
              label="Marcar todas"
              [text]="true"
              size="small"
              (click)="markAllAsRead()">
            </p-button>
          }
        </div>

        <!-- Notification List -->
        <div class="notification-list">
          @if (isLoading()) {
            <div class="p-4 text-center">
              <i class="pi pi-spin pi-spinner text-2xl text-primary"></i>
            </div>
          } @else if (notificaciones().length === 0) {
            <div class="p-4 text-center text-color-secondary">
              <i class="pi pi-inbox text-4xl mb-2" style="display: block"></i>
              <p class="m-0 text-sm">No hay notificaciones</p>
            </div>
          } @else {
            @for (notif of notificaciones(); track notif.id) {
              <div
                class="notification-item flex gap-3 p-3 cursor-pointer border-bottom-1 surface-border"
                [class.notification-unread]="!notif.leida"
                (click)="onNotificationClick(notif, notifPanel)">
                <div class="flex-shrink-0 flex align-items-center">
                  <i [class]="(notif.icono || 'pi pi-info-circle') + ' text-xl'"
                     [class.text-primary]="notif.severidad === 'info'"
                     [class.text-green-500]="notif.severidad === 'success'"
                     [class.text-orange-500]="notif.severidad === 'warn'"
                     [class.text-red-500]="notif.severidad === 'error'">
                  </i>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="m-0 text-sm white-space-normal"
                     [class.font-semibold]="!notif.leida">
                    {{ notif.mensaje }}
                  </p>
                  <span class="text-xs text-color-secondary">{{ getRelativeTime(notif.fechaCreacion) }}</span>
                </div>
                @if (!notif.leida) {
                  <div class="flex-shrink-0 flex align-items-center">
                    <span class="inline-block border-circle bg-primary" style="width: 0.5rem; height: 0.5rem"></span>
                  </div>
                }
              </div>
            }
          }
        </div>
      </div>
    </p-popover>
  `,
  styles: [`
    :host {
      display: contents;
    }

    .notification-badge {
      position: absolute;
      top: 0;
      right: 0;
      transform: translate(25%, -25%);
      pointer-events: none;
    }

    .notification-panel {
      width: 360px;
    }

    .notification-list {
      max-height: 400px;
      overflow-y: auto;
    }

    .notification-unread {
      background-color: color-mix(in srgb, var(--p-primary-color) 10%, var(--p-surface-0));
      border-left: 3px solid var(--p-primary-color);
    }

    .notification-item:not(.notification-unread) {
      border-left: 3px solid transparent;
    }

    .notification-item:hover {
      background-color: var(--p-surface-200);
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotificationBellComponent implements OnInit {
  private readonly notificacionService = inject(NotificacionService);
  private readonly router = inject(Router);

  protected readonly unreadCount = this.notificacionService.unreadCount;
  protected readonly notificaciones = this.notificacionService.notificaciones;
  protected readonly isLoading = this.notificacionService.isLoading;

  ngOnInit(): void {
    this.notificacionService.loadUnreadCount();
  }

  onBellClick(event: Event, panel: Popover): void {
    this.notificacionService.loadRecientes().subscribe();
    panel.toggle(event);
  }

  onNotificationClick(notif: Notificacion, panel: Popover): void {
    if (!notif.leida) {
      this.notificacionService.markAsRead(notif.id).subscribe();
    }

    const route = this.notificacionService.getRouteForNotification(notif);
    if (route) {
      this.router.navigateByUrl(route);
    }
    panel.hide();
  }

  markAllAsRead(): void {
    this.notificacionService.markAllAsRead().subscribe();
  }

  getRelativeTime(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return 'Ahora';
    if (diffMins < 60) return `Hace ${diffMins} min`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `Hace ${diffHours} h`;
    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 30) return `Hace ${diffDays} d`;
    return date.toLocaleDateString();
  }
}
