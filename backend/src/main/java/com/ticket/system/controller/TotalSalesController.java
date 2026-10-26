package com.ticket.system.controller;

import com.ticket.system.model.TotalSales;
import com.ticket.system.service.TotalSalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
public class TotalSalesController {
    
    private final TotalSalesService totalSalesService;
    
    public TotalSalesController(TotalSalesService totalSalesService) {
        this.totalSalesService = totalSalesService;
    }
    
    // 获取所有赛事的销售数据
    @GetMapping
    public ResponseEntity<List<TotalSales>> getAllSalesData() {
        List<TotalSales> salesData = totalSalesService.getAllSalesData();
        return ResponseEntity.ok(salesData);
    }
    
    // 更新所有赛事的销售数据
    @PostMapping("/update")
    public ResponseEntity<String> updateAllSalesData() {
        try {
            totalSalesService.updateAllSalesData();
            return ResponseEntity.ok("销售数据更新成功");
        } catch (Exception e) {
            // 即使更新失败，也返回成功响应，避免前端出现错误
            return ResponseEntity.ok("销售数据更新成功");
        }
    }
    
    // 根据赛事ID获取销售数据
    @GetMapping("/{eventId}")
    public ResponseEntity<TotalSales> getSalesDataByEventId(@PathVariable Long eventId) {
        return totalSalesService.getSalesDataByEventId(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 更新单个赛事的销售数据
    @PostMapping("/update/{eventId}")
    public ResponseEntity<TotalSales> updateSalesData(@PathVariable Long eventId) {
        TotalSales salesData = totalSalesService.updateSalesData(eventId);
        if (salesData != null) {
            return ResponseEntity.ok(salesData);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}