import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Dialog } from 'primeng/dialog';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { ModeloService } from '../../../services/modelo.service';
import { MarcaService } from '../../../services/marca.service';
import { ModeloListDto, ModeloCreateDto, ModeloUpdateDto } from '../../../models/modelo.model';
import { MarcaListDto } from '../../../models/marca.model';

@Component({
  selector: 'app-modelo-config',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Button,
    TableModule,
    InputText,
    Select,
    Dialog,
    Toast
  ],
  templateUrl: './modelo-config.html',
  styleUrl: './modelo-config.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class ModeloConfigComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly modeloService = inject(ModeloService);
  private readonly marcaService = inject(MarcaService);
  private readonly messageService = inject(MessageService);

  readonly modelos = signal<ModeloListDto[]>([]);
  readonly marcas = signal<MarcaListDto[]>([]);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly showDialog = signal(false);
  readonly isEditMode = signal(false);
  readonly selectedModeloId = signal<number | null>(null);

  modeloForm: FormGroup;

  constructor() {
    this.modeloForm = this.fb.group({
      marcaId: [null, Validators.required],
      descripcion: ['', [Validators.required, Validators.maxLength(100)]]
    });
  }

  ngOnInit(): void {
    this.loadModelos();
    this.loadMarcas();
  }

  loadModelos(): void {
    this.isLoading.set(true);
    this.modeloService.getAllModelos().subscribe({
      next: (modelos) => {
        this.modelos.set(modelos);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los modelos'
        });
        this.isLoading.set(false);
      }
    });
  }

  loadMarcas(): void {
    this.marcaService.getAllMarcas().subscribe({
      next: (marcas) => {
        this.marcas.set(marcas);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las marcas'
        });
      }
    });
  }

  openCreateDialog(): void {
    this.isEditMode.set(false);
    this.selectedModeloId.set(null);
    this.modeloForm.reset();
    this.showDialog.set(true);
  }

  openEditDialog(modelo: ModeloListDto): void {
    this.isEditMode.set(true);
    this.selectedModeloId.set(modelo.id);
    this.modeloForm.patchValue({
      marcaId: modelo.marcaId,
      descripcion: modelo.descripcion
    });
    this.showDialog.set(true);
  }

  closeDialog(): void {
    this.showDialog.set(false);
    this.modeloForm.reset();
  }

  saveModelo(): void {
    if (!this.modeloForm.valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario incompleto',
        detail: 'Por favor complete todos los campos requeridos'
      });
      return;
    }

    this.isSaving.set(true);
    const formValue = this.modeloForm.value;

    if (this.isEditMode()) {
      const updateData: ModeloUpdateDto = {
        marcaId: formValue.marcaId,
        descripcion: formValue.descripcion
      };

      this.modeloService.updateModelo(this.selectedModeloId()!, updateData).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Modelo actualizado correctamente'
          });
          this.loadModelos();
          this.closeDialog();
          this.isSaving.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'No se pudo actualizar el modelo'
          });
          this.isSaving.set(false);
        }
      });
    } else {
      const createData: ModeloCreateDto = {
        marcaId: formValue.marcaId,
        descripcion: formValue.descripcion
      };

      this.modeloService.createModelo(createData).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Modelo creado correctamente'
          });
          this.loadModelos();
          this.closeDialog();
          this.isSaving.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'No se pudo crear el modelo'
          });
          this.isSaving.set(false);
        }
      });
    }
  }

  getMarcaNombre(marcaId: number): string {
    const marca = this.marcas().find(m => m.id === marcaId);
    return marca?.descripcion || '-';
  }
}
