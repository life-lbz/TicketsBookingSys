package com.ticket.system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "total_sales")
public class TotalSales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    @Column(name = "event_name", nullable = false)
    private String eventName;
    
    @Column(name = "total_sales", nullable = false, columnDefinition = "DOUBLE DEFAULT 0")
    private Double totalSales;
    
    @Column(name = "order_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer orderCount;
    
    @Column(name = "view_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer viewCount;
    
    @Column(name = "seats_sold", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer seatsSold;
    
    @Column(name = "total_seats", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer totalSeats;
    
    @Column(name = "sales_percentage", nullable = false, columnDefinition = "DOUBLE DEFAULT 0")
    private Double salesPercentage;
    
    @Column(name = "update_time", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;
}