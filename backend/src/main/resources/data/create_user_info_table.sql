-- 创建user_info表
CREATE TABLE IF NOT EXISTS user_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(255),
    gender VARCHAR(10),
    birthday DATE,
    city VARCHAR(255),
    life_status VARCHAR(255),
    industry VARCHAR(255),
    hobbies VARCHAR(255),
    signature VARCHAR(255),
    phone VARCHAR(20),
    avatar TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES users(username)
);