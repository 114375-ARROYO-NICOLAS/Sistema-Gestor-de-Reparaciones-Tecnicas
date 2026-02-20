import {
  Component,
  input,
  output,
  signal,
  ViewChild,
  ElementRef,
  AfterViewInit,
  OnDestroy,
  inject,
  effect
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';
import { Checkbox } from 'primeng/checkbox';
import { Tag } from 'primeng/tag';
import { MessageService, PrimeTemplate } from 'primeng/api';
import { ServicioService } from '../../services/servicio.service';
import SignaturePad from 'signature_pad';

@Component({
  selector: 'app-finalizar-trabajo-dialog',
  imports: [
    CommonModule,
    FormsModule,
    Dialog,
    Button,
    Checkbox,
    Tag,
    PrimeTemplate
  ],
  templateUrl: './finalizar-trabajo-dialog.html',
  styleUrl: './finalizar-trabajo-dialog.scss'
})
export class FinalizarTrabajoDialogComponent implements AfterViewInit, OnDestroy {
  readonly servicioId = input.required<number>();
  readonly servicioNumero = input<string>('');
  readonly confirmed = output<void>();

  @ViewChild('signatureCanvas') signatureCanvas!: ElementRef<HTMLCanvasElement>;
  private signaturePad!: SignaturePad;

  private readonly servicioService = inject(ServicioService);
  private readonly messageService = inject(MessageService);

  dialogVisible = false;
  aceptaConformidad = false;
  enviarEmail = false;
  descargarPdfCheck = false;

  readonly hasSignature = signal(false);
  readonly finalizando = signal(false);

  private canvasInitialized = false;

  constructor() {
    effect(() => {
      if (this.aceptaConformidad && !this.canvasInitialized) {
        setTimeout(() => this.setupCanvas(), 100);
      }
    });
  }

  ngAfterViewInit(): void {
    // Canvas will be set up when dialog opens
  }

  ngOnDestroy(): void {
    if (this.signaturePad) {
      this.signaturePad.off();
    }
  }

  showDialog(): void {
    this.dialogVisible = true;
    this.aceptaConformidad = false;
    this.enviarEmail = false;
    this.descargarPdfCheck = false;
    this.hasSignature.set(false);
    this.canvasInitialized = false;

    setTimeout(() => this.setupCanvas(), 300);
  }

  onDialogHide(): void {
    this.resetState();
  }

  private setupCanvas(): void {
    if (!this.signatureCanvas?.nativeElement) {
      return;
    }

    const canvas = this.signatureCanvas.nativeElement;

    if (this.signaturePad) {
      this.signaturePad.off();
    }

    const rect = canvas.getBoundingClientRect();
    const ratio = Math.max(window.devicePixelRatio || 1, 1);

    canvas.width = rect.width * ratio;
    canvas.height = rect.height * ratio;

    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.scale(ratio, ratio);
      canvas.style.width = `${rect.width}px`;
      canvas.style.height = `${rect.height}px`;
    }

    this.signaturePad = new SignaturePad(canvas, {
      backgroundColor: 'rgb(255, 255, 255)',
      penColor: 'rgb(0, 0, 0)',
      minWidth: 1,
      maxWidth: 3,
      throttle: 8,
      minDistance: 2,
      velocityFilterWeight: 0.5
    });

    this.signaturePad.addEventListener('beginStroke', () => {
      if (!this.aceptaConformidad) {
        this.signaturePad.clear();
        return;
      }
      this.hasSignature.set(true);
    });

    this.canvasInitialized = true;
  }

  clearSignature(): void {
    if (this.signaturePad) {
      this.signaturePad.clear();
      this.hasSignature.set(false);
    }
  }

  private getSignatureBase64(): string | null {
    if (this.signaturePad && !this.signaturePad.isEmpty()) {
      const dataUrl = this.signaturePad.toDataURL('image/png');
      return dataUrl.split(',')[1];
    }
    return null;
  }

  finalizar(): void {
    const firmaBase64 = this.getSignatureBase64();
    if (!firmaBase64) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Firma requerida',
        detail: 'Debe firmar para finalizar el trabajo'
      });
      return;
    }

    this.finalizando.set(true);

    this.servicioService.finalizarServicio(this.servicioId(), firmaBase64).subscribe({
      next: () => {
        // Ejecutar envíos seleccionados
        if (this.enviarEmail) {
          this.servicioService.enviarPdfFinalPorEmail(this.servicioId()).subscribe({
            next: () => this.messageService.add({
              severity: 'success',
              summary: 'Email enviado',
              detail: 'El comprobante final ha sido enviado por email'
            }),
            error: () => this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo enviar el email'
            })
          });
        }

        if (this.descargarPdfCheck) {
          this.servicioService.descargarPdfFinal(this.servicioId()).subscribe({
            next: (blob) => {
              const url = window.URL.createObjectURL(blob);
              const link = document.createElement('a');
              link.href = url;
              link.download = `comprobante-final-${this.servicioNumero()}.pdf`;
              link.click();
              window.URL.revokeObjectURL(url);
            },
            error: () => this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo descargar el PDF'
            })
          });
        }

        this.finalizando.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Trabajo finalizado',
          detail: 'El servicio ha sido finalizado correctamente'
        });
        this.confirmed.emit();
        this.dialogVisible = false;
        this.resetState();
      },
      error: (err) => {
        console.error('Error al finalizar servicio:', err);
        this.finalizando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: err.error?.message || 'No se pudo finalizar el servicio'
        });
      }
    });
  }

  cerrar(): void {
    this.dialogVisible = false;
    this.resetState();
  }

  private resetState(): void {
    this.aceptaConformidad = false;
    this.enviarEmail = false;
    this.descargarPdfCheck = false;
    this.hasSignature.set(false);
    this.finalizando.set(false);
    this.canvasInitialized = false;
    if (this.signaturePad) {
      this.signaturePad.clear();
    }
  }
}
