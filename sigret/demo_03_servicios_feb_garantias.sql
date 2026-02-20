-- =============================================================
-- SIGRET — Datos de Demostración  |  Archivo 3 de 3
-- SERVICIOS FEBRERO 2026 + GARANTÍA GTA2600002
-- =============================================================
-- IMPORTANTE: Ejecutar DESPUÉS de demo_02_servicios_dic_ene.sql
--
-- Estados resultantes en el dashboard:
--   FINALIZADO          → 13 servicios (dic + ene)
--   TERMINADO           →  2 servicios (listos para retirar)
--   EN_REPARACION       →  2 servicios (en taller)
--   APROBADO            →  2 servicios (presupuesto aprobado, sin OT)
--   PRESUPUESTADO       →  2 servicios (esperando respuesta cliente)
--   RECIBIDO            →  2 servicios (recién ingresados)
--   RECHAZADO           →  5 servicios
--   GARANTIA_SIN_REP.   →  1 servicio
--   ESP. EVAL. GARANTIA →  1 servicio
-- =============================================================

-- =============================================================
-- SERVICIOS — FEBRERO 2026 (ids 19-27)
-- =============================================================
INSERT INTO servicios (
    id_servicio, numero_servicio, id_cliente, id_equipo, id_empleado_recepcion,
    tipo_ingreso, firma_ingreso, firma_conformidad, falla_reportada, observaciones,
    es_garantia, id_servicio_garantia, garantia_dentro_plazo, garantia_cumple_condiciones,
    observaciones_garantia, id_tecnico_evaluacion, fecha_evaluacion_garantia,
    observaciones_evaluacion_garantia, abona_visita, monto_visita, monto_pagado,
    estado, fecha_creacion, fecha_recepcion, fecha_devolucion_prevista,
    fecha_devolucion_real, activo
) VALUES
-- SRV2600011 | Marcelo Acosta | Televisor Sony KD-55X80K | TERMINADO (listo para retirar)
(19, 'SRV2600011', 7, 19, 3, 'CLIENTE_TRAE', NULL, NULL,
 'El televisor se apagó repentinamente y no enciende. La luz indicadora parpadea 6 veces y se apaga, repetidamente.',
 'Sin base. Control remoto incluido.', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'TERMINADO',
 '2026-02-03 09:30:00', '2026-02-03', '2026-02-17', '2026-02-17', 1),

-- SRV2600012 | Silvana Moreno | Lavarropas Whirlpool WLF80AB | EN_REPARACION
(20, 'SRV2600012', 8, 20, 2, 'CLIENTE_TRAE', NULL, NULL,
 'La lavarropas vibra excesivamente durante el centrifugado y pierde agua por la parte inferior del frente.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'EN_REPARACION',
 '2026-02-05 10:00:00', '2026-02-05', '2026-02-19', NULL, 1),

-- SRV2600013 | Ignacio Pereyra | Heladera Ariston HBB24DAABC | EN_REPARACION
(21, 'SRV2600013', 9, 21, 3, 'CLIENTE_TRAE', NULL, NULL,
 'La heladera no enfría correctamente. El compresor arranca pero se apaga a los pocos segundos.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'EN_REPARACION',
 '2026-02-07 11:15:00', '2026-02-07', '2026-02-21', NULL, 1),

-- SRV2600014 | Lorena Quintero | Aire Acond. Carrier 53HVH0181 | APROBADO (presupuesto aprobado, OT pendiente)
-- Ingresado como EMPRESA_BUSCA con visita técnica previa (abona_visita=true)
(22, 'SRV2600014', 10, 22, 2, 'EMPRESA_BUSCA', NULL, NULL,
 'El aire acondicionado dejó de funcionar completamente luego de una tormenta eléctrica intensa.',
 'Se abona visita técnica en domicilio. Unidad exterior con quemaduras visibles en la placa.',
 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 1, 5000.00, NULL, 'APROBADO',
 '2026-02-10 09:00:00', '2026-02-10', '2026-02-28', NULL, 1),

-- SRV2600015 | Eduardo Barrionuevo | Televisor Noblex 55X6500 | APROBADO (opción alternativa aprobada)
(23, 'SRV2600015', 11, 23, 3, 'CLIENTE_TRAE', NULL, NULL,
 'El televisor presenta líneas verticales de colores en toda la pantalla que no desaparecen.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'APROBADO',
 '2026-02-12 10:30:00', '2026-02-12', '2026-02-26', NULL, 1),

-- SRV2600016 | Natalia Correa | Lavarropas Drean Next 8.12 Eco | PRESUPUESTADO (esperando respuesta)
(24, 'SRV2600016', 12, 24, 2, 'CLIENTE_TRAE', NULL, NULL,
 'La lavarropas muestra el código de error E5 al iniciar el centrifugado. El ciclo se interrumpe abruptamente.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'PRESUPUESTADO',
 '2026-02-14 08:45:00', '2026-02-14', '2026-02-28', NULL, 1),

-- SRV2600017 | Jorge Méndez | Microondas Philco PHCT25 | PRESUPUESTADO (esperando respuesta)
(25, 'SRV2600017', 1, 25, 3, 'CLIENTE_TRAE', NULL, NULL,
 'El microondas no calienta y emite un ruido extraño al intentar funcionar.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'PRESUPUESTADO',
 '2026-02-17 11:00:00', '2026-02-17', '2026-03-03', NULL, 1),

-- SRV2600018 | Valentina Ramos | Heladera Philco FR-PHCE200 | RECIBIDO (recién ingresado)
(26, 'SRV2600018', 2, 26, 2, 'CLIENTE_TRAE', NULL, NULL,
 'La heladera hace un ruido intermitente (clic repetido) y la temperatura no es uniforme en todos los sectores.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'RECIBIDO',
 '2026-02-19 09:30:00', '2026-02-19', '2026-03-05', NULL, 1),

-- SRV2600019 | Héctor Flores | Lavavajillas Bosch SMV46MX03E | RECIBIDO (recién ingresado hoy)
(27, 'SRV2600019', 3, 27, 3, 'CLIENTE_TRAE', NULL, NULL,
 'El lavavajillas no finaliza el ciclo de lavado y muestra un código de error en el panel de control.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'RECIBIDO',
 '2026-02-20 10:00:00', '2026-02-20', '2026-03-06', NULL, 1);

-- =============================================================
-- GARANTÍA — GTA2600002 (id 29)
-- Eduardo Barrionuevo | Televisor LG 55UQ8050
-- Garantía del servicio SRV2600003 (id=11) devuelto el 2026-01-24
-- Dentro del plazo: 2026-01-24 + 90 días = 2026-04-24 > hoy ✓
-- =============================================================
INSERT INTO servicios (
    id_servicio, numero_servicio, id_cliente, id_equipo, id_empleado_recepcion,
    tipo_ingreso, firma_ingreso, firma_conformidad, falla_reportada, observaciones,
    es_garantia, id_servicio_garantia, garantia_dentro_plazo, garantia_cumple_condiciones,
    observaciones_garantia, id_tecnico_evaluacion, fecha_evaluacion_garantia,
    observaciones_evaluacion_garantia, abona_visita, monto_visita, monto_pagado,
    estado, fecha_creacion, fecha_recepcion, fecha_devolucion_prevista,
    fecha_devolucion_real, activo
) VALUES
(29, 'GTA2600002', 11, 11, 1, 'CLIENTE_TRAE', NULL, NULL,
 'El televisor presenta líneas horizontales similares a las que tenía antes de la reparación anterior.',
 'Ingreso por garantía del servicio SRV2600003. Pendiente de evaluación técnica.',
 1, 11, 1, NULL,
 'Cliente reporta recurrencia de líneas en pantalla. Pendiente de evaluación.',
 NULL, NULL, NULL,
 0, 0.00, NULL, 'ESPERANDO_EVALUACION_GARANTIA',
 '2026-02-18 14:00:00', '2026-02-18', NULL, NULL, 1);

-- =============================================================
-- DETALLE DE SERVICIOS — FEBRERO 2026
-- =============================================================
INSERT INTO detalle_servicios (id_detalle_servicio, id_servicio, componente, presente, comentario) VALUES
-- SRV2600011 — Televisor Sony KD-55X80K
(11, 19, 'Cable de alimentación',        1, NULL),
(12, 19, 'Control remoto',               1, NULL),
-- SRV2600018 — Heladera Philco FR-PHCE200
(13, 26, 'Cable de alimentación',        1, NULL),
(14, 26, 'Cajones y estantes interiores',1, NULL),
(15, 26, 'Bandejas de la puerta',        1, NULL);

-- =============================================================
-- PRESUPUESTOS — FEBRERO 2026 (ids 19-27)
-- Nota: no se crean presupuestos para servicios de garantía (GTA)
-- =============================================================
INSERT INTO presupuestos (
    id_presupuesto, numero_presupuesto, id_servicio, diagnostico, id_empleado,
    monto_repuestos_original, monto_repuestos_alternativo, mano_obra,
    monto_total_original, monto_total_alternativo,
    mostrar_original, mostrar_alternativo,
    tipo_confirmado, fecha_confirmacion, canal_confirmacion,
    estado, fecha_creacion, fecha_solicitud, fecha_pactada, fecha_vencimiento
) VALUES
-- PRE2600011 | SRV2600011 | Televisor Sony KD-55X80K | APROBADO (con opción dual)
-- Se ofrece placa original y placa alternativa compatible. Cliente eligió ORIGINAL.
(19, 'PRE2600011', 19,
 'Placa principal (main board) con fallo en el circuito de procesamiento de imagen. Se detectaron componentes SMD quemados en la sección de gestión de energía. Se ofrece opción con placa original de fábrica Sony o placa alternativa compatible de origen asiático.',
 3, 42000.00, 28000.00, 20000.00, 62000.00, 48000.00, 1, 1,
 'ORIGINAL', '2026-02-05 14:00:00', 'PRESENCIAL',
 'APROBADO', '2026-02-03 10:00:00', NULL, NULL, '2026-02-17'),

-- PRE2600012 | SRV2600012 | Lavarropas Whirlpool WLF80AB | APROBADO
(20, 'PRE2600012', 20,
 'Rodamiento 6205 2RS con juego axial excesivo causante de la vibración. Sello de agua roto que origina la pérdida de agua por la parte frontal del equipo. Se reemplazan ambas piezas y se verifica el estado del tambor.',
 2, 10000.00, NULL, 18000.00, 28000.00, NULL, 1, 0,
 'ORIGINAL', '2026-02-07 10:00:00', 'WHATSAPP',
 'APROBADO', '2026-02-05 10:30:00', NULL, NULL, '2026-02-19'),

-- PRE2600013 | SRV2600013 | Heladera Ariston HBB24DAABC | APROBADO
(21, 'PRE2600013', 21,
 'Termostato electrónico de control de temperatura defectuoso. El sensor NTC presenta deriva en la medición de temperatura, causando que el compresor reciba señal de parada prematura. Se reemplaza el termostato con sensor NTC integrado.',
 3, 7500.00, NULL, 15000.00, 22500.00, NULL, 1, 0,
 'ORIGINAL', '2026-02-09 11:00:00', 'TELEFONO',
 'APROBADO', '2026-02-07 11:45:00', NULL, NULL, '2026-02-21'),

-- PRE2600014 | SRV2600014 | Aire Acond. Carrier 53HVH0181 | APROBADO (sin OT creada aún)
(22, 'PRE2600014', 22,
 'Placa de control de la unidad exterior dañada por descarga atmosférica. El módulo de protección y el driver del compresor presentan componentes quemados. La unidad interior y el compresor se encuentran en buen estado.',
 2, 38000.00, NULL, 22000.00, 60000.00, NULL, 1, 0,
 'ORIGINAL', '2026-02-13 09:30:00', 'PRESENCIAL',
 'APROBADO', '2026-02-10 09:30:00', NULL, NULL, '2026-02-24'),

-- PRE2600015 | SRV2600015 | Televisor Noblex 55X6500 | APROBADO (opción alternativa elegida)
-- Panel original muy costoso. Cliente eligió opción ALTERNATIVA (kit retroiluminación).
(23, 'PRE2600015', 23,
 'Panel LCD con daño en la capa polarizadora y falla en dos tiras de retroiluminación LED. Se ofrecen dos alternativas: reemplazo completo del panel LCD original, o reparación con kit de tiras LED de retroiluminación manteniendo el panel actual.',
 3, 45000.00, 12000.00, 15000.00, 60000.00, 27000.00, 1, 1,
 'ALTERNATIVO', '2026-02-14 16:00:00', 'WHATSAPP',
 'APROBADO', '2026-02-12 11:00:00', NULL, NULL, '2026-02-26'),

-- PRE2600016 | SRV2600016 | Lavarropas Drean Next 8.12 Eco | ENVIADO (esperando respuesta)
(24, 'PRE2600016', 24,
 'El error E5 corresponde a falla en el módulo de control de velocidad del motor (módulo inverter). El componente fue verificado con osciloscopio y confirma señal defectuosa en la salida de control.',
 2, 16500.00, NULL, 18000.00, 34500.00, NULL, 1, 0,
 NULL, NULL, NULL,
 'ENVIADO', '2026-02-14 09:15:00', NULL, NULL, '2026-02-28'),

-- PRE2600017 | SRV2600017 | Microondas Philco PHCT25 | ENVIADO (esperando respuesta)
(25, 'PRE2600017', 25,
 'Magnetrón con filamento roto detectado por medición con multímetro (circuito abierto). Diodo de alta tensión sin continuidad en sentido directo. Se cotiza reemplazo de magnetrón y diodo HV.',
 3, 8500.00, NULL, 12000.00, 20500.00, NULL, 1, 0,
 NULL, NULL, NULL,
 'ENVIADO', '2026-02-17 11:30:00', NULL, NULL, '2026-03-03'),

-- PRE2600018 | SRV2600018 | Heladera Philco FR-PHCE200 | PENDIENTE (sin diagnóstico aún)
(26, 'PRE2600018', 26,
 '',
 2, 0.00, NULL, 0.00, 0.00, NULL, 1, 0,
 NULL, NULL, NULL,
 'PENDIENTE', '2026-02-19 09:45:00', NULL, NULL, NULL),

-- PRE2600019 | SRV2600019 | Lavavajillas Bosch SMV46MX03E | PENDIENTE (sin diagnóstico aún)
(27, 'PRE2600019', 27,
 '',
 3, 0.00, NULL, 0.00, 0.00, NULL, 1, 0,
 NULL, NULL, NULL,
 'PENDIENTE', '2026-02-20 10:15:00', NULL, NULL, NULL);

-- =============================================================
-- DETALLE DE PRESUPUESTOS — FEBRERO 2026 (ids 29-36)
-- PRE2600018 y PRE2600019 no tienen ítems (PENDIENTE sin diagnóstico)
-- =============================================================
INSERT INTO detalle_presupuestos (id_detalle_presupuesto, id_presupuesto, item, cantidad, precio_original, precio_alternativo) VALUES
-- PRE2600011 — Televisor Sony (id 29) — precio dual
(29, 19, 'Placa principal (main board) Sony KD-55X80K', 1, 42000.00, 28000.00),
-- PRE2600012 — Lavarropas Whirlpool (ids 30-31)
(30, 20, 'Rodamiento 6205 2RS',                         1, 4200.00,  NULL),
(31, 20, 'Sello de agua',                               1, 5800.00,  NULL),
-- PRE2600013 — Heladera Ariston (id 32)
(32, 21, 'Termostato electrónico con sensor NTC',        1, 7500.00,  NULL),
-- PRE2600014 — Aire Acond. Carrier (id 33)
(33, 22, 'Placa de control unidad exterior Carrier',     1, 38000.00, NULL),
-- PRE2600015 — Televisor Noblex (id 34) — precio dual
(34, 23, 'Panel LCD 55" / Kit retroiluminación LED',     1, 45000.00, 12000.00),
-- PRE2600016 — Lavarropas Drean (id 35)
(35, 24, 'Módulo de control inverter Drean',             1, 16500.00, NULL),
-- PRE2600017 — Microondas Philco (id 36)
(36, 25, 'Magnetrón compatible 2M246-01GF',              1, 8500.00,  NULL);

-- =============================================================
-- ÓRDENES DE TRABAJO — FEBRERO 2026 (ids 15-17)
-- =============================================================
INSERT INTO ordenes_trabajo (
    id_orden_trabajo, numero_orden_trabajo, id_servicio, id_presupuesto, id_empleado,
    monto_total_repuestos, monto_extras, observaciones_extras, es_sin_costo,
    estado, fecha_creacion, fecha_comienzo, fecha_fin
) VALUES
-- OT2600009 | SRV2600011 | Televisor Sony | gperalta | TERMINADA (placa original)
(15, 'OT2600009', 19, 19, 5, 42000.00, 0.00, NULL, 0, 'TERMINADA',
 '2026-02-05 14:30:00', '2026-02-06', '2026-02-17'),
-- OT2600010 | SRV2600012 | Lavarropas Whirlpool | respinola | EN_PROGRESO
(16, 'OT2600010', 20, 20, 4, 10000.00, 0.00, NULL, 0, 'EN_PROGRESO',
 '2026-02-07 10:30:00', '2026-02-10', NULL),
-- OT2600011 | SRV2600013 | Heladera Ariston | kroldan | EN_PROGRESO
(17, 'OT2600011', 21, 21, 8, 7500.00,  0.00, NULL, 0, 'EN_PROGRESO',
 '2026-02-09 11:30:00', '2026-02-12', NULL);

-- =============================================================
-- DETALLE DE ÓRDENES DE TRABAJO — FEBRERO 2026 (ids 23-26)
-- =============================================================
INSERT INTO detalle_ordenes_trabajo (
    id_detalle_orden_trabajo, id_orden_trabajo, id_repuesto,
    item_descripcion, cantidad, comentario, completado
) VALUES
-- OT2600009 — Televisor Sony (id 23) — ítem completado
(23, 15, NULL, 'Placa principal (main board) Sony KD-55X80K', 1, NULL, 1),
-- OT2600010 — Lavarropas Whirlpool (ids 24-25) — en progreso
(24, 16, NULL, 'Rodamiento 6205 2RS',                         1, NULL, 0),
(25, 16, NULL, 'Sello de agua',                               1, NULL, 0),
-- OT2600011 — Heladera Ariston (id 26) — en progreso
(26, 17, NULL, 'Termostato electrónico con sensor NTC',        1, NULL, 0);

-- =============================================================
-- REACTIVAR RESTRICCIONES DE CLAVE FORÁNEA
-- =============================================================
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
-- FIN DE LA CARGA DE DATOS DE DEMOSTRACIÓN
-- =============================================================
-- Resumen de lo cargado:
--   Empleados/Usuarios : 8  (1 propietario, 2 admin, 5 técnicos)
--   Clientes           : 12
--   Equipos            : 27
--   Servicios          : 29 (incluye 2 garantías)
--   Presupuestos       : 27
--   Órdenes de Trabajo : 17
-- =============================================================
