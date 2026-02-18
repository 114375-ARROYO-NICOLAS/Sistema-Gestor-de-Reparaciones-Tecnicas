import { Component, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ClientService } from '../../services/client.service';
import { ClientListDto } from '../../models/client.model';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { Tooltip } from 'primeng/tooltip';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-clientes-eliminados',
  imports: [
    CommonModule,
    Button,
    TableModule,
    ConfirmDialog,
    Tooltip,
    ToastModule
  ],
  providers: [ConfirmationService, MessageService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './clientes-eliminados.html'
})
export class ClientesEliminadosComponent {
  private readonly clientService = inject(ClientService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly clientes = signal<ClientListDto[]>([]);
  readonly totalRecords = signal(0);
  readonly restaurando = signal<number | null>(null);

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  cargarClientes(event?: any): void {
    this.loading.set(true);
    const page = event?.first != null && event?.rows ? event.first / event.rows : 0;
    const size = event?.rows ?? 10;

    this.clientService.getInactiveClients(page, size).subscribe({
      next: (response) => {
        this.clientes.set(response.content);
        this.totalRecords.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar clientes eliminados'
        });
        this.loading.set(false);
      }
    });
  }

  confirmarRestauracion(cliente: ClientListDto): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea restaurar al cliente "${cliente.nombreCompleto}"?`,
      header: 'Confirmar Restauración',
      icon: 'pi pi-refresh',
      acceptLabel: 'Sí, restaurar',
      rejectLabel: 'Cancelar',
      accept: () => this.restaurarCliente(cliente)
    });
  }

  private restaurarCliente(cliente: ClientListDto): void {
    this.restaurando.set(cliente.id);
    this.clientService.reactivateClient(cliente.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Cliente Restaurado',
          detail: `El cliente "${cliente.nombreCompleto}" fue restaurado correctamente`
        });
        this.cargarClientes();
        this.restaurando.set(null);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al restaurar el cliente'
        });
        this.restaurando.set(null);
      }
    });
  }

  verDetalle(id: number): void {
    this.router.navigate(['/clientes', id]);
  }

  volver(): void {
    this.router.navigate(['/clientes']);
  }
}
