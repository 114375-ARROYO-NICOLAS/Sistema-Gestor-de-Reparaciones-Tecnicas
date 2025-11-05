import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { Table, TableModule } from 'primeng/table';
import { InputText } from 'primeng/inputtext';
import { Dialog } from 'primeng/dialog';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { TipoEquipoService } from '../../../services/tipo-equipo.service';
import { TipoEquipoListDto, TipoEquipoCreateDto, TipoEquipoUpdateDto } from '../../../models/tipo-equipo.model';

@Component({
  selector: 'app-tipo-equipo-config',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Button,
    TableModule,
    InputText,
    Dialog,
    Toast
  ],
  templateUrl: './tipo-equipo-config.html',
  styleUrl: './tipo-equipo-config.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class TipoEquipoConfigComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly tipoEquipoService = inject(TipoEquipoService);
  private readonly messageService = inject(MessageService);

  readonly tiposEquipo = signal<TipoEquipoListDto[]>([]);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly showDialog = signal(false);
  readonly isEditMode = signal(false);
  readonly selectedTipoId = signal<number | null>(null);

  tipoForm: FormGroup;

  constructor() {
    this.tipoForm = this.fb.group({
      descripcion: ['', [Validators.required, Validators.maxLength(100)]]
    });
  }

  ngOnInit(): void {
    this.loadTiposEquipo();
  }

  loadTiposEquipo(): void {
    this.isLoading.set(true);
    this.tipoEquipoService.getAllTiposEquipo().subscribe({
      next: (tipos) => {
        this.tiposEquipo.set(tipos);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los tipos de equipo'
        });
        this.isLoading.set(false);
      }
    });
  }

  openCreateDialog(): void {
    this.isEditMode.set(false);
    this.selectedTipoId.set(null);
    this.tipoForm.reset();
    this.showDialog.set(true);
  }

  openEditDialog(tipo: TipoEquipoListDto): void {
    this.isEditMode.set(true);
    this.selectedTipoId.set(tipo.id);
    this.tipoForm.patchValue({
      descripcion: tipo.descripcion
    });
    this.showDialog.set(true);
  }

  closeDialog(): void {
    this.showDialog.set(false);
    this.tipoForm.reset();
  }

  saveTipo(): void {
    if (!this.tipoForm.valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario incompleto',
        detail: 'Por favor complete todos los campos requeridos'
      });
      return;
    }

    this.isSaving.set(true);
    const formValue = this.tipoForm.value;

    if (this.isEditMode()) {
      const updateData: TipoEquipoUpdateDto = {
        descripcion: formValue.descripcion
      };

      this.tipoEquipoService.updateTipoEquipo(this.selectedTipoId()!, updateData).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Tipo de equipo actualizado correctamente'
          });
          this.loadTiposEquipo();
          this.closeDialog();
          this.isSaving.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'No se pudo actualizar el tipo de equipo'
          });
          this.isSaving.set(false);
        }
      });
    } else {
      const createData: TipoEquipoCreateDto = {
        descripcion: formValue.descripcion
      };

      this.tipoEquipoService.createTipoEquipo(createData).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Tipo de equipo creado correctamente'
          });
          this.loadTiposEquipo();
          this.closeDialog();
          this.isSaving.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'No se pudo crear el tipo de equipo'
          });
          this.isSaving.set(false);
        }
      });
    }
  }
}
