package com.ticket.system.controller;

import com.ticket.system.common.ApiResponse;
import com.ticket.system.model.AfterSales;
import com.ticket.system.model.Order;
import com.ticket.system.model.Seat;
import com.ticket.system.repository.AfterSalesRepository;
import com.ticket.system.repository.OrderRepository;
import com.ticket.system.repository.SeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/after-sales")
public class AfterSalesController {

    private final AfterSalesRepository afterSalesRepository;
    private final OrderRepository orderRepository;
    private final SeatRepository seatRepository;

    public AfterSalesController(AfterSalesRepository afterSalesRepository, 
                               OrderRepository orderRepository, 
                               SeatRepository seatRepository) {
        this.afterSalesRepository = afterSalesRepository;
        this.orderRepository = orderRepository;
        this.seatRepository = seatRepository;
    }

    // 获取所有售后请求
    @GetMapping
    public ResponseEntity<ApiResponse<List<AfterSales>>> getAllAfterSales() {
        List<AfterSales> afterSalesList = afterSalesRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(afterSalesList));
    }

    // 提交售后申请
    @PostMapping
    public ResponseEntity<AfterSales> createAfterSales(@RequestBody AfterSales afterSales) {
        afterSales.setStatus("待处理");
        afterSales.setCreateTime(LocalDateTime.now());
        AfterSales savedAfterSales = afterSalesRepository.save(afterSales);
        
        // 更新对应订单的状态为售后中
        if (afterSales.getOrderId() != null) {
            Order order = orderRepository.findById(afterSales.getOrderId())
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            order.setStatus("AFTER_SALES");
            orderRepository.save(order);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAfterSales);
    }

    // 更新售后状态
    @PutMapping("/{id}")
    public ResponseEntity<AfterSales> updateAfterSalesStatus(@PathVariable Long id, @RequestParam String status) {
        AfterSales afterSales = afterSalesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("售后请求不存在"));
        afterSales.setStatus(status);
        AfterSales updatedAfterSales = afterSalesRepository.save(afterSales);
        
        // 当售后处理完成后，更新对应订单的状态
        if (afterSales.getOrderId() != null) {
            Order order = orderRepository.findById(afterSales.getOrderId())
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            if (status.equals("已处理")) {
                // 售后处理完成，将订单状态更新为已完成
                order.setStatus("COMPLETED");
            } else if (status.equals("已拒绝")) {
                // 售后被拒绝，将订单状态恢复为已完成
                order.setStatus("COMPLETED");
            }
            orderRepository.save(order);
        }
        
        return ResponseEntity.ok(updatedAfterSales);
    }
    
    // 处理售后请求（退款或换票）
    @PutMapping("/{id}/process")
    public ResponseEntity<AfterSales> processAfterSales(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        AfterSales afterSales = afterSalesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("售后请求不存在"));
        
        String status = (String) request.get("status");
        String type = (String) request.get("type");
        
        afterSales.setStatus(status);
        AfterSales updatedAfterSales = afterSalesRepository.save(afterSales);
        
        // 当售后处理完成后，更新对应订单的状态并处理座位
        if (afterSales.getOrderId() != null) {
            Order order = orderRepository.findById(afterSales.getOrderId())
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            
            // 处理退款
            if (type.equals("退款")) {
                // 释放座位
                if (order.getSeats() != null) {
                    String[] seats = order.getSeats().split("、");
                    for (String seat : seats) {
                        // 解析座位字符串，如"3排16座"
                        try {
                            int rowNum = Integer.parseInt(seat.split("排")[0]);
                            int colNum = Integer.parseInt(seat.split("排")[1].split("座")[0]);
                            // 释放座位
                            List<Seat> seatList = seatRepository.findByEventIdAndRowNumAndColNum(order.getEventId(), rowNum, colNum);
                            for (Seat s : seatList) {
                                s.setStatus("AVAILABLE");
                                s.setLockedBy(null);
                                s.setLockTime(null);
                                s.setLockExpireTime(null);
                                seatRepository.save(s);
                            }
                        } catch (Exception e) {
                            // 解析失败，跳过该座位
                        }
                    }
                }
                // 将订单状态更新为已退款
                order.setStatus("REFUNDED");
            }
            
            orderRepository.save(order);
        }
        
        return ResponseEntity.ok(updatedAfterSales);
    }

    // 删除售后请求（取消售后）
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAfterSales(@PathVariable Long id) {
        AfterSales afterSales = afterSalesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("售后请求不存在"));
        
        // 删除售后请求
        afterSalesRepository.deleteById(id);
        
        // 更新对应订单的状态为已完成
        if (afterSales.getOrderId() != null && afterSales.getOrderId() != 0) {
            Order order = orderRepository.findById(afterSales.getOrderId())
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            order.setStatus("COMPLETED");
            orderRepository.save(order);
        }
        
        return ResponseEntity.noContent().build();
    }
}