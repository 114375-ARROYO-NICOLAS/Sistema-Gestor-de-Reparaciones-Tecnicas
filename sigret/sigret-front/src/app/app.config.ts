import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection, isDevMode } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { providePrimeNG } from 'primeng/config';
import Lara from '@primeng/themes/lara';
import { definePreset } from '@primeuix/themes';

// Servicios útiles
import { MessageService } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';

// Interceptors
import { authInterceptor } from './interceptors/auth.interceptor';
import { sessionExpiryInterceptor } from './interceptors/session-expiry.interceptor';

import { routes } from './app.routes';
import { provideServiceWorker } from '@angular/service-worker';

// ArroyoElectromecanica corporate theme - Blue (#2171a5) and Burgundy (#7b1a2d)
const ArroyoTheme = definePreset(Lara, {
  primitive: {
    arroyo: {
      50: '#e8f1f8',
      100: '#c5dcee',
      200: '#9ec4e3',
      300: '#77acd6',
      400: '#5896cb',
      500: '#2171a5',
      600: '#1c6091',
      700: '#174f7a',
      800: '#123e63',
      900: '#0d2d4c',
      950: '#081d33'
    }
  },
  semantic: {
    primary: {
      50: '{arroyo.50}',
      100: '{arroyo.100}',
      200: '{arroyo.200}',
      300: '{arroyo.300}',
      400: '{arroyo.400}',
      500: '{arroyo.500}',
      600: '{arroyo.600}',
      700: '{arroyo.700}',
      800: '{arroyo.800}',
      900: '{arroyo.900}',
      950: '{arroyo.950}'
    },
    colorScheme: {
      light: {
        primary: {
          color: '{arroyo.600}',
          contrastColor: '#ffffff',
          hoverColor: '{arroyo.700}',
          activeColor: '{arroyo.800}'
        },
        content: {
          borderColor: '{surface.300}'
        }
      },
      dark: {
        primary: {
          color: '{arroyo.400}',
          contrastColor: '{arroyo.950}',
          hoverColor: '{arroyo.300}',
          activeColor: '{arroyo.200}'
        },
        content: {
          borderColor: '{surface.600}'
        }
      }
    }
  }
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideHttpClient(
      withInterceptorsFromDi(),
      withInterceptors([authInterceptor, sessionExpiryInterceptor])
    ),
    provideAnimations(),
    providePrimeNG({
      theme: {
        preset: ArroyoTheme,
        options: {
          prefix: 'p',
          darkModeSelector: '.app-dark',  // Custom selector for dark mode toggle
          cssLayer: false
        }
      },
      ripple: true,
      translation: {
        firstDayOfWeek: 1,
        dayNames: ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'],
        dayNamesShort: ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'],
        dayNamesMin: ['Do', 'Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sa'],
        monthNames: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'],
        monthNamesShort: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'],
        today: 'Hoy',
        clear: 'Limpiar',
        dateFormat: 'dd/mm/yy',
        weekHeader: 'Sm'
      }
    }),
    
    // Servicios útiles para toasts, confirmaciones, etc.
    MessageService,
    ConfirmationService,
    
    provideRouter(routes), 
    provideServiceWorker('ngsw-worker.js', {
      enabled: !isDevMode(),
      registrationStrategy: 'registerWhenStable:30000'
    })
  ]
};