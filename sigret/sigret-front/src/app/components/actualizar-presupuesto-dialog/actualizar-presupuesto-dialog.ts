import { Component, inject, signal, computed, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { DatePickerModule } from 'primeng/datepicker';
import { CheckboxModule } from 'primeng/checkbox';
import { Textarea } from 'primeng/textarea';
import { Presupuesto, PresupuestoActualizarReenviarDto } from '../../models/presupuesto.model';
import { PresupuestoService } from '../../services/presupuesto.service';
import { MessageService } from 'primeng/api';
import { finalize } from 'rxjs';

export interface ActualizarPresupuestoResult {
  presupuesto: Presupuesto;
  reenviado: boolean;
}

@Component({
  selector: 'app-actualizar-presupuesto-dialog',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    DialogModule,
    ButtonModule,
    InputNumberModule,
    DatePickerModule,
    CheckboxModule,
    Textarea
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './actualizar-presupuesto-dialog.html'
})
export class ActualizarPresupuestoDialog {
  private readonly fb = inject(FormBuilder);
  private readonly presupuestoService = inject(PresupuestoService);
  private readonly messageService = inject(MessageService);

  readonly visible = input<boolean>(false);
  readonly presupuesto = input<Presupuesto | null>(null);

  readonly visibleChange = output<boolean>();
  readonly updated = output<ActualizarPresupuestoResult>();

  readonly guardando = signal<boolean>(false);
  readonly reenviarEmail = signal<boolean>(false);
  readonly mostrarOriginal = signal<boolean>(true);
  readonly mostrarAlternativo = signal<boolean>(false);
  readonly mensajeAdicional = signal<string>('');

  readonly minFechaVencimiento = new Date(new Date().setDate(new Date().getDate() + 1));

  formulario!: FormGroup;

  readonly montoTotalOriginal = computed(() => {
    // This is recalculated when we need it via method call since FormArray isn't signal-based
    return this._calcularMontoTotalOriginal();
  });

  readonly montoTotalAlternativo = computed(() => {
    return this._calcularMontoTotalAlternativo();
  });

  onShow(): void {
    const p = this.presupuesto();
    if (!p) return;

    this.inicializarFormulario(p);
    this.mostrarOriginal.set(p.mostrarOriginal);
    this.mostrarAlternativo.set(p.mostrarAlternativo);
    this.reenviarEmail.set(false);
    this.mensajeAdicional.set('');
  }

  private inicializarFormulario(presupuesto: Presupuesto): void {
    this.formulario = this.fb.group({
      fechaVencimiento: [
        presupuesto.fechaVencimiento ? new Date(presupuesto.fechaVencimiento) : null,
        Validators.required
      ],
      manoObra: [presupuesto.manoObra, [Validators.required, Validators.min(0)]],
      detalles: this.fb.array([])
    });

    const detallesArray = this.formulario.get('detalles') as FormArray;
    for (const detalle of presupuesto.detalles) {
      detallesArray.push(this.fb.group({
        item: [detalle.item, Validators.required],
        cantidad: [detalle.cantidad, [Validators.required, Validators.min(1)]],
        precioOriginal: [detalle.precioOriginal, Validators.min(0)],
        precioAlternativo: [detalle.precioAlternativo ?? null, Validators.min(0)]
      }));
    }
  }

  get detalles(): FormArray {
    return this.formulario?.get('detalles') as FormArray;
  }

  calcularMontoTotalOriginal(): number {
    return this._calcularMontoTotalOriginal();
  }

  calcularMontoTotalAlternativo(): number | null {
    return this._calcularMontoTotalAlternativo();
  }

  private _calcularMontoTotalOriginal(): number {
    if (!this.formulario) return 0;
    const manoObra = this.formulario.get('manoObra')?.value || 0;
    const detallesArr = this.formulario.get('detalles') as FormArray;
    if (!detallesArr) return manoObra;

    let montoRepuestos = 0;
    for (let i = 0; i < detallesArr.length; i++) {
      const cantidad = detallesArr.at(i).get('cantidad')?.value || 0;
      const precio = detallesArr.at(i).get('precioOriginal')?.value || 0;
      montoRepuestos += cantidad * precio;
    }
    return montoRepuestos + manoObra;
  }

  private _calcularMontoTotalAlternativo(): number | null {
    if (!this.formulario) return null;
    const manoObra = this.formulario.get('manoObra')?.value || 0;
    const detallesArr = this.formulario.get('detalles') as FormArray;
    if (!detallesArr) return null;

    let montoRepuestos = 0;
    let tieneAlternativos = false;

    for (let i = 0; i < detallesArr.length; i++) {
      const cantidad = detallesArr.at(i).get('cantidad')?.value || 0;
      const precioAlt = detallesArr.at(i).get('precioAlternativo')?.value;
      if (precioAlt) {
        tieneAlternativos = true;
        montoRepuestos += cantidad * precioAlt;
      }
    }

    return tieneAlternativos ? montoRepuestos + manoObra : null;
  }

  onConfirm(): void {
    if (!this.formulario || this.formulario.invalid || !this.presupuesto()) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Complete todos los campos requeridos'
      });
      return;
    }

    if (this.reenviarEmail() && !this.mostrarOriginal() && !this.mostrarAlternativo()) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Debe seleccionar al menos un precio para mostrar al cliente'
      });
      return;
    }

    this.guardando.set(true);

    const formValues = this.formulario.value;
    const dto: PresupuestoActualizarReenviarDto = {
      fechaVencimiento: formValues.fechaVencimiento
        ? new Date(formValues.fechaVencimiento).toISOString().split('T')[0]
        : '',
      detalles: formValues.detalles.map((d: any) => ({
        item: d.item,
        cantidad: d.cantidad,
        precioOriginal: d.precioOriginal,
        precioAlternativo: d.precioAlternativo || undefined
      })),
      manoObra: formValues.manoObra,
      mostrarOriginal: this.mostrarOriginal(),
      mostrarAlternativo: this.mostrarAlternativo(),
      reenviarEmail: this.reenviarEmail(),
      mensajeAdicional: this.mensajeAdicional() || undefined
    };

    this.presupuestoService.actualizarYReenviar(this.presupuesto()!.id, dto)
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe({
        next: (presupuestoActualizado) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Exito',
            detail: this.reenviarEmail()
              ? 'Presupuesto actualizado y reenviado al cliente'
              : 'Presupuesto actualizado correctamente'
          });
          this.updated.emit({
            presupuesto: presupuestoActualizado,
            reenviado: this.reenviarEmail()
          });
          this.visibleChange.emit(false);
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: err.error?.message || 'Error al actualizar el presupuesto'
          });
        }
      });
  }

  onHide(): void {
    this.visibleChange.emit(false);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS'
    }).format(amount);
  }
}
