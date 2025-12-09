-- =====================================================
-- Migración: Agregar campo tipo_precio a presupuesto_tokens
-- Fecha: 2025-12-09
-- Descripción: Permite guardar qué tipo de precio (ORIGINAL/ALTERNATIVO)
--              fue seleccionado al aprobar un presupuesto
-- =====================================================

-- Agregar columna tipo_precio a la tabla presupuesto_tokens
ALTER TABLE presupuesto_tokens
ADD COLUMN tipo_precio VARCHAR(20) NULL;

-- Agregar comentario a la columna
COMMENT ON COLUMN presupuesto_tokens.tipo_precio IS 'Tipo de precio seleccionado al aprobar: ORIGINAL o ALTERNATIVO';

-- Verificar que la columna se creó correctamente
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_name = 'presupuesto_tokens' AND column_name = 'tipo_precio';

-- =====================================================
-- Nota: Si usas JPA/Hibernate con update automático,
-- esta migración se aplicará automáticamente al iniciar
-- la aplicación después de modificar la entidad.
-- =====================================================