import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Table, TableModule } from 'primeng/table';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Toast } from 'primeng/toast';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Badge } from 'primeng/badge';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { TooltipModule } from 'primeng/tooltip';

import { EquipoService } from '../../services/equipo.service';
import { EquipoListDto } from '../../models/equipo.model';

@Component({
  selector: 'app-equipo-management',
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    Button,
    InputText,
    Toast,
    ConfirmDialog,
    Badge,
    IconField,
    InputIcon,
    TooltipModule
  ],
  templateUrl: './equipo-management.component.html',
  styleUrl: './equipo-management.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService, ConfirmationService]
})
export class EquipoManagementComponent implements OnInit {
  private readonly equipoService = inject(EquipoService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  readonly equipos = signal<EquipoListDto[]>([]);
  readonly loading = signal(true);
  readonly searchTerm = signal('');
  readonly expanded = signal(false);

  ngOnInit(): void {
    this.loadEquipos();
  }

  loadEquipos(): void {
    this.loading.set(true);
    this.equipoService.getAllEquipos().subscribe({
      next: (equipos) => {
        this.equipos.set(equipos);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error al cargar equipos:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los equipos'
        });
        this.loading.set(false);
      }
    });
  }

  onSearch(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchTerm.set(value);

    if (value.length >= 2) {
      this.equipoService.searchEquipos(value).subscribe({
        next: (equipos) => {
          this.equipos.set(equipos);
        },
        error: (error) => {
          console.error('Error al buscar equipos:', error);
        }
      });
    } else if (value.length === 0) {
      this.loadEquipos();
    }
  }

  viewEquipo(equipo: EquipoListDto): void {
    this.router.navigate(['/equipos', equipo.id]);
  }

  editEquipo(equipo: EquipoListDto): void {
    this.router.navigate(['/equipos', equipo.id, 'editar']);
  }

  deleteEquipo(equipo: EquipoListDto): void {
    this.confirmationService.confirm({
      message: `¿Está seguro que desea eliminar el equipo "${equipo.descripcionCompleta}"?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.equipoService.deleteEquipo(equipo.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Equipo eliminado correctamente'
            });
            this.loadEquipos();
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error.message || 'No se pudo eliminar el equipo'
            });
          }
        });
      }
    });
  }

  createEquipo(): void {
    this.router.navigate(['/equipos/nuevo']);
  }

  clearSearch(): void {
    this.searchTerm.set('');
    this.loadEquipos();
  }
}
