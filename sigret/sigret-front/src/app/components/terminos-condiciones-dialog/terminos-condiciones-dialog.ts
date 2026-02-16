import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';

@Component({
  selector: 'app-terminos-condiciones-dialog',
  imports: [CommonModule, Dialog, Button],
  templateUrl: './terminos-condiciones-dialog.html',
  styleUrl: './terminos-condiciones-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TerminosCondicionesDialogComponent {
  visible = signal(false);

  showDialog(): void {
    this.visible.set(true);
  }

  hideDialog(): void {
    this.visible.set(false);
  }
}
