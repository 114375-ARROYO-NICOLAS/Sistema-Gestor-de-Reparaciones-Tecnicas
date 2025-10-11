# Debug Loop Infinito

## Instrucciones para el Usuario

Por favor, abre la consola del navegador (F12) y envíame:

1. **Cuántas veces aparece el mensaje** `🔄 Cargando empleados`
2. **Cuántas veces aparece el mensaje** `⚠️ Ya hay una carga en progreso`
3. **Screenshot o copia del Network tab** mostrando las peticiones

## También ejecuta esto en la consola:

```javascript
// Contar peticiones
const requests = performance.getEntriesByType('resource')
  .filter(r => r.name.includes('/api/empleados'))
  .length;
console.log('Total de peticiones a /api/empleados:', requests);
```

Esta información me dirá exactamente dónde está el problema.

