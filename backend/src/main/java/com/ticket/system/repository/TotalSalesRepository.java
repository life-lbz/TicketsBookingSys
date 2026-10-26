package com.ticket.system.repository;

import com.ticket.system.model.TotalSales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TotalSalesRepository extends JpaRepository<TotalSales, Long> {
    // 根据赛事ID查找销售信息
    Optional<TotalSales> findByEventId(Long eventId);
    
    // 获取所有赛事的销售信息，按销售额降序排序
    List<TotalSales> findAllByOrderByTotalSalesDesc();
}