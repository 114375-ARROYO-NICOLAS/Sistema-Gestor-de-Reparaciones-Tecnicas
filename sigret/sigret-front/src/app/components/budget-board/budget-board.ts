import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CdkDragDrop, CdkDrag, CdkDropList, CdkDropListGroup, transferArrayItem } from '@angular/cdk/drag-drop';
import { PresupuestoService } from '../../services/presupuesto.service';
import { WebSocketService } from '../../services/websocket.service';
import { Presupuesto, EstadoPresupuesto, PresupuestoEvent } from '../../models/presupuesto.model';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BadgeModule } from 'primeng/badge';
import { CardModule } from 'primeng/card';
import { Tag } from 'primeng/tag';

interface Column {
  name: string;
  state: EstadoPresupuesto;
  color: string;
  budgets: Presupuesto[];
}

@Component({
  selector: 'app-budget-board',
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
  templateUrl: './budget-board.html',
  styleUrls: ['./budget-board.scss']
})
export class BudgetBoardComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly budgetService = inject(PresupuestoService);
  private readonly wsService = inject(WebSocketService);
  private readonly messageService = inject(MessageService);

  private wsSubscription?: Subscription;

  // Signals
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly isConnected = computed(() => this.wsService.isConnected());

  // Columnas del tablero Kanban
  columns: Column[] = [
    {
      name: 'Pendiente',
      state: EstadoPresupuesto.PENDIENTE,
      color: '#ffc107',
      budgets: []
    },
    {
      name: 'En Curso',
      state: EstadoPresupuesto.EN_CURSO,
      color: '#17a2b8',
      budgets: []
    },
    {
      name: 'Listo',
      state: EstadoPresupuesto.LISTO,
      color: '#6f42c1',
      budgets: []
    },
    {
      name: 'Enviado',
      state: EstadoPresupuesto.ENVIADO,
      color: '#fd7e14',
      budgets: []
    },
    {
      name: 'Aprobado',
      state: EstadoPresupuesto.APROBADO,
      color: '#28a745',
      budgets: []
    },
    {
      name: 'Rechazado',
      state: EstadoPresupuesto.RECHAZADO,
      color: '#dc3545',
      budgets: []
    }
  ];

  ngOnInit(): void {
    this.loadBudgets();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    this.wsSubscription?.unsubscribe();
  }

  loadBudgets(): void {
    this.loading.set(true);
    this.error.set(null);

    this.budgetService.obtenerPresupuestos().subscribe({
      next: (response) => {
        const budgets = response.content || response;
        this.organizeBudgetsByState(budgets);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading budgets:', err);
        this.error.set('Error al cargar los presupuestos. Por favor, intente nuevamente.');
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los presupuestos'
        });
      }
    });
  }

  private organizeBudgetsByState(budgets: Presupuesto[]): void {
    // Limpiar columnas
    this.columns.forEach(col => col.budgets = []);

    // Distribuir presupuestos por estado
    // VENCIDO se muestra en la columna de ENVIADO
    budgets.forEach(budget => {
      const targetState = budget.estado === EstadoPresupuesto.VENCIDO
        ? EstadoPresupuesto.ENVIADO
        : budget.estado;
      const column = this.columns.find(col => col.state === targetState);
      if (column) {
        column.budgets.push(budget);
      }
    });

    // Ordenar presupuestos por fecha de creación (más recientes primero)
    this.columns.forEach(col => {
      col.budgets.sort((a, b) => {
        return new Date(b.fechaCreacion).getTime() - new Date(a.fechaCreacion).getTime();
      });
    });
  }

  private connectWebSocket(): void {
    this.wsService.connect();
    // Limpiar cualquier evento anterior para evitar mostrar notificaciones viejas al entrar
    this.wsService.clearLastPresupuestoEvent();

    this.wsSubscription = this.wsService.presupuestoEvent$.subscribe({
      next: (event: PresupuestoEvent | null) => {
        if (event) {
          this.handleWebSocketEvent(event);
          // Limpiar el evento después de procesarlo
          this.wsService.clearLastPresupuestoEvent();
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

  private handleWebSocketEvent(event: PresupuestoEvent): void {
    console.log('Handling WebSocket event:', event);

    switch (event.tipoEvento) {
      case 'CREADO':
        if (event.presupuesto) {
          this.addBudget(event.presupuesto);
          this.messageService.add({
            severity: 'info',
            summary: 'Nuevo Presupuesto',
            detail: `Presupuesto para servicio ${event.numeroServicio} creado`
          });
        }
        break;

      case 'ACTUALIZADO':
        if (event.presupuesto) {
          this.updateBudget(event.presupuesto);
        }
        break;

      case 'CAMBIO_ESTADO':
        if (event.presupuesto && event.estadoAnterior) {
          this.moveBudget(event.presupuesto, event.estadoAnterior);
          this.messageService.add({
            severity: 'success',
            summary: 'Estado Actualizado',
            detail: `Presupuesto de servicio ${event.numeroServicio} cambió de estado`
          });
        }
        break;

      case 'ELIMINADO':
        this.deleteBudget(event.presupuestoId);
        this.messageService.add({
          severity: 'info',
          summary: 'Presupuesto Eliminado',
          detail: `Presupuesto eliminado`
        });
        break;
    }
  }

  private getColumnForState(state: EstadoPresupuesto): Column | undefined {
    // VENCIDO se muestra en la columna ENVIADO
    const targetState = state === EstadoPresupuesto.VENCIDO ? EstadoPresupuesto.ENVIADO : state;
    return this.columns.find(col => col.state === targetState);
  }

  private addBudget(budget: Presupuesto): void {
    const column = this.getColumnForState(budget.estado);
    if (column) {
      const exists = column.budgets.some(b => b.id === budget.id);
      if (!exists) {
        column.budgets.unshift(budget);
      }
    }
  }

  private updateBudget(budget: Presupuesto): void {
    // Buscar en todas las columnas porque el estado pudo haber cambiado
    for (const col of this.columns) {
      const index = col.budgets.findIndex(b => b.id === budget.id);
      if (index !== -1) {
        col.budgets[index] = budget;
        return;
      }
    }
  }

  private moveBudget(budget: Presupuesto, previousState: EstadoPresupuesto): void {
    // Remove from previous state column
    const previousColumn = this.getColumnForState(previousState);
    if (previousColumn) {
      previousColumn.budgets = previousColumn.budgets.filter(b => b.id !== budget.id);
    }

    // Add to new state column
    const newColumn = this.getColumnForState(budget.estado);
    if (newColumn) {
      const exists = newColumn.budgets.some(b => b.id === budget.id);
      if (!exists) {
        newColumn.budgets.unshift(budget);
      }
    }
  }

  private deleteBudget(budgetId: number): void {
    this.columns.forEach(column => {
      column.budgets = column.budgets.filter(b => b.id !== budgetId);
    });
  }

  // Drag & Drop using Angular CDK
  dropBudget(event: CdkDragDrop<Presupuesto[]>, targetColumn?: Column): void {
    const budget = event.item.data as Presupuesto;
    const previousState = budget.estado;

    // Determine target state
    let targetState: EstadoPresupuesto;

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

    // Update state in backend
    this.budgetService.cambiarEstadoPresupuesto(budget.id, targetState).subscribe({
      next: () => {
        budget.estado = targetState;
        this.messageService.add({
          severity: 'success',
          summary: 'Estado Actualizado',
          detail: `Presupuesto movido a ${targetColumn.name}`
        });
      },
      error: (err) => {
        console.error('Error updating budget state:', err);
        // Revert the move
        transferArrayItem(
          event.container.data,
          event.previousContainer.data,
          event.currentIndex,
          event.previousIndex
        );

        // Determine specific error message based on error status
        let errorMessage = 'No se pudo cambiar el estado del presupuesto';
        let errorSummary = 'Error';

        if (err.status === 403) {
          errorSummary = 'Acceso Denegado';
          errorMessage = 'No tiene permisos para cambiar el estado de este presupuesto';
        } else if (err.status === 400 && err.error?.message) {
          errorSummary = 'Error de Validación';
          errorMessage = err.error.message;
        } else if (err.error?.message) {
          errorMessage = err.error.message;
        }

        this.messageService.add({
          severity: 'error',
          summary: errorSummary,
          detail: errorMessage
        });
      }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-UY', {
      style: 'currency',
      currency: 'UYU'
    }).format(amount);
  }

  refresh(): void {
    this.loadBudgets();
  }

  viewBudgetDetail(budgetId: number, event?: MouseEvent): void {
    // Prevent navigation if we're dragging
    if (event && (event.target as HTMLElement).closest('.cdk-drag-preview')) {
      return;
    }
    this.router.navigate(['/presupuestos', budgetId]);
  }
}
