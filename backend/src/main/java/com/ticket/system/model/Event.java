package com.ticket.system.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "events")
@Data
public class Event {
    @Id
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String time;
    
    @Column(nullable = false)
    private String venue;
    
    @Column(nullable = false)
    private double minPrice;
    
    private String cover;
    
    @Column(nullable = false)
    private boolean hot;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false, columnDefinition = "varchar(20) default '未发售'")
    private String status; // 状态：未发售、售票中、停售、售罄
}