package com.ticket.system.repository;

import com.ticket.system.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUsername(String username);
    List<Order> findByUserIdOrderByCreateTimeDesc(Long userId);
    List<Order> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, String status);
    List<Order> findByStatusAndExpireTimeBefore(String status, LocalDateTime expireTime);
    List<Order> findByEventIdAndStatus(Long eventId, String status);
}