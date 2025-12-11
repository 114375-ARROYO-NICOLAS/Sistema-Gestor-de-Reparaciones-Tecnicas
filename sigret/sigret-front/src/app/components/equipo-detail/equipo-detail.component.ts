import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { Badge } from 'primeng/badge';
import { Tag } from 'primeng/tag';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { EquipoService } from '../../services/equipo.service';
import { EquipoResponseDto } from '../../models/equipo.model';

@Component({
  selector: 'app-equipo-detail',
  imports: [
    CommonModule,
    Button,
    Card,
    Badge,
    Tag,
    Toast
  ],
  templateUrl: './equipo-detail.component.html',
  styleUrl: './equipo-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class EquipoDetailComponent implements OnInit {
  private readonly equipoService = inject(EquipoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly messageService = inject(MessageService);

  readonly equipo = signal<EquipoResponseDto | null>(null);
  readonly loading = signal(true);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadEquipo(+id);
    } else {
      this.router.navigate(['/equipos']);
    }
  }

  loadEquipo(id: number): void {
    this.loading.set(true);
    this.equipoService.getEquipoById(id).subscribe({
      next: (equipo) => {
        this.equipo.set(equipo);
        this.loading.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar el equipo'
        });
        this.loading.set(false);
        this.router.navigate(['/equipos']);
      }
    });
  }

  editEquipo(): void {
    const equipo = this.equipo();
    if (equipo) {
      this.router.navigate(['/equipos', equipo.id, 'editar']);
    }
  }

  goBack(): void {
    this.router.navigate(['/equipos']);
  }
}
