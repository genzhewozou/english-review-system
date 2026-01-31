-- MySQL Database Setup Script for English Learning System
-- Run this script to set up the database and user for production

-- Create database
CREATE DATABASE IF NOT EXISTS english_learning_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create user for the application (optional, for production security)
CREATE USER IF NOT EXISTS 'english_learning_user'@'localhost' IDENTIFIED BY 'secure_password';
CREATE USER IF NOT EXISTS 'english_learning_user'@'%' IDENTIFIED BY 'secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON english_learning_db.* TO 'english_learning_user'@'localhost';
GRANT ALL PRIVILEGES ON english_learning_db.* TO 'english_learning_user'@'%';

-- Flush privileges
FLUSH PRIVILEGES;

-- Use the database
USE english_learning_db;

-- Create indexes for better performance (will be created by JPA, but listed here for reference)
-- These will be automatically created by Hibernate based on entity annotations

-- Note: The actual table creation will be handled by Hibernate DDL
-- This script is primarily for database and user setup