-- =====================================================
-- PORTAL PÚBLICO - MySQL Schema
-- Tablas independientes del negocio académico
-- Compatible with MySQL 8.0+
-- =====================================================

-- =====================================================
-- 1. INSTITUTION (Información de la Institución)
-- =====================================================

CREATE TABLE IF NOT EXISTS institution (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(200),
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(150),
    website VARCHAR(200),
    mission TEXT,
    vision TEXT,
    history TEXT,
    `values` TEXT,
    logo_url TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. NEWS (Noticias del Portal)
-- =====================================================

CREATE TABLE IF NOT EXISTS news (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    title VARCHAR(200),
    content TEXT,
    image_url TEXT,
    is_published BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. EVENT (Eventos del Portal)
-- =====================================================

CREATE TABLE IF NOT EXISTS event (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    title VARCHAR(200),
    description TEXT,
    event_date DATE,
    location VARCHAR(200),
    is_published BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- 4. PORTAL ADVERTISEMENT (Banners/Publicidad)
-- =====================================================

CREATE TABLE IF NOT EXISTS portal_advertisement (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    image_url TEXT,
    link_url VARCHAR(500),
    position VARCHAR(20) DEFAULT 'BANNER' CHECK (position IN ('BANNER', 'SIDEBAR', 'FOOTER')),
    display_order INT DEFAULT 0,
    is_published BOOLEAN DEFAULT TRUE,
    start_date DATE,
    end_date DATE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- 5. PORTAL CONTACT (Mensajes de Contacto)
-- =====================================================

CREATE TABLE IF NOT EXISTS portal_contact (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_responded BOOLEAN DEFAULT FALSE,
    response TEXT,
    response_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES
-- =====================================================

CREATE INDEX idx_news_published ON news(is_published, is_deleted);
CREATE INDEX idx_news_created ON news(created_at);
CREATE INDEX idx_event_published ON event(is_published, is_deleted);
CREATE INDEX idx_event_date ON event(event_date);
CREATE INDEX idx_advertisement_published ON portal_advertisement(is_published, is_deleted);
CREATE INDEX idx_advertisement_position ON portal_advertisement(position, display_order);
CREATE INDEX idx_portal_contact_read ON portal_contact(is_read, is_responded);
CREATE INDEX idx_portal_contact_created ON portal_contact(created_at);
