-- =====================================================
-- SEED DATA - MySQL Portal Público
-- Datos iniciales para el portal público
-- Compatible with MySQL 8.0+
-- =====================================================

-- =====================================================
-- 1. INSTITUTION DATA
-- =====================================================

INSERT INTO institution (name, address, phone, email, website, mission, vision, history, `values`, logo_url, is_active)
VALUES (
    'Escuela Normal Emiliano Zapata',
    'Av. principal S/N, Col. Centro',
    '+52 999 999 9999',
    'contacto@enez.edu.mx',
    'https://www.enez.edu.mx',
    'Formar profesionales de la educación con excelencia académica, compromiso social y valores éticos.',
    'Ser una institución líder en la formación de educadores de calidad, contribuyendo al desarrollo integral de la sociedad.',
    'Institución de educación superior comprometida con la excelencia académica desde su fundación.',
    'Integridad, Excelencia, Compromiso, Innovación, Servicio.',
    NULL,
    TRUE
);

-- =====================================================
-- 2. NEWS
-- =====================================================

INSERT INTO news (title, content, image_url, is_published, is_deleted) VALUES
('Inicio del Semestre 2025-1', 'Se da inicio oficial al nuevo semestre académico. Bienvenidos estudiantes.', NULL, TRUE, FALSE),
('Convocatoria de Inscripción 2025', 'Inscripciones abiertas para nuevo ingreso. Consulta los requisitos.', NULL, TRUE, FALSE),
('Premio a la Excelencia Académica', 'Convocatoria para estudiantes con promedio mayor a 90.', NULL, TRUE, FALSE),
('Semana de la Educación 2025', 'Evento académico con conferencias y talleres pedagógicos.', NULL, TRUE, FALSE);

-- =====================================================
-- 3. EVENTS
-- =====================================================

INSERT INTO event (title, description, event_date, location, is_published, is_deleted) VALUES
('Semana Pedagógica', 'Evento académico con conferencias y talleres sobre metodología educativa.', '2025-03-15', 'Auditorio Principal', TRUE, FALSE),
('Feria de Proyectos', 'Presentaciones de proyectos finales de semestre.', '2025-06-10', 'Plantel Central', TRUE, FALSE),
('Conferencia: Futuro de la Educación', 'Charla con especialistas en educación.', '2025-04-20', 'Sala de Videoconferencias', TRUE, FALSE),
('Congreso de Educación Normal', 'Encuentro de escuelas normales de la región.', '2025-05-15', 'Auditorio Principal', TRUE, FALSE);

-- =====================================================
-- 4. PORTAL ADVERTISEMENTS
-- =====================================================

INSERT INTO portal_advertisement (title, description, image_url, link_url, position, display_order, is_published, start_date, end_date, is_deleted) VALUES 
('Admisiones 2025', 'Inscripciones abiertas para nuevo ingreso', '/images/banners/admision2025.jpg', '/admisiones', 'BANNER', 1, TRUE, '2025-01-01', '2025-03-31', FALSE),
('Becas Disponibles', 'Programas de beca para estudiantes', '/images/banners/becas.jpg', '/becas', 'SIDEBAR', 1, TRUE, NULL, NULL, FALSE);

SELECT 'Seed del portal público insertado correctamente' AS resultado;
