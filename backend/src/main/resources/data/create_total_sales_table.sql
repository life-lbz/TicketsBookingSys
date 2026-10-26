-- 创建total_sales表，用于存储每个赛事的销售信息
CREATE TABLE IF NOT EXISTS total_sales (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    total_sales DOUBLE NOT NULL DEFAULT 0,
    order_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    seats_sold INT NOT NULL DEFAULT 0,
    total_seats INT NOT NULL DEFAULT 0,
    sales_percentage DOUBLE NOT NULL DEFAULT 0,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id)
);