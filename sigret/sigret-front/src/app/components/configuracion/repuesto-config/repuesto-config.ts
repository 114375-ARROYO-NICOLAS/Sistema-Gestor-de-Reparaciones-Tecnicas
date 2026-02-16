import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { InputText } from 'primeng/inputtext';
import { Dialog } from 'primeng/dialog';
import { Toast } from 'primeng/toast';
import { Select } from 'primeng/select';
import { MessageService } from 'primeng/api';
import { Tooltip } from 'primeng/tooltip';

import { RepuestoService } from '../../../services/repuesto.service';
import { TipoEquipoService } from '../../../services/tipo-equipo.service';
import { RepuestoListDto, RepuestoCreateDto, RepuestoUpdateDto } from '../../../models/repuesto.model';
import { TipoEquipoListDto } from '../../../models/tipo-equipo.model';

@Component({
  selector: 'app-repuesto-config',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Button,
    TableModule,
    InputText,
    Dialog,
    Toast,
    Select,
    Tooltip
  ],
  templateUrl: './repuesto-config.html',
  styleUrl: './repuesto-config.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class RepuestoConfigComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly repuestoService = inject(RepuestoService);
  private readonly tipoEquipoService = inject(TipoEquipoService);
  private readonly messageService = inject(MessageService);

  readonly repuestos = signal<RepuestoListDto[]>([]);
  readonly tiposEquipo = signal<TipoEquipoListDto[]>([]);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly showDialog = signal(false);
  readonly isEditMode = signal(false);
  readonly selectedRepuestoId = signal<number | null>(null);

  repuestoForm: FormGroup;

  constructor() {
    this.repuestoForm = this.fb.group({
      tipoEquipoId: [null, [Validators.required]],
      descripcion: ['', [Validators.required, Validators.maxLength(200)]]
    });
  }

  ngOnInit(): void {
    this.loadTiposEquipo();
    this.loadRepuestos();
  }

  loadTiposEquipo(): void {
    this.tipoEquipoService.getAllTiposEquipo().subscribe({
      next: (tipos) => {
        this.tiposEquipo.set(tipos);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los tipos de equipo'
        });
      }
    });
  }

  loadRepuestos(): void {
    this.isLoading.set(true);
    this.repuestoService.getAllRepuestos().subscribe({
      next: (repuestos) => {
        this.repuestos.set(repuestos);
        this.isLoading.set(false);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los repuestos'
        });
        this.isLoading.set(false);
      }
    });
  }

  openCreateDialog(): void {
    this.isEditMode.set(false);
    this.selectedRepuestoId.set(null);
    this.repuestoForm.reset();
    this.showDialog.set(true);
  }

  openEditDialog(repuesto: RepuestoListDto): void {
    this.isEditMode.set(true);
    this.selectedRepuestoId.set(repuesto.id);
    this.repuestoForm.patchValue({
      tipoEquipoId: repuesto.tipoEquipoId,
      descripcion: repuesto.descripcion
    });
    this.showDialog.set(true);
  }

  closeDialog(): void {
    this.showDialog.set(false);
    this.repuestoForm.reset();
  }

  saveRepuesto(): void {
    if (!this.repuestoForm.valid) {
      this.repuestoForm.markAllAsTouched();
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario incompleto',
        detail: 'Por favor complete todos los campos requeridos'
      });
      return;
    }

    this.isSaving.set(true);
    const formValue = this.repuestoForm.value;

    if (this.isEditMode()) {
      const updateData: RepuestoUpdateDto = {
        tipoEquipoId: formValue.tipoEquipoId,
        descripcion: formValue.descripcion
      };

      this.repuestoService.updateRepuesto(this.selectedRepuestoId()!, updateData).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Repuesto actualizado correctamente'
          });
          this.loadRepuestos();
          this.closeDialog();
          this.isSaving.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'No se pudo actualizar el repuesto'
          });
          this.isSaving.set(false);
        }
      });
    } else {
      const createData: RepuestoCreateDto = {
        tipoEquipoId: formValue.tipoEquipoId,
        descripcion: formValue.descripcion
      };

      this.repuestoService.createRepuesto(createData).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Repuesto creado correctamente'
          });
          this.loadRepuestos();
          this.closeDialog();
          this.isSaving.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'No se pudo crear el repuesto'
          });
          this.isSaving.set(false);
        }
      });
    }
  }

  deleteRepuesto(repuesto: RepuestoListDto): void {
    if (confirm(`¿Está seguro de eliminar el repuesto "${repuesto.descripcion}"?`)) {
      this.repuestoService.deleteRepuesto(repuesto.id).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Repuesto eliminado correctamente'
          });
          this.loadRepuestos();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'No se pudo eliminar el repuesto'
          });
        }
      });
    }
  }
}
