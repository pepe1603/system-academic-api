-- =====================================================
-- RESET SCHEMA PUBLIC
-- Elimina y recrea el schema public completo
-- Ejecutar en PostgreSQL (DataGrip/Consola)
-- =====================================================

-- Eliminar schema public con todas sus dependencias
DROP SCHEMA public CASCADE;

-- Recrear schema público
CREATE SCHEMA public;

-- Garantizar permisos
GRANT ALL ON SCHEMA public TO public;
GRANT ALL ON SCHEMA public TO avnadmin;

SELECT 'Schema public reseteado correctamente' AS resultado;
