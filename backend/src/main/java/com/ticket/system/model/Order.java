package com.ticket.system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ticket_order") // 'ORDER' is a reserved keyword in SQL
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderNo; // 订单号，如：ORD202403180001
    
    @Column(nullable = false)
    private Long userId; // 用户ID，关联users表
    
    @Column(nullable = false)
    private String username; // 用户名
    
    @Column(nullable = false)
    private Long eventId; // 赛事ID，关联events表
    
    @Column(nullable = false)
    private String eventName; // 赛事名称
    
    @Column(nullable = false)
    private String venue; // 场馆
    
    @Column(nullable = false)
    private String seats; // 座位信息，如："5排8座,5排9座"
    
    @Column(nullable = false)
    private Double totalPrice; // 订单总价
    
    @Column(nullable = false)
    private String status; // 订单状态：PENDING(待支付), COMPLETED(已完成), CANCELLED(已取消), TIMEOUT(超时), AFTER_SALES(售后中)
    
    @Column(nullable = false)
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime payTime; // 支付时间
    
    private LocalDateTime expireTime; // 订单过期时间（15分钟支付期限）
    
    private String paymentMethod; // 支付方式
    
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (orderNo == null) {
            orderNo = generateOrderNo();
        }
    }
    
    private String generateOrderNo() {
        // 生成订单号：ORD + 年月日 + 6位随机数
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "ORD" + dateStr + randomStr;
    }
}
