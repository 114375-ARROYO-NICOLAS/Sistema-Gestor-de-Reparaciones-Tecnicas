import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { Select } from 'primeng/select';
import { AutoComplete } from 'primeng/autocomplete';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { EquipoService } from '../../services/equipo.service';
import { MarcaService } from '../../services/marca.service';
import { ModeloService } from '../../services/modelo.service';
import { TipoEquipoService } from '../../services/tipo-equipo.service';
import { ClientService } from '../../services/client.service';
import { MarcaListDto } from '../../models/marca.model';
import { ModeloListDto } from '../../models/modelo.model';
import { TipoEquipoListDto } from '../../models/tipo-equipo.model';
import { ClientListDto } from '../../models/client.model';
import { EquipoCreateDto } from '../../models/equipo.model';

@Component({
  selector: 'app-equipo-create',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    Button,
    InputText,
    Textarea,
    Select,
    AutoComplete,
    Toast
  ],
  templateUrl: './equipo-create.component.html',
  styleUrl: './equipo-create.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class EquipoCreateComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly equipoService = inject(EquipoService);
  private readonly marcaService = inject(MarcaService);
  private readonly modeloService = inject(ModeloService);
  private readonly tipoEquipoService = inject(TipoEquipoService);
  private readonly clientService = inject(ClientService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly messageService = inject(MessageService);

  readonly marcas = signal<MarcaListDto[]>([]);
  readonly modelos = signal<ModeloListDto[]>([]);
  readonly tiposEquipo = signal<TipoEquipoListDto[]>([]);
  readonly clientes = signal<ClientListDto[]>([]);
  readonly isSaving = signal(false);
  readonly isEditMode = signal(false);
  readonly equipoId = signal<number | null>(null);

  equipoForm: FormGroup;
  selectedCliente = signal<ClientListDto | null>(null);

  constructor() {
    this.equipoForm = this.fb.group({
      tipoEquipoId: [null, Validators.required],
      marcaId: [null, Validators.required],
      modeloId: [null],
      numeroSerie: [''],
      color: [''],
      observaciones: ['']
    });
  }

  ngOnInit(): void {
    this.loadMarcas();
    this.loadTiposEquipo();

    // Check if we're in edit mode
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nuevo') {
      this.isEditMode.set(true);
      this.equipoId.set(+id);
      this.loadEquipo(+id);
    }

    // Watch marca changes to load modelos
    this.equipoForm.get('marcaId')?.valueChanges.subscribe(marcaId => {
      if (marcaId) {
        this.loadModelosByMarca(marcaId);
      } else {
        this.modelos.set([]);
        this.equipoForm.patchValue({ modeloId: null });
      }
    });
  }

  loadEquipo(id: number): void {
    this.equipoService.getEquipoById(id).subscribe({
      next: (equipo) => {
        // TODO: Implementar carga de datos para edición
        console.log('Equipo cargado:', equipo);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar el equipo'
        });
        this.router.navigate(['/equipos']);
      }
    });
  }

  loadMarcas(): void {
    this.marcaService.getAllMarcas().subscribe({
      next: (marcas) => {
        this.marcas.set(marcas);
      },
      error: (error) => {
        console.error('Error al cargar marcas:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las marcas'
        });
      }
    });
  }

  loadModelosByMarca(marcaId: number): void {
    this.modeloService.getModelosByMarca(marcaId).subscribe({
      next: (modelos) => {
        this.modelos.set(modelos);
      },
      error: (error) => {
        console.error('Error al cargar modelos:', error);
      }
    });
  }

  loadTiposEquipo(): void {
    this.tipoEquipoService.getAllTiposEquipo().subscribe({
      next: (tipos) => {
        this.tiposEquipo.set(tipos);
      },
      error: (error) => {
        console.error('Error al cargar tipos de equipo:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los tipos de equipo'
        });
      }
    });
  }

  buscarClientes(event: any): void {
    const query = event.query || '';

    if (query.length < 2) {
      this.clientes.set([]);
      return;
    }

    this.clientService.autocompleteClients({ termino: query, limite: 10 }).subscribe({
      next: (clientes) => {
        this.clientes.set(clientes);
      },
      error: (error) => {
        console.error('Error al buscar clientes:', error);
      }
    });
  }

  onClienteSelected(event: any): void {
    const cliente = event.value as ClientListDto;
    this.selectedCliente.set(cliente);
  }

  onClienteCleared(): void {
    this.selectedCliente.set(null);
  }

  saveEquipo(): void {
    if (!this.equipoForm.valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario incompleto',
        detail: 'Por favor complete todos los campos requeridos'
      });
      return;
    }

    this.isSaving.set(true);
    const formValue = this.equipoForm.value;

    const equipoData: EquipoCreateDto = {
      tipoEquipoId: formValue.tipoEquipoId,
      marcaId: formValue.marcaId,
      modeloId: formValue.modeloId || undefined,
      numeroSerie: formValue.numeroSerie || undefined,
      color: formValue.color || undefined,
      observaciones: formValue.observaciones || undefined
    };

    this.equipoService.createEquipo(equipoData).subscribe({
      next: (equipo) => {
        // Si hay un cliente seleccionado, asociar el equipo
        const clienteId = this.selectedCliente()?.id;
        if (clienteId) {
          this.equipoService.asociarEquipoACliente(equipo.id, clienteId).subscribe({
            next: () => {
              this.messageService.add({
                severity: 'success',
                summary: 'Éxito',
                detail: 'Equipo creado y asociado al cliente correctamente'
              });
              this.router.navigate(['/equipos']);
            },
            error: (error) => {
              this.messageService.add({
                severity: 'warn',
                summary: 'Equipo creado',
                detail: 'Equipo creado pero no se pudo asociar al cliente'
              });
              this.router.navigate(['/equipos']);
            }
          });
        } else {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Equipo creado correctamente'
          });
          this.router.navigate(['/equipos']);
        }
        this.isSaving.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'No se pudo crear el equipo'
        });
        this.isSaving.set(false);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/equipos']);
  }
}
