-- MineHub Velocity Plugin Database Schema
-- This file contains the database schema for the MineHub plugin

-- Create the database (run this manually before using the plugin)
-- CREATE DATABASE minehub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Server information table
CREATE TABLE IF NOT EXISTS minehub_servers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    owner_uuid VARCHAR(36) NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_online BOOLEAN DEFAULT FALSE,
    description TEXT,
    max_players INT DEFAULT 20,
    version VARCHAR(50) DEFAULT 'Unknown',

    -- Indexes for better performance
    INDEX idx_name (name),
    INDEX idx_owner_uuid (owner_uuid),
    INDEX idx_last_seen (last_seen),
    INDEX idx_is_online (is_online),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Player tracking table
CREATE TABLE IF NOT EXISTS minehub_players (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_server VARCHAR(255),
    join_count INT DEFAULT 1,

    -- Indexes for better performance
    INDEX idx_username (username),
    INDEX idx_last_join (last_join),
    INDEX idx_join_count (join_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional: Create example data
-- INSERT INTO minehub_servers (name, host, port, owner_uuid, owner_name, description, max_players) 
-- VALUES ('lobby', 'localhost', 25566, '00000000-0000-0000-0000-000000000000', 'System', 'Main lobby server', 100);
