-- =============================================================
-- SIGRET — Datos de Demostración  |  Archivo 2 de 3
-- SERVICIOS DICIEMBRE 2025 + ENERO 2026
-- =============================================================
-- IMPORTANTE: Ejecutar DESPUÉS de demo_01_catalogo_y_usuarios.sql
--
-- IDs de empleados relevantes:
--   1 = lbertini (Propietario)      4 = respinola (Técnico lavarropas)
--   2 = vmolina  (Administrativo)   5 = gperalta  (Técnico televisores)
--   3 = soviedo  (Administrativo)   6 = haguirre  (Técnico AC)
--                                   7 = mcordoba  (Técnico microondas/horno)
--                                   8 = kroldan   (Técnico general)
-- =============================================================

-- =============================================================
-- SERVICIOS — DICIEMBRE 2025 (ids 1-8)
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
-- SRV2500001 | Jorge Méndez | Lavarropas Samsung WW90T | FINALIZADO
(1, 'SRV2500001', 1, 1, 2, 'CLIENTE_TRAE', NULL, NULL,
 'El lavarropas hace ruido metálico fuerte durante el centrifugado y vibra en exceso.',
 'Equipo ingresado en buen estado externo.', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 26500.00, 'FINALIZADO',
 '2025-12-02 09:15:00', '2025-12-02', '2025-12-12', '2025-12-11', 1),

-- SRV2500002 | Valentina Ramos | Heladera LG GR-B429GGUA | FINALIZADO
(2, 'SRV2500002', 2, 2, 2, 'CLIENTE_TRAE', NULL, NULL,
 'La heladera no enfría. El compresor arranca y se detiene al cabo de unos segundos.',
 'Sin bandeja colectora de agua.', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 60000.00, 'FINALIZADO',
 '2025-12-05 10:30:00', '2025-12-05', '2025-12-19', '2025-12-18', 1),

-- SRV2500003 | Héctor Flores | Televisor Samsung UN55AU7000 | FINALIZADO
(3, 'SRV2500003', 3, 3, 3, 'CLIENTE_TRAE', NULL, NULL,
 'El televisor se apagó repentinamente y no enciende más. Al momento de fallar se percibió olor a quemado.',
 'Sin base/soporte. Control remoto incluido.', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 35500.00, 'FINALIZADO',
 '2025-12-08 11:00:00', '2025-12-08', '2025-12-22', '2025-12-20', 1),

-- SRV2500004 | Claudia Suárez | Aire Acond. Carrier 53HVA1201 | FINALIZADO
(4, 'SRV2500004', 4, 4, 2, 'CLIENTE_TRAE', NULL, NULL,
 'El aire acondicionado no enfría correctamente. Sale aire tibio incluso en la máxima potencia de frío.',
 'Equipo desmontado previamente por el cliente.', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 32000.00, 'FINALIZADO',
 '2025-12-10 14:00:00', '2025-12-10', '2025-12-22', '2025-12-22', 1),

-- SRV2500005 | Rodrigo Villalba | Microondas Gafa G1755AFC | RECHAZADO
(5, 'SRV2500005', 5, 5, 3, 'CLIENTE_TRAE', NULL, NULL,
 'El microondas no calienta. La luz interior y el plato giratorio funcionan correctamente, pero no calienta.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'RECHAZADO',
 '2025-12-12 09:45:00', '2025-12-12', NULL, NULL, 1),

-- SRV2500006 | Patricia Castro | Heladera Whirlpool WRM45A | FINALIZADO
(6, 'SRV2500006', 6, 6, 2, 'CLIENTE_TRAE', NULL, NULL,
 'La heladera no mantiene temperatura en el freezer. El freezer descongela solo cada dos o tres días.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 19500.00, 'FINALIZADO',
 '2025-12-15 08:30:00', '2025-12-15', '2025-12-27', '2025-12-26', 1),

-- SRV2500007 | Marcelo Acosta | Lavarropas Drean Next 10.06 Eco | FINALIZADO
(7, 'SRV2500007', 7, 7, 3, 'CLIENTE_TRAE', NULL, NULL,
 'La lavarropas no desagua al finalizar el ciclo. Queda agua en el tambor. Además emite un ruido al centrifugar.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 22500.00, 'FINALIZADO',
 '2025-12-17 10:00:00', '2025-12-17', '2025-12-29', '2025-12-29', 1),

-- SRV2500008 | Silvana Moreno | Cocina Ariston LI660A | RECHAZADO
(8, 'SRV2500008', 8, 8, 2, 'CLIENTE_TRAE', NULL, NULL,
 'La cocina a gas no enciende. El sistema de encendido eléctrico no genera chispa en ninguna hornalla.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'RECHAZADO',
 '2025-12-19 15:20:00', '2025-12-19', NULL, NULL, 1);

-- =============================================================
-- SERVICIOS — ENERO 2026 (ids 9-18) + GTA2600001 (id 28)
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
-- SRV2600001 | Ignacio Pereyra | Lavarropas LG F1403RD | FINALIZADO
(9, 'SRV2600001', 9, 9, 2, 'CLIENTE_TRAE', NULL, NULL,
 'El lavarropas muestra el código de error E4 y no completa el ciclo de lavado. Se detiene en el medio del programa.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 67000.00, 'FINALIZADO',
 '2026-01-07 09:00:00', '2026-01-07', '2026-01-21', '2026-01-17', 1),

-- SRV2600002 | Lorena Quintero | Heladera Samsung RT38K | FINALIZADO
(10, 'SRV2600002', 10, 10, 2, 'CLIENTE_TRAE', NULL, NULL,
 'La heladera pierde frío y presenta acumulación de escarcha en el evaporador. Se escucha un silbido al funcionar.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 35000.00, 'FINALIZADO',
 '2026-01-09 11:30:00', '2026-01-09', '2026-01-23', '2026-01-21', 1),

-- SRV2600003 | Eduardo Barrionuevo | Televisor LG 55UQ8050 | FINALIZADO
(11, 'SRV2600003', 11, 11, 3, 'CLIENTE_TRAE', NULL, NULL,
 'El televisor presenta líneas horizontales oscuras en la pantalla y la imagen parpadea intermitentemente.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 32000.00, 'FINALIZADO',
 '2026-01-12 10:15:00', '2026-01-12', '2026-01-26', '2026-01-24', 1),

-- SRV2600004 | Natalia Correa | Aire Acond. Surrey 553IQV1201 | FINALIZADO
(12, 'SRV2600004', 12, 12, 2, 'CLIENTE_TRAE', NULL, NULL,
 'El equipo de aire acondicionado no enciende. El control remoto no lo activa y el botón del equipo tampoco.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 53000.00, 'FINALIZADO',
 '2026-01-14 14:30:00', '2026-01-14', '2026-01-28', '2026-01-27', 1),

-- SRV2600005 | Jorge Méndez | Freezer Electrolux DW50X6 | RECHAZADO
(13, 'SRV2600005', 1, 13, 3, 'CLIENTE_TRAE', NULL, NULL,
 'El freezer no enfría correctamente. Se acumula escarcha en la parte inferior y el compresor trabaja continuamente.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'RECHAZADO',
 '2026-01-16 09:30:00', '2026-01-16', NULL, NULL, 1),

-- SRV2600006 | Valentina Ramos | Microondas LG S4500VR | FINALIZADO
(14, 'SRV2600006', 2, 14, 2, 'CLIENTE_TRAE', NULL, NULL,
 'El microondas hace chispas en el interior cuando funciona y el plato dejó de girar.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 15000.00, 'FINALIZADO',
 '2026-01-19 10:45:00', '2026-01-19', '2026-02-02', '2026-01-30', 1),

-- SRV2600007 | Héctor Flores | Secarropas Whirlpool WCF80A | FINALIZADO
(15, 'SRV2600007', 3, 15, 3, 'CLIENTE_TRAE', NULL, NULL,
 'La secadora no calienta. El tambor gira con normalidad pero la ropa no se seca. Funciona solo como ventilador en frío.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 21500.00, 'FINALIZADO',
 '2026-01-21 11:00:00', '2026-01-21', '2026-02-04', '2026-02-03', 1),

-- SRV2600008 | Claudia Suárez | Cava de Vinos Surrey CW18 | TERMINADO (listo para retirar)
(16, 'SRV2600008', 4, 16, 2, 'CLIENTE_TRAE', NULL, NULL,
 'La cava de vinos emite un ruido constante y la temperatura interna no se mantiene estable.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'TERMINADO',
 '2026-01-24 09:00:00', '2026-01-24', '2026-02-07', '2026-02-10', 1),

-- SRV2600009 | Rodrigo Villalba | Horno Ariston FA5844C | RECHAZADO
(17, 'SRV2600009', 5, 17, 3, 'CLIENTE_TRAE', NULL, NULL,
 'El horno eléctrico no alcanza la temperatura indicada. La temperatura real es significativamente menor a la seleccionada.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, NULL, 'RECHAZADO',
 '2026-01-27 14:00:00', '2026-01-27', NULL, NULL, 1),

-- SRV2600010 | Patricia Castro | Calefactor Patrick HPK135M10 | FINALIZADO
(18, 'SRV2600010', 6, 18, 2, 'CLIENTE_TRAE', NULL, NULL,
 'El calefactor eléctrico enciende pero no genera calor. El ventilador funciona con normalidad pero sopla aire frío.',
 NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 0, 0.00, 20000.00, 'FINALIZADO',
 '2026-01-29 10:30:00', '2026-01-29', '2026-02-12', '2026-02-10', 1),

-- GTA2600001 | Jorge Méndez | Lavarropas Samsung WW90T | GARANTIA_SIN_REPARACION
-- Garantía del servicio SRV2500001 (id=1) — devuelto el 2025-12-11 (90 días = hasta 2026-03-11)
(28, 'GTA2600001', 1, 1, 1, 'CLIENTE_TRAE', NULL, NULL,
 'El lavarropas vuelve a hacer ruido similar al que motivó la reparación anterior hace un mes y medio.',
 'Ingreso por garantía del servicio SRV2500001.',
 1, 1, 1, 0,
 'Cliente reporta recurrencia del ruido. Tras evaluación técnica se determinó que no corresponde a la misma falla reparada anteriormente.',
 4, '2026-01-26 15:00:00',
 'Se revisó el equipo completamente. El rodamiento y el sello reemplazados se encuentran en perfecto estado. El ruido detectado corresponde a sobrecarga del tambor por exceso de ropa. Se instruyó al cliente sobre la capacidad máxima del equipo. La garantía no cubre errores de uso.',
 0, 0.00, NULL, 'GARANTIA_SIN_REPARACION',
 '2026-01-25 10:30:00', '2026-01-25', NULL, NULL, 1);

-- =============================================================
-- DETALLE DE SERVICIOS (checklist de componentes al ingreso)
-- Para SRV2500001, SRV2500002, SRV2500003
-- =============================================================
INSERT INTO detalle_servicios (id_detalle_servicio, id_servicio, componente, presente, comentario) VALUES
-- SRV2500001 — Lavarropas Samsung
(1,  1, 'Cable de alimentación',        1, NULL),
(2,  1, 'Manguera de entrada de agua',  1, NULL),
(3,  1, 'Manguera de desagüe',          1, NULL),
(4,  1, 'Tapa superior',                1, NULL),
-- SRV2500002 — Heladera LG
(5,  2, 'Cable de alimentación',        1, NULL),
(6,  2, 'Cajones y estantes interiores',1, NULL),
(7,  2, 'Bandeja colectora de agua',    0, 'No fue entregada por el cliente'),
-- SRV2500003 — Televisor Samsung
(8,  3, 'Cable de alimentación',        1, NULL),
(9,  3, 'Control remoto',               1, NULL),
(10, 3, 'Base/soporte de escritorio',   0, 'El cliente no entregó la base');

-- =============================================================
-- PRESUPUESTOS — DICIEMBRE 2025 (ids 1-8)
-- =============================================================
INSERT INTO presupuestos (
    id_presupuesto, numero_presupuesto, id_servicio, diagnostico, id_empleado,
    monto_repuestos_original, monto_repuestos_alternativo, mano_obra,
    monto_total_original, monto_total_alternativo,
    mostrar_original, mostrar_alternativo,
    tipo_confirmado, fecha_confirmacion, canal_confirmacion,
    estado, fecha_creacion, fecha_solicitud, fecha_pactada, fecha_vencimiento
) VALUES
-- PRE2500001 | SRV2500001 | Lavarropas Samsung | APROBADO
(1, 'PRE2500001', 1,
 'Se detectó rodamiento 6205 2RS con juego axial excesivo y sello de agua deteriorado que causaban vibración y ruido metálico durante el centrifugado. Se procede al reemplazo de ambas piezas y ajuste del tambor.',
 2, 8500.00, NULL, 18000.00, 26500.00, NULL, 1, 0,
 'ORIGINAL', '2025-12-04 11:00:00', 'PRESENCIAL',
 'APROBADO', '2025-12-02 09:30:00', NULL, NULL, '2025-12-16'),

-- PRE2500002 | SRV2500002 | Heladera LG | APROBADO
(2, 'PRE2500002', 2,
 'Compresor principal averiado con bobina en cortocircuito. Sistema de refrigeración con leve pérdida de gas R-600a. Se reemplaza el compresor Embraco EMT6144GK y se recarga el sistema con nitrógeno seco.',
 2, 35000.00, NULL, 25000.00, 60000.00, NULL, 1, 0,
 'ORIGINAL', '2025-12-07 14:30:00', 'WHATSAPP',
 'APROBADO', '2025-12-05 11:00:00', NULL, NULL, '2025-12-19'),

-- PRE2500003 | SRV2500003 | Televisor Samsung | APROBADO
(3, 'PRE2500003', 3,
 'Fuente de alimentación quemada por sobretensión de red. Módulo de retroiluminación LED con segmentos dañados. Se reemplazan ambas unidades y se verifica el panel principal.',
 3, 13500.00, NULL, 22000.00, 35500.00, NULL, 1, 0,
 'ORIGINAL', '2025-12-10 10:00:00', 'TELEFONO',
 'APROBADO', '2025-12-08 11:30:00', NULL, NULL, '2025-12-22'),

-- PRE2500004 | SRV2500004 | Aire Acond. Carrier | APROBADO
(4, 'PRE2500004', 4,
 'Sistema con carga de gas R-410A al 60% de la presión nominal por fuga en válvula Schrader. Filtro deshidratador saturado. Se localiza y repara la fuga, se reemplaza el filtro y se realiza recarga completa. Limpieza profunda de unidades interior y exterior.',
 2, 12000.00, NULL, 20000.00, 32000.00, NULL, 1, 0,
 'ORIGINAL', '2025-12-12 09:00:00', 'PRESENCIAL',
 'APROBADO', '2025-12-10 14:30:00', NULL, NULL, '2025-12-24'),

-- PRE2500005 | SRV2500005 | Microondas Gafa | RECHAZADO
(5, 'PRE2500005', 5,
 'Magnetrón 2M246 completamente quemado con filamento roto y cátodo dañado. El costo del repuesto original importado supera ampliamente el valor comercial del equipo. Se recomienda al cliente evaluar la reposición del aparato.',
 3, 33000.00, NULL, 12000.00, 45000.00, NULL, 1, 0,
 NULL, NULL, NULL,
 'RECHAZADO', '2025-12-12 10:15:00', NULL, NULL, '2025-12-26'),

-- PRE2500006 | SRV2500006 | Heladera Whirlpool | APROBADO
(6, 'PRE2500006', 6,
 'Termostato electrónico K59-L2140 defectuoso que no interrumpe el ciclo de frío correctamente. Condensador trasero con acumulación de suciedad que reduce la eficiencia de enfriamiento. Se reemplaza el termostato y se realiza limpieza integral del condensador.',
 2, 4500.00, NULL, 15000.00, 19500.00, NULL, 1, 0,
 'ORIGINAL', '2025-12-17 11:00:00', 'WHATSAPP',
 'APROBADO', '2025-12-15 09:00:00', NULL, NULL, '2025-12-29'),

-- PRE2500007 | SRV2500007 | Lavarropas Drean | APROBADO
(7, 'PRE2500007', 7,
 'Bomba de desagüe obstruida por cuerpo extraño (moneda de 10 pesos) alojado en el impulsor. Correa de transmisión 1270J4 con desgaste avanzado y fisuras. Se extrae el cuerpo extraño, se reemplaza la correa y se ajusta la tensión del motor.',
 3, 6500.00, NULL, 16000.00, 22500.00, NULL, 1, 0,
 'ORIGINAL', '2025-12-19 10:00:00', 'PRESENCIAL',
 'APROBADO', '2025-12-17 10:30:00', NULL, NULL, '2025-12-31'),

-- PRE2500008 | SRV2500008 | Cocina Ariston | RECHAZADO
(8, 'PRE2500008', 8,
 'Plaqueta de control electrónico defectuosa con microcontrolador dañado. El módulo de encendido piezoeléctrico no recibe señal de la placa. Repuesto de importación con stock limitado y tiempo de entrega extendido.',
 2, 26000.00, NULL, 12000.00, 38000.00, NULL, 1, 0,
 NULL, NULL, NULL,
 'RECHAZADO', '2025-12-19 15:45:00', NULL, NULL, '2026-01-02');

-- =============================================================
-- PRESUPUESTOS — ENERO 2026 (ids 9-18)
-- =============================================================
INSERT INTO presupuestos (
    id_presupuesto, numero_presupuesto, id_servicio, diagnostico, id_empleado,
    monto_repuestos_original, monto_repuestos_alternativo, mano_obra,
    monto_total_original, monto_total_alternativo,
    mostrar_original, mostrar_alternativo,
    tipo_confirmado, fecha_confirmacion, canal_confirmacion,
    estado, fecha_creacion, fecha_solicitud, fecha_pactada, fecha_vencimiento
) VALUES
-- PRE2600001 | SRV2600001 | Lavarropas LG | APROBADO
(9, 'PRE2600001', 9,
 'Motor de lavado quemado por sobrecarga eléctrica (bobinas en cortocircuito). Placa de control electrónica con componentes SMD dañados en la sección del driver del motor. Se reemplazan el motor completo y la placa de control principal.',
 2, 37000.00, NULL, 30000.00, 67000.00, NULL, 1, 0,
 'ORIGINAL', '2026-01-09 10:00:00', 'PRESENCIAL',
 'APROBADO', '2026-01-07 09:30:00', NULL, NULL, '2026-01-21'),

-- PRE2600002 | SRV2600002 | Heladera Samsung | APROBADO
(10, 'PRE2600002', 10,
 'Pérdida de gas refrigerante R-600a por microfisura en tubería del evaporador. Filtro deshidratador saturado por acumulación de humedad. Se localiza y suelda la fisura, se reemplaza el filtro secador y se recarga el sistema.',
 2, 13000.00, NULL, 22000.00, 35000.00, NULL, 1, 0,
 'ORIGINAL', '2026-01-11 16:00:00', 'TELEFONO',
 'APROBADO', '2026-01-09 12:00:00', NULL, NULL, '2026-01-23'),

-- PRE2600003 | SRV2600003 | Televisor LG | APROBADO
(11, 'PRE2600003', 11,
 'Tiras de retroiluminación LED con fallas en el 50% de los segmentos (LEDs quemados y abiertos). Driver de retroiluminación con tensión fuera de rango. Se reemplaza el conjunto completo de tiras LED y se ajusta el driver.',
 3, 14000.00, NULL, 18000.00, 32000.00, NULL, 1, 0,
 'ORIGINAL', '2026-01-14 11:00:00', 'WHATSAPP',
 'APROBADO', '2026-01-12 10:45:00', NULL, NULL, '2026-01-26'),

-- PRE2600004 | SRV2600004 | Aire Acond. Surrey | APROBADO
(12, 'PRE2600004', 12,
 'Placa de control del inverter dañada por descarga eléctrica atmosférica. El módulo de potencia presenta componentes SMD quemados en la sección de conmutación. Se reemplaza la placa de control completa.',
 2, 28000.00, NULL, 25000.00, 53000.00, NULL, 1, 0,
 'ORIGINAL', '2026-01-16 09:30:00', 'PRESENCIAL',
 'APROBADO', '2026-01-14 15:00:00', NULL, NULL, '2026-01-28'),

-- PRE2600005 | SRV2600005 | Freezer Electrolux | RECHAZADO
(13, 'PRE2600005', 13,
 'Compresor principal averiado con rotor bloqueado. Evaporador con daño severo por congelamiento. El costo de reparación supera el 70% del valor de reposición del equipo. Se recomienda evaluar la compra de un equipo nuevo.',
 3, 78000.00, NULL, 17000.00, 95000.00, NULL, 1, 0,
 NULL, NULL, NULL,
 'RECHAZADO', '2026-01-16 10:00:00', NULL, NULL, '2026-01-30'),

-- PRE2600006 | SRV2600006 | Microondas LG | APROBADO
(14, 'PRE2600006', 14,
 'Fusible de seguridad de cerámica 20 A quemado por cortocircuito en el diodo de alta tensión HV. Se reemplazan el fusible y el diodo, se verifica el magnetrón y la capacidad del condensador de alta tensión.',
 2, 3000.00, NULL, 12000.00, 15000.00, NULL, 1, 0,
 'ORIGINAL', '2026-01-21 14:00:00', 'TELEFONO',
 'APROBADO', '2026-01-19 11:15:00', NULL, NULL, '2026-02-02'),

-- PRE2600007 | SRV2600007 | Secarropas Whirlpool | APROBADO
(15, 'PRE2600007', 15,
 'Resistencia calefactora de 2000 W quemada (medición de continuidad: circuito abierto). Termostato de seguridad verificado en buen estado. Se reemplaza la resistencia y se realiza prueba de funcionamiento completa.',
 3, 7500.00, NULL, 14000.00, 21500.00, NULL, 1, 0,
 'ORIGINAL', '2026-01-23 10:00:00', 'PRESENCIAL',
 'APROBADO', '2026-01-21 11:30:00', NULL, NULL, '2026-02-04'),

-- PRE2600008 | SRV2600008 | Cava de Vinos Surrey | APROBADO
(16, 'PRE2600008', 16,
 'Compresor de mini-refrigeración averiado con pérdida de hermeticidad. Sistema de refrigeración limpio sin obstrucciones. Se reemplaza el compresor y se recarga el sistema con gas R-134a.',
 2, 18000.00, NULL, 20000.00, 38000.00, NULL, 1, 0,
 'ORIGINAL', '2026-01-26 15:00:00', 'WHATSAPP',
 'APROBADO', '2026-01-24 09:30:00', NULL, NULL, '2026-02-07'),

-- PRE2600009 | SRV2600009 | Horno Ariston | RECHAZADO
(17, 'PRE2600009', 17,
 'Elemento calefactor de horno quemado (rotura del filamento de resistencia). Termostato de control con deriva significativa en la medición. Repuestos originales con escasa disponibilidad en plaza y tiempo de importación de 30 días.',
 3, 19000.00, NULL, 13000.00, 32000.00, NULL, 1, 0,
 NULL, NULL, NULL,
 'RECHAZADO', '2026-01-27 14:30:00', NULL, NULL, '2026-02-10'),

-- PRE2600010 | SRV2600010 | Calefactor Patrick | APROBADO
(18, 'PRE2600010', 18,
 'Motor del ventilador bloqueado por acumulación de pelusa y polvo en el eje. Resistencia térmica con rotura detectada por medición de continuidad. Se desbloquea y limpia el motor, se reemplaza la resistencia y se realiza prueba de temperatura.',
 2, 8000.00, NULL, 12000.00, 20000.00, NULL, 1, 0,
 'ORIGINAL', '2026-01-31 11:00:00', 'PRESENCIAL',
 'APROBADO', '2026-01-29 11:00:00', NULL, NULL, '2026-02-12');

-- =============================================================
-- DETALLE DE PRESUPUESTOS — DICIEMBRE 2025 + ENERO 2026 (ids 1-28)
-- =============================================================
INSERT INTO detalle_presupuestos (id_detalle_presupuesto, id_presupuesto, item, cantidad, precio_original, precio_alternativo) VALUES
-- PRE2500001 (ids 1-2)
(1,  1, 'Rodamiento 6205 2RS',                           1, 3500.00, NULL),
(2,  1, 'Sello de agua para lavarropas',                 1, 5000.00, NULL),
-- PRE2500002 (id 3)
(3,  2, 'Compresor Embraco EMT6144GK',                   1, 35000.00, NULL),
-- PRE2500003 (ids 4-5)
(4,  3, 'Fuente de alimentación para TV Samsung',         1, 5500.00, NULL),
(5,  3, 'Módulo de retroiluminación LED 55"',             1, 8000.00, NULL),
-- PRE2500004 (ids 6-7)
(6,  4, 'Gas refrigerante R-410A (carga completa)',       1, 8000.00, NULL),
(7,  4, 'Filtro deshidratador',                          1, 4000.00, NULL),
-- PRE2500005 (id 8) — RECHAZADO
(8,  5, 'Magnetrón 2M246 (importado)',                   1, 33000.00, NULL),
-- PRE2500006 (id 9)
(9,  6, 'Termostato electrónico K59-L2140',              1, 4500.00, NULL),
-- PRE2500007 (ids 10-11)
(10, 7, 'Bomba de desagüe 1000 W',                       1, 4000.00, NULL),
(11, 7, 'Correa de transmisión 1270J4',                  1, 2500.00, NULL),
-- PRE2500008 (id 12) — RECHAZADO
(12, 8, 'Plaqueta de control electrónica (importada)',   1, 26000.00, NULL),
-- PRE2600001 (ids 13-14)
(13, 9, 'Motor de lavado 600 W',                         1, 15000.00, NULL),
(14, 9, 'Placa de control electrónica',                  1, 22000.00, NULL),
-- PRE2600002 (ids 15-16)
(15, 10, 'Gas refrigerante R-600a',                      1, 9500.00,  NULL),
(16, 10, 'Filtro secador deshidratador',                 1, 3500.00,  NULL),
-- PRE2600003 (id 17)
(17, 11, 'Kit tiras retroiluminación LED 55"',           1, 14000.00, NULL),
-- PRE2600004 (id 18)
(18, 12, 'Placa inverter unidad interior',               1, 28000.00, NULL),
-- PRE2600005 (ids 19-20) — RECHAZADO
(19, 13, 'Compresor para freezer vertical',              1, 58000.00, NULL),
(20, 13, 'Evaporador de repuesto',                       1, 20000.00, NULL),
-- PRE2600006 (ids 21-22)
(21, 14, 'Fusible de seguridad cerámica 20 A',           1, 800.00,   NULL),
(22, 14, 'Diodo de alta tensión HV (2CL69)',             1, 2200.00,  NULL),
-- PRE2600007 (id 23)
(23, 15, 'Resistencia calefactora 2000 W',               1, 7500.00,  NULL),
-- PRE2600008 (id 24)
(24, 16, 'Compresor mini-refrigeración R-134a',          1, 18000.00, NULL),
-- PRE2600009 (ids 25-26) — RECHAZADO
(25, 17, 'Elemento calefactor de horno eléctrico',       1, 11000.00, NULL),
(26, 17, 'Termostato de horno con bulbo capilar',        1, 8000.00,  NULL),
-- PRE2600010 (ids 27-28)
(27, 18, 'Motor ventilador calefactor 200 W',            1, 4800.00,  NULL),
(28, 18, 'Resistencia térmica eléctrica 2000 W',         1, 3200.00,  NULL);

-- =============================================================
-- ÓRDENES DE TRABAJO — DICIEMBRE 2025 (ids 1-6)
-- =============================================================
INSERT INTO ordenes_trabajo (
    id_orden_trabajo, numero_orden_trabajo, id_servicio, id_presupuesto, id_empleado,
    monto_total_repuestos, monto_extras, observaciones_extras, es_sin_costo,
    estado, fecha_creacion, fecha_comienzo, fecha_fin
) VALUES
-- OT2500001 | SRV2500001 | Lavarropas Samsung | respinola | TERMINADA
(1,  'OT2500001', 1,  1,  4, 8500.00,  0.00, NULL, 0, 'TERMINADA',
 '2025-12-04 11:30:00', '2025-12-04', '2025-12-11'),
-- OT2500002 | SRV2500002 | Heladera LG | kroldan | TERMINADA
(2,  'OT2500002', 2,  2,  8, 35000.00, 0.00, NULL, 0, 'TERMINADA',
 '2025-12-07 15:00:00', '2025-12-08', '2025-12-18'),
-- OT2500003 | SRV2500003 | Televisor Samsung | gperalta | TERMINADA
(3,  'OT2500003', 3,  3,  5, 13500.00, 0.00, NULL, 0, 'TERMINADA',
 '2025-12-10 10:30:00', '2025-12-10', '2025-12-20'),
-- OT2500004 | SRV2500004 | Aire Acond. Carrier | haguirre | TERMINADA
(4,  'OT2500004', 4,  4,  6, 12000.00, 0.00, NULL, 0, 'TERMINADA',
 '2025-12-12 09:30:00', '2025-12-12', '2025-12-22'),
-- OT2500005 | SRV2500006 | Heladera Whirlpool | kroldan | TERMINADA
(5,  'OT2500005', 6,  6,  8, 4500.00,  0.00, NULL, 0, 'TERMINADA',
 '2025-12-17 11:30:00', '2025-12-17', '2025-12-26'),
-- OT2500006 | SRV2500007 | Lavarropas Drean | respinola | TERMINADA
(6,  'OT2500006', 7,  7,  4, 6500.00,  0.00, NULL, 0, 'TERMINADA',
 '2025-12-19 10:30:00', '2025-12-19', '2025-12-29');

-- =============================================================
-- ÓRDENES DE TRABAJO — ENERO 2026 (ids 7-14)
-- =============================================================
INSERT INTO ordenes_trabajo (
    id_orden_trabajo, numero_orden_trabajo, id_servicio, id_presupuesto, id_empleado,
    monto_total_repuestos, monto_extras, observaciones_extras, es_sin_costo,
    estado, fecha_creacion, fecha_comienzo, fecha_fin
) VALUES
-- OT2600001 | SRV2600001 | Lavarropas LG | respinola | TERMINADA
(7,  'OT2600001', 9,  9,  4, 37000.00, 0.00, NULL, 0, 'TERMINADA',
 '2026-01-09 10:30:00', '2026-01-09', '2026-01-17'),
-- OT2600002 | SRV2600002 | Heladera Samsung | kroldan | TERMINADA
(8,  'OT2600002', 10, 10, 8, 13000.00, 0.00, NULL, 0, 'TERMINADA',
 '2026-01-11 16:30:00', '2026-01-12', '2026-01-21'),
-- OT2600003 | SRV2600003 | Televisor LG | gperalta | TERMINADA
(9,  'OT2600003', 11, 11, 5, 14000.00, 0.00, NULL, 0, 'TERMINADA',
 '2026-01-14 11:30:00', '2026-01-14', '2026-01-24'),
-- OT2600004 | SRV2600004 | Aire Acond. Surrey | haguirre | TERMINADA
(10, 'OT2600004', 12, 12, 6, 28000.00, 0.00, NULL, 0, 'TERMINADA',
 '2026-01-16 10:00:00', '2026-01-16', '2026-01-27'),
-- OT2600005 | SRV2600006 | Microondas LG | mcordoba | TERMINADA
(11, 'OT2600005', 14, 14, 7, 3000.00,  0.00, NULL, 0, 'TERMINADA',
 '2026-01-21 14:30:00', '2026-01-22', '2026-01-30'),
-- OT2600006 | SRV2600007 | Secarropas Whirlpool | respinola | TERMINADA
(12, 'OT2600006', 15, 15, 4, 7500.00,  0.00, NULL, 0, 'TERMINADA',
 '2026-01-23 10:30:00', '2026-01-23', '2026-02-03'),
-- OT2600007 | SRV2600008 | Cava de Vinos Surrey | kroldan | TERMINADA
(13, 'OT2600007', 16, 16, 8, 18000.00, 0.00, NULL, 0, 'TERMINADA',
 '2026-01-26 15:30:00', '2026-01-27', '2026-02-10'),
-- OT2600008 | SRV2600010 | Calefactor Patrick | mcordoba | TERMINADA
(14, 'OT2600008', 18, 18, 7, 8000.00,  0.00, NULL, 0, 'TERMINADA',
 '2026-01-31 11:30:00', '2026-02-01', '2026-02-10');

-- =============================================================
-- DETALLE DE ÓRDENES DE TRABAJO — DICIEMBRE 2025 + ENERO 2026
-- Los ítems se copian desde el presupuesto aprobado (texto libre)
-- =============================================================
INSERT INTO detalle_ordenes_trabajo (
    id_detalle_orden_trabajo, id_orden_trabajo, id_repuesto,
    item_descripcion, cantidad, comentario, completado
) VALUES
-- OT2500001 — Lavarropas Samsung (ids 1-2)
(1,  1,  NULL, 'Rodamiento 6205 2RS',                           1, NULL, 1),
(2,  1,  NULL, 'Sello de agua para lavarropas',                 1, NULL, 1),
-- OT2500002 — Heladera LG (id 3)
(3,  2,  NULL, 'Compresor Embraco EMT6144GK',                   1, NULL, 1),
-- OT2500003 — Televisor Samsung (ids 4-5)
(4,  3,  NULL, 'Fuente de alimentación para TV Samsung',         1, NULL, 1),
(5,  3,  NULL, 'Módulo de retroiluminación LED 55"',             1, NULL, 1),
-- OT2500004 — Aire Acond. Carrier (ids 6-7)
(6,  4,  NULL, 'Gas refrigerante R-410A (carga completa)',       1, NULL, 1),
(7,  4,  NULL, 'Filtro deshidratador',                          1, NULL, 1),
-- OT2500005 — Heladera Whirlpool (id 8)
(8,  5,  NULL, 'Termostato electrónico K59-L2140',              1, NULL, 1),
-- OT2500006 — Lavarropas Drean (ids 9-10)
(9,  6,  NULL, 'Bomba de desagüe 1000 W',                       1, NULL, 1),
(10, 6,  NULL, 'Correa de transmisión 1270J4',                  1, NULL, 1),
-- OT2600001 — Lavarropas LG (ids 11-12)
(11, 7,  NULL, 'Motor de lavado 600 W',                         1, NULL, 1),
(12, 7,  NULL, 'Placa de control electrónica',                  1, NULL, 1),
-- OT2600002 — Heladera Samsung (ids 13-14)
(13, 8,  NULL, 'Gas refrigerante R-600a',                       1, NULL, 1),
(14, 8,  NULL, 'Filtro secador deshidratador',                  1, NULL, 1),
-- OT2600003 — Televisor LG (id 15)
(15, 9,  NULL, 'Kit tiras retroiluminación LED 55"',            1, NULL, 1),
-- OT2600004 — Aire Acond. Surrey (id 16)
(16, 10, NULL, 'Placa inverter unidad interior',                1, NULL, 1),
-- OT2600005 — Microondas LG (ids 17-18)
(17, 11, NULL, 'Fusible de seguridad cerámica 20 A',            1, NULL, 1),
(18, 11, NULL, 'Diodo de alta tensión HV (2CL69)',              1, NULL, 1),
-- OT2600006 — Secarropas Whirlpool (id 19)
(19, 12, NULL, 'Resistencia calefactora 2000 W',                1, NULL, 1),
-- OT2600007 — Cava de Vinos Surrey (id 20)
(20, 13, NULL, 'Compresor mini-refrigeración R-134a',           1, NULL, 1),
-- OT2600008 — Calefactor Patrick (ids 21-22)
(21, 14, NULL, 'Motor ventilador calefactor 200 W',             1, NULL, 1),
(22, 14, NULL, 'Resistencia térmica eléctrica 2000 W',          1, NULL, 1);

-- =============================================================
-- FIN DEL ARCHIVO 2 — Continuar con demo_03_servicios_feb_garantias.sql
-- =============================================================
