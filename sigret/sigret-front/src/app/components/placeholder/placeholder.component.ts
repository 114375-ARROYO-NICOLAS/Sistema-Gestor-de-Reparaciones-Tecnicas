import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-placeholder',
  imports: [CommonModule, CardModule, ButtonModule],
  template: `
    <div class="placeholder-container">
      <p-card>
        <ng-template pTemplate="header">
          <div class="placeholder-header">
            <i class="pi pi-cog" style="font-size: 3rem; color: #64748b;"></i>
            <h2>{{ getPageTitle() }}</h2>
          </div>
        </ng-template>
        
        <div class="placeholder-content">
          <p class="placeholder-description">
            Esta sección está en desarrollo. Próximamente estará disponible con todas las funcionalidades.
          </p>
          
          <div class="placeholder-actions">
            <p-button 
              label="Volver al Dashboard" 
              icon="pi pi-home" 
              severity="primary"
              (click)="goToDashboard()">
            </p-button>
          </div>
        </div>
      </p-card>
    </div>
  `,
  styleUrl: './placeholder.component.scss'
})
export class PlaceholderComponent {
  
  constructor(private router: Router) {}

  protected getPageTitle(): string {
    const url = this.router.url;
    const segments = url.split('/');
    const lastSegment = segments[segments.length - 1];
    
    const titles: { [key: string]: string } = {
      'reparaciones': 'Reparaciones',
      'equipos': 'Equipos',
      'mensajes': 'Mensajes',
      'calendario': 'Calendario',
      'clientes': 'Clientes',
      'tecnicos': 'Técnicos',
      'inventario': 'Inventario',
      'profile': 'Perfil de Usuario',
      'settings': 'Configuración'
    };
    
    return titles[lastSegment] || 'Página en Desarrollo';
  }

  protected goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}
