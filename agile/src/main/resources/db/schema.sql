CREATE TABLE core_drive (
    code VARCHAR(32) PRIMARY KEY,
    title VARCHAR(128) NOT NULL
);

CREATE TABLE octant (
    code VARCHAR(64) PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    sign_x SMALLINT NOT NULL CHECK (sign_x IN (-1, 1)),
    sign_y SMALLINT NOT NULL CHECK (sign_y IN (-1, 1)),
    sign_z SMALLINT NOT NULL CHECK (sign_z IN (-1, 1)),
    UNIQUE (sign_x, sign_y, sign_z)
);

CREATE TABLE persona_archetype (
    code VARCHAR(64) PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    drive_code VARCHAR(32) NOT NULL REFERENCES core_drive (code),
    shadow_description VARCHAR(512) NOT NULL
);

CREATE TABLE narrative_archetype (
    code VARCHAR(64) PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    shadow_description VARCHAR(512) NOT NULL
);

CREATE TABLE symbol_category (
    code VARCHAR(64) PRIMARY KEY,
    title VARCHAR(128) NOT NULL
);

CREATE TABLE semantic_object (
    id VARCHAR(128) PRIMARY KEY,
    essence TEXT NOT NULL,
    x DOUBLE PRECISION NOT NULL CHECK (x BETWEEN -1.0 AND 1.0),
    y DOUBLE PRECISION NOT NULL CHECK (y BETWEEN -1.0 AND 1.0),
    z DOUBLE PRECISION NOT NULL CHECK (z BETWEEN -1.0 AND 1.0),
    drive_code VARCHAR(32) NOT NULL REFERENCES core_drive (code),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE symbol (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    category_code VARCHAR(64) NOT NULL REFERENCES symbol_category (code),
    x DOUBLE PRECISION NOT NULL CHECK (x BETWEEN -1.0 AND 1.0),
    y DOUBLE PRECISION NOT NULL CHECK (y BETWEEN -1.0 AND 1.0),
    z DOUBLE PRECISION NOT NULL CHECK (z BETWEEN -1.0 AND 1.0),
    UNIQUE (name, category_code)
);

CREATE TABLE analysis_result (
    object_id VARCHAR(128) PRIMARY KEY REFERENCES semantic_object (id) ON DELETE CASCADE,
    octant_code VARCHAR(64) NOT NULL REFERENCES octant (code),
    persona_code VARCHAR(64) NOT NULL REFERENCES persona_archetype (code),
    narrative_code VARCHAR(64) NOT NULL REFERENCES narrative_archetype (code),
    shadow_manifestation VARCHAR(512) NOT NULL,
    drive_code VARCHAR(32) NOT NULL REFERENCES core_drive (code),
    analyzed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quiz_session (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS'
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ABANDONED')),
    result_object_id VARCHAR(128) REFERENCES semantic_object (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ
);

CREATE TABLE quiz_answer (
    session_id BIGINT NOT NULL REFERENCES quiz_session (id) ON DELETE CASCADE,
    axis CHAR(1) NOT NULL CHECK (axis IN ('X', 'Y', 'Z')),
    question_index INT NOT NULL CHECK (question_index >= 0),
    answer_value INT NOT NULL CHECK (answer_value BETWEEN 0 AND 2),
    PRIMARY KEY (session_id, axis, question_index)
);

CREATE TABLE semantic_match (
    probe_object_id VARCHAR(128) REFERENCES semantic_object (id) ON DELETE CASCADE,
    candidate_object_id VARCHAR(128) REFERENCES semantic_object (id) ON DELETE CASCADE,
    distance DOUBLE PRECISION NOT NULL CHECK (distance >= 0.0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (probe_object_id, candidate_object_id),
    CHECK (probe_object_id <> candidate_object_id)
);

CREATE INDEX idx_semantic_object_position ON semantic_object (x, y, z);

CREATE INDEX idx_analysis_octant ON analysis_result (octant_code);

CREATE INDEX idx_analysis_persona ON analysis_result (persona_code);

CREATE INDEX idx_quiz_answer_axis ON quiz_answer (axis, answer_value);

INSERT INTO core_drive (code, title) VALUES
    ('STABILITY', 'Стабильность и контроль'),
    ('INDEPENDENCE', 'Независимость и истина'),
    ('TRANSFORMATION', 'Изменение и риск'),
    ('BELONGING', 'Принадлежность и связь');

INSERT INTO octant (code, title, sign_x, sign_y, sign_z) VALUES
    ('SEEKER_INNOVATOR', 'Искатель / Инноватор', -1, 1, 1),
    ('REBEL_PIONEER', 'Бунтарь / Первопроходец', -1, 1, -1),
    ('SAGE_ANALYST', 'Мудрец / Аналитик', -1, -1, 1),
    ('MASTER_PRAGMATIST', 'Мастер / Прагматик', -1, -1, -1),
    ('PROPHET_IDEOLOGUE', 'Пророк / Идеолог', 1, 1, 1),
    ('LEADER_REFORMER', 'Вождь / Реформатор', 1, 1, -1),
    ('GUARDIAN_LEADER', 'Хранитель / Духовный лидер', 1, -1, 1),
    ('CAREGIVER_EVERYMAN', 'Опекун / Обыватель', 1, -1, -1);

INSERT INTO persona_archetype (code, title, drive_code, shadow_description) VALUES
    ('HERO', 'Герой', 'TRANSFORMATION', 'Агрессор, гордец, не умеющий отступить'),
    ('SAGE', 'Мудрец', 'INDEPENDENCE', 'Догматик, оторванный от реальности теоретик'),
    ('RULER', 'Правитель', 'STABILITY', 'Тиран, бюрократ, контролер-параноик'),
    ('CREATOR', 'Творец', 'STABILITY', 'Перфекционист, созидание ради процесса'),
    ('REBEL', 'Бунтарь', 'TRANSFORMATION', 'Бессмысленное разрушение, авантюризм'),
    ('SEEKER', 'Искатель', 'INDEPENDENCE', 'Изоляция, эгоцентризм'),
    ('CAREGIVER', 'Опекун', 'BELONGING', 'Потеря индивидуальности, зависимость от чужого мнения'),
    ('JESTER', 'Шут', 'BELONGING', 'Цинизм, отсутствие долгосрочного видения');

INSERT INTO narrative_archetype (code, title, shadow_description) VALUES
    ('JOURNEY', 'Путь / Путешествие', 'Блуждание без цели'),
    ('DEATH_REBIRTH', 'Смерть и Возрождение', 'Зацикленность на разрушении'),
    ('ORDER_VS_CHAOS', 'Борьба Порядка и Хаоса', 'Тоталитарный контроль'),
    ('FALL', 'Падение', 'Саморазрушение, утрата смысла');

INSERT INTO symbol_category (code, title) VALUES
    ('PERSONA', 'Персона'),
    ('SHADOW', 'Тень'),
    ('ANIMA_ANIMUS', 'Анима и Анимус'),
    ('SELF', 'Самость'),
    ('WORLD_TREE', 'Мировое древо'),
    ('GREAT_MOTHER', 'Великая мать'),
    ('HERO', 'Герой-освободитель');