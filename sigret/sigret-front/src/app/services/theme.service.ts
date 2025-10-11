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
    
    // Toggle the dark mode class on <html> element as per PrimeNG documentation
    // This matches the darkModeSelector: '.app-dark' in app.config.ts
    if (theme === 'dark') {
      html.classList.add('app-dark');
    } else {
      html.classList.remove('app-dark');
    }
    
    // Also set data-theme attribute for compatibility with custom components
    html.setAttribute('data-theme', theme);
  }
}
