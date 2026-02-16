import { Component, inject, signal, computed, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Checkbox } from 'primeng/checkbox';
import { InputText } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { ServicioList } from '../../models/servicio.model';
import { ServicioService } from '../../services/servicio.service';
import { ItemEvaluacionForm, ItemEvaluacionGarantia } from '../../models/item-evaluacion-garantia.model';

export type ResultadoEvaluacion = 'CUMPLE' | 'NO_CUMPLE' | 'SIN_REPARACION';

export interface EvaluacionResult {
  resultado: ResultadoEvaluacion;
  itemsSeleccionados?: ItemEvaluacionGarantia[];
  observaciones?: string;
}

@Component({
  selector: 'app-evaluacion-garantia-dialog',
  imports: [
    CommonModule,
    FormsModule,
    Dialog,
    Button,
    ProgressSpinner,
    Checkbox,
    InputText,
    TextareaModule
  ],
  templateUrl: './evaluacion-garantia-dialog.html',
  styleUrl: './evaluacion-garantia-dialog.scss'
})
export class EvaluacionGarantiaDialog {
  private readonly servicioService = inject(ServicioService);

  // Outputs
  readonly confirmed = output<EvaluacionResult>();
  readonly cancelled = output<void>();

  // State
  visible = false;
  servicio!: ServicioList;
  readonly resultado = signal<ResultadoEvaluacion>('CUMPLE');
  observaciones = '';

  // Items
  readonly cargandoItems = signal<boolean>(false);
  readonly itemsEvaluacion = signal<ItemEvaluacionForm[]>([]);

  // Computed
  readonly itemsSeleccionados = computed(() =>
    this.itemsEvaluacion().filter(item => item.seleccionado)
  );

  readonly dialogHeader = computed(() => {
    switch (this.resultado()) {
      case 'CUMPLE':
        return 'Evaluar Garantía - Cumple Condiciones';
      case 'NO_CUMPLE':
        return 'Evaluar Garantía - No Cumple';
      case 'SIN_REPARACION':
        return 'Evaluar Garantía - Sin Reparación';
      default:
        return 'Evaluar Garantía';
    }
  });

  /**
   * Abre el diálogo de evaluación
   */
  open(servicio: ServicioList, resultadoEval: ResultadoEvaluacion): void {
    console.log('📋 Abriendo diálogo de evaluación', { servicio, resultadoEval });
    this.servicio = servicio;
    this.resultado.set(resultadoEval);
    this.observaciones = '';
    this.itemsEvaluacion.set([]);
    this.visible = true;

    // Solo cargar items si la garantía CUMPLE
    if (resultadoEval === 'CUMPLE') {
      console.log('✅ Garantía CUMPLE - Cargando items del servicio original, ID:', servicio.id);
      this.cargarItemsServicioOriginal();
    } else {
      console.log('❌ Garantía NO CUMPLE o SIN REPARACION:', resultadoEval);
    }
  }

  /**
   * Carga los items del servicio original para evaluación
   */
  private cargarItemsServicioOriginal(): void {
    console.log('🔄 Iniciando carga de items...');
    this.cargandoItems.set(true);

    this.servicioService.obtenerItemsServicioOriginal(this.servicio.id).subscribe({
      next: (items) => {
        console.log('✅ Items recibidos del backend:', items);
        // Convertir a ItemEvaluacionForm
        const itemsForm: ItemEvaluacionForm[] = items.map(item => ({
          ...item,
          seleccionado: false,
          comentarioEvaluacion: ''
        }));
        this.itemsEvaluacion.set(itemsForm);
        console.log('📝 Items seteados en signal:', itemsForm);
        this.cargandoItems.set(false);
      },
      error: (error) => {
        console.error('❌ Error al cargar items del servicio original:', error);
        this.cargandoItems.set(false);
        // En caso de error, permitir continuar sin items
        this.itemsEvaluacion.set([]);
      }
    });
  }

  /**
   * Valida si el formulario es válido para enviar
   */
  esFormularioValido(): boolean {
    if (this.resultado() === 'CUMPLE') {
      // Para CUMPLE, debe haber al menos un item seleccionado
      // O permitir continuar sin items si no hay ninguno disponible
      return this.itemsEvaluacion().length === 0 || this.itemsSeleccionados().length > 0;
    } else {
      // Para NO_CUMPLE y SIN_REPARACION, las observaciones son obligatorias
      return this.observaciones.trim().length > 0;
    }
  }

  /**
   * Confirma la evaluación
   */
  onConfirm(): void {
    if (!this.esFormularioValido()) {
      return;
    }

    const result: EvaluacionResult = {
      resultado: this.resultado()
    };

    if (this.resultado() === 'CUMPLE') {
      // Convertir items seleccionados a ItemEvaluacionGarantia (solo los que tienen repuestoId)
      result.itemsSeleccionados = this.itemsSeleccionados()
        .filter(item => item.repuestoId !== null)
        .map(item => ({
          repuestoId: item.repuestoId!,
          comentario: item.comentarioEvaluacion || undefined
        }));
    } else {
      result.observaciones = this.observaciones;
    }

    this.confirmed.emit(result);
    this.visible = false;
  }

  /**
   * Cancela la evaluación
   */
  onCancel(): void {
    this.cancelled.emit();
    this.visible = false;
  }
}
