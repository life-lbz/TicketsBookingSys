package com.ticket.system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "collections")
@Getter
@Setter
public class Collection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
}