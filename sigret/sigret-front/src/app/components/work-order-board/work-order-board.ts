import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { CdkDragDrop, CdkDrag, CdkDropList, CdkDropListGroup, transferArrayItem } from '@angular/cdk/drag-drop';
import { OrdenTrabajoService } from '../../services/orden-trabajo.service';
import { WebSocketService } from '../../services/websocket.service';
import { OrdenTrabajo, EstadoOrdenTrabajo, OrdenTrabajoEvent } from '../../models/orden-trabajo.model';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BadgeModule } from 'primeng/badge';
import { CardModule } from 'primeng/card';
import { Tag } from 'primeng/tag';

interface Column {
  name: string;
  state: EstadoOrdenTrabajo;
  color: string;
  orders: OrdenTrabajo[];
}

@Component({
  selector: 'app-work-order-board',
  imports: [
    CommonModule,
    ButtonModule,
    ProgressSpinnerModule,
    BadgeModule,
    CardModule,
    Tag,
    CdkDrag,
    CdkDropList,
    CdkDropListGroup
  ],
  templateUrl: './work-order-board.html',
  styleUrls: ['./work-order-board.scss']
})
export class WorkOrderBoardComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly orderService = inject(OrdenTrabajoService);
  private readonly wsService = inject(WebSocketService);
  private readonly messageService = inject(MessageService);
  private readonly location = inject(Location);

  private wsSubscription?: Subscription;

  // Signals
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly isConnected = computed(() => this.wsService.isConnected());

  // Columnas del tablero Kanban
  columns: Column[] = [
    {
      name: 'Pendiente',
      state: EstadoOrdenTrabajo.PENDIENTE,
      color: '#ffc107',
      orders: []
    },
    {
      name: 'En Progreso',
      state: EstadoOrdenTrabajo.EN_PROGRESO,
      color: '#007bff',
      orders: []
    },
    {
      name: 'Terminada',
      state: EstadoOrdenTrabajo.TERMINADA,
      color: '#28a745',
      orders: []
    },
    {
      name: 'Cancelada',
      state: EstadoOrdenTrabajo.CANCELADA,
      color: '#dc3545',
      orders: []
    }
  ];

  goBack(): void {
    this.location.back();
  }

  ngOnInit(): void {
    this.loadOrders();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    this.wsSubscription?.unsubscribe();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.error.set(null);

    this.orderService.obtenerOrdenesTrabajo().subscribe({
      next: (response) => {
        const orders = response.content || response;
        this.organizeOrdersByState(orders);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading work orders:', err);
        this.error.set('Error al cargar las órdenes de trabajo. Por favor, intente nuevamente.');
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las órdenes de trabajo'
        });
      }
    });
  }

  private organizeOrdersByState(orders: OrdenTrabajo[]): void {
    // Limpiar columnas
    this.columns.forEach(col => col.orders = []);

    // Distribuir órdenes por estado
    orders.forEach(order => {
      const column = this.columns.find(col => col.state === order.estado);
      if (column) {
        column.orders.push(order);
      }
    });

    // Ordenar órdenes por fecha de creación (más recientes primero)
    this.columns.forEach(col => {
      col.orders.sort((a, b) => {
        return new Date(b.fechaCreacion).getTime() - new Date(a.fechaCreacion).getTime();
      });
    });
  }

  private connectWebSocket(): void {
    this.wsService.connect();

    this.wsSubscription = this.wsService.ordenTrabajoEvent$.subscribe({
      next: (event: OrdenTrabajoEvent | null) => {
        if (event) {
          this.handleWebSocketEvent(event);
        }
      },
      error: (err) => {
        console.error('WebSocket error:', err);
        this.messageService.add({
          severity: 'warn',
          summary: 'Conexión WebSocket',
          detail: 'Error en la conexión de tiempo real'
        });
      }
    });
  }

  private handleWebSocketEvent(event: OrdenTrabajoEvent): void {
    console.log('Handling WebSocket event:', event);

    switch (event.tipoEvento) {
      case 'CREADO':
        if (event.ordenTrabajo) {
          this.addOrder(event.ordenTrabajo);
          this.messageService.add({
            severity: 'info',
            summary: 'Nueva Orden de Trabajo',
            detail: `Orden para servicio ${event.numeroServicio} creada`
          });
        }
        break;

      case 'ACTUALIZADO':
        if (event.ordenTrabajo) {
          this.updateOrder(event.ordenTrabajo);
        }
        break;

      case 'CAMBIO_ESTADO':
        if (event.ordenTrabajo && event.estadoAnterior) {
          this.moveOrder(event.ordenTrabajo, event.estadoAnterior);
          this.messageService.add({
            severity: 'success',
            summary: 'Estado Actualizado',
            detail: `Orden de trabajo de servicio ${event.numeroServicio} cambió de estado`
          });
        }
        break;

      case 'ELIMINADO':
        this.deleteOrder(event.ordenTrabajoId);
        this.messageService.add({
          severity: 'info',
          summary: 'Orden Eliminada',
          detail: `Orden de trabajo eliminada`
        });
        break;
    }
  }

  private addOrder(order: OrdenTrabajo): void {
    const column = this.columns.find(col => col.state === order.estado);
    if (column) {
      const exists = column.orders.some(o => o.id === order.id);
      if (!exists) {
        column.orders.unshift(order);
      }
    }
  }

  private updateOrder(order: OrdenTrabajo): void {
    const column = this.columns.find(col => col.state === order.estado);
    if (column) {
      const index = column.orders.findIndex(o => o.id === order.id);
      if (index !== -1) {
        column.orders[index] = order;
      }
    }
  }

  private moveOrder(order: OrdenTrabajo, previousState: EstadoOrdenTrabajo): void {
    // Remove from previous state
    const previousColumn = this.columns.find(col => col.state === previousState);
    if (previousColumn) {
      previousColumn.orders = previousColumn.orders.filter(o => o.id !== order.id);
    }

    // Add to new state
    const newColumn = this.columns.find(col => col.state === order.estado);
    if (newColumn) {
      const exists = newColumn.orders.some(o => o.id === order.id);
      if (!exists) {
        newColumn.orders.unshift(order);
      }
    }
  }

  private deleteOrder(orderId: number): void {
    this.columns.forEach(column => {
      column.orders = column.orders.filter(o => o.id !== orderId);
    });
  }

  // Drag & Drop using Angular CDK
  dropOrder(event: CdkDragDrop<OrdenTrabajo[]>, targetColumn?: Column): void {
    const order = event.item.data as OrdenTrabajo;
    const previousState = order.estado;

    // Determine target state
    let targetState: EstadoOrdenTrabajo;

    if (targetColumn) {
      targetState = targetColumn.state;
    } else {
      return; // No valid target
    }

    // If state didn't change, just reorder within same column
    if (previousState === targetState) {
      return; // No need to update backend for reordering
    }

    // Move item between arrays
    if (event.previousContainer !== event.container) {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    }

    // Update state in backend using specialized endpoints when applicable
    let request$: Observable<any>;
    if (targetState === 'EN_PROGRESO') {
      request$ = this.orderService.iniciarOrdenTrabajo(order.id);
    } else if (targetState === 'TERMINADA') {
      request$ = this.orderService.finalizarOrdenTrabajo(order.id);
    } else {
      request$ = this.orderService.cambiarEstadoOrdenTrabajo(order.id, targetState);
    }

    request$.subscribe({
      next: () => {
        order.estado = targetState;
        this.messageService.add({
          severity: 'success',
          summary: 'Estado Actualizado',
          detail: `Orden movida a ${targetColumn.name}`
        });
      },
      error: (err) => {
        console.error('Error updating order state:', err);
        // Revert the move
        transferArrayItem(
          event.container.data,
          event.previousContainer.data,
          event.currentIndex,
          event.previousIndex
        );
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cambiar el estado de la orden'
        });
      }
    });
  }

  formatCurrency(amount: number | undefined): string {
    if (!amount) return 'N/A';
    return new Intl.NumberFormat('es-UY', {
      style: 'currency',
      currency: 'UYU'
    }).format(amount);
  }

  refresh(): void {
    this.loadOrders();
  }

  viewOrderDetail(orderId: number, event?: MouseEvent): void {
    // Prevent navigation if we're dragging
    if (event && (event.target as HTMLElement).closest('.cdk-drag-preview')) {
      return;
    }
    this.router.navigate(['/ordenes-trabajo', orderId]);
  }
}
