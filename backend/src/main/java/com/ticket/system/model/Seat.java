package com.ticket.system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Data
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    @Column(name = "row_num", nullable = false)
    private int rowNum;
    
    @Column(name = "col_num", nullable = false)
    private int colNum;
    
    @Column(name = "price", nullable = false)
    private double price;
    
    @Column(name = "status", nullable = false) // AVAILABLE, SOLD, LOCKED
    private String status;
    
    @Column(name = "locked_by")
    private Long lockedBy;
    
    @Column(name = "lock_time")
    private LocalDateTime lockTime;
    
    @Column(name = "lock_expire_time")
    private LocalDateTime lockExpireTime;
}