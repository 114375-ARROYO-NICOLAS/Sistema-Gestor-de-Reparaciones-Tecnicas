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
    const body = document.body;
    
    // Aplicar clase dark-mode para PrimeNG
    if (theme === 'dark') {
      body.classList.add('dark-mode');
    } else {
      body.classList.remove('dark-mode');
    }
    
    // También aplicar data-theme para compatibilidad
    document.documentElement.setAttribute('data-theme', theme);
  }
}
