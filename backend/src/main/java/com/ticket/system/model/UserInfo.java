package com.ticket.system.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_info")
@Data
public class UserInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "nickname")
    private String nickname;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "birthday")
    private LocalDate birthday;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "life_status")
    private String lifeStatus;
    
    @Column(name = "industry")
    private String industry;
    
    @Column(name = "hobbies")
    private String hobbies;
    
    @Column(name = "signature")
    private String signature;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "avatar", columnDefinition = "TEXT")
    private String avatar;
    
    @Column(name = "motions", columnDefinition = "TEXT")
    private String motions;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}