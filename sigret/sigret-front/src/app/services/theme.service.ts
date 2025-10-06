import { Injectable, signal, computed } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly darkModeSubject = signal<boolean>(false);
  
  public readonly isDarkMode = computed(() => this.darkModeSubject());
  public readonly currentTheme = computed(() => this.isDarkMode() ? 'dark' : 'light');

  constructor() {
    // Check for saved theme preference or default to light mode
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
      this.darkModeSubject.set(savedTheme === 'dark');
    }
    this.applyTheme();
  }

  toggleTheme(): void {
    this.darkModeSubject.update(dark => !dark);
    this.applyTheme();
    localStorage.setItem('theme', this.currentTheme());
  }

  private applyTheme(): void {
    const theme = this.currentTheme();
    const html = document.documentElement;
    const body = document.body;
    
    // Aplicar clase dark-mode para PrimeNG en el html y body
    if (theme === 'dark') {
      html.classList.add('dark-mode');
      body.classList.add('dark-mode');
      html.classList.add('app-dark'); // También añadir app-dark para compatibilidad con Sakai
      body.classList.add('app-dark');
    } else {
      html.classList.remove('dark-mode');
      body.classList.remove('dark-mode');
      html.classList.remove('app-dark');
      body.classList.remove('app-dark');
    }
    
    // También aplicar data-theme para compatibilidad
    html.setAttribute('data-theme', theme);
  }
}
