import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { InputText } from 'primeng/inputtext';
import { Dialog } from 'primeng/dialog';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { MarcaService } from '../../../services/marca.service';
import { MarcaListDto, MarcaCreateDto, MarcaUpdateDto } from '../../../models/marca.model';

@Component({
  selector: 'app-marca-config',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Button,
    TableModule,
    InputText,
    Dialog,
    Toast
  ],
  templateUrl: './marca-config.html',
  styleUrl: './marca-config.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class MarcaConfigComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly marcaService = inject(MarcaService);
  private readonly messageService = inject(MessageService);

  readonly marcas = signal<MarcaListDto[]>([]);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly showDialog = signal(false);
  readonly isEditMode = signal(false);
  readonly selectedMarcaId = signal<number | null>(null);

  marcaForm: FormGroup;

  constructor() {
    this.marcaForm = this.fb.group({
      descripcion: ['', [Validators.required, Validators.maxLength(100)]]
    });
  }

  ngOnInit(): void {
    this.loadMarcas();
  }

  loadMarcas(): void {
    this.isLoading.set(true);
    this.marcaService.getAllMarcas().subscribe({
      next: (marcas) => {
        this.marcas.set(marcas);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las marcas'
        });
        this.isLoading.set(false);
      }
    });
  }

  openCreateDialog(): void {
    this.isEditMode.set(false);
    this.selectedMarcaId.set(null);
    this.marcaForm.reset();
    this.showDialog.set(true);
  }

  openEditDialog(marca: MarcaListDto): void {
    this.isEditMode.set(true);
    this.selectedMarcaId.set(marca.id);
    this.marcaForm.patchValue({
      descripcion: marca.descripcion
    });
    this.showDialog.set(true);
  }

  closeDialog(): void {
    this.showDialog.set(false);
    this.marcaForm.reset();
  }

  saveMarca(): void {
    if (!this.marcaForm.valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario incompleto',
        detail: 'Por favor complete todos los campos requeridos'
      });
      return;
    }

    this.isSaving.set(true);
    const formValue = this.marcaForm.value;

    if (this.isEditMode()) {
      const updateData: MarcaUpdateDto = {
        descripcion: formValue.descripcion
      };

      this.marcaService.updateMarca(this.selectedMarcaId()!, updateData).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Marca actualizada correctamente'
          });
          this.loadMarcas();
          this.closeDialog();
          this.isSaving.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'No se pudo actualizar la marca'
          });
          this.isSaving.set(false);
        }
      });
    } else {
      const createData: MarcaCreateDto = {
        descripcion: formValue.descripcion
      };

      this.marcaService.createMarca(createData).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Marca creada correctamente'
          });
          this.loadMarcas();
          this.closeDialog();
          this.isSaving.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'No se pudo crear la marca'
          });
          this.isSaving.set(false);
        }
      });
    }
  }
}
