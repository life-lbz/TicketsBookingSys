CREATE DATABASE IF NOT EXISTS ticket_system;

USE ticket_system;

CREATE TABLE IF NOT EXISTS after_sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    event_id INT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    type ENUM('退票', '换票') NOT NULL,
    description TEXT NOT NULL,
    status ENUM('待处理', '已处理', '已拒绝') DEFAULT '待处理',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id)
);

-- 查看表结构
DESCRIBE after_sales;