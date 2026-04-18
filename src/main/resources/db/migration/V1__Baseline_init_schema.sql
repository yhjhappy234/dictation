-- Flyway Baseline Migration
-- This file represents the baseline schema for existing database
-- Flyway will baseline at this version and not execute the SQL

-- User table
CREATE TABLE IF NOT EXISTS user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    status VARCHAR(255) NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Dictation Batch table
CREATE TABLE IF NOT EXISTS dictation_batch (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    batch_name VARCHAR(255) NOT NULL,
    total_words INTEGER NOT NULL,
    completed_words INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL CHECK (status IN ('CREATED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL
);

-- Word table
CREATE TABLE IF NOT EXISTS word (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_text VARCHAR(255) NOT NULL,
    pinyin VARCHAR(255),
    sort_order INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL CHECK (status IN ('PENDING', 'PLAYING', 'COMPLETED', 'SKIPPED')),
    batch_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Dictation Record table
CREATE TABLE IF NOT EXISTS dictation_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL CHECK (status IN ('STARTED', 'COMPLETED', 'SKIPPED')),
    repeat_count INTEGER NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_seconds INTEGER
);

-- Suggestion table
CREATE TABLE IF NOT EXISTS suggestion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_id BIGINT NOT NULL,
    suggestion_type VARCHAR(255) NOT NULL CHECK (suggestion_type IN ('REVIEW_NEEDED', 'HIGH_DIFFICULTY', 'FREQUENT_ERROR', 'LONG_DURATION', 'NEW_WORD')),
    priority INTEGER NOT NULL,
    message VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

-- Dictation Task table
CREATE TABLE IF NOT EXISTS dictation_task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_name VARCHAR(255) NOT NULL,
    words VARCHAR(255) NOT NULL,
    word_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    current_index INTEGER DEFAULT 0,
    correct_count INTEGER DEFAULT 0,
    wrong_count INTEGER DEFAULT 0,
    is_favorite BOOLEAN,
    dictator VARCHAR(50),
    created_at TIMESTAMP NOT NULL
);

-- Task Record table
CREATE TABLE IF NOT EXISTS task_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    word VARCHAR(255) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    error_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    dictator VARCHAR(50),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    read_count INTEGER
);

-- Difficult Word table
CREATE TABLE IF NOT EXISTS difficult_word (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_text TEXT NOT NULL UNIQUE,
    error_count INTEGER NOT NULL DEFAULT 0,
    dictator TEXT,
    avg_duration_seconds INTEGER,
    last_practice_date TIMESTAMP,
    mastery_level INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Audit Log table
CREATE TABLE IF NOT EXISTS audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id BIGINT,
    username VARCHAR(255),
    operation VARCHAR(255) NOT NULL,
    method VARCHAR(255),
    params VARCHAR(2000),
    result VARCHAR(2000),
    success BOOLEAN,
    ip_address VARCHAR(255),
    duration_ms BIGINT,
    error_message VARCHAR(1000),
    timestamp TIMESTAMP NOT NULL
);