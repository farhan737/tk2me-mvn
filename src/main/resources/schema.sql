-- Drop tables if they exist
DROP TABLE IF EXISTS users_friends;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS friend_requests;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL
);

-- Create friend_requests table
CREATE TABLE IF NOT EXISTS friend_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT,
    receiver_id BIGINT,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT,
    receiver_id BIGINT,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- Create users_friends join table
CREATE TABLE IF NOT EXISTS users_friends (
    user_id BIGINT,
    friends_id BIGINT,
    PRIMARY KEY (user_id, friends_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (friends_id) REFERENCES users(id)
);
