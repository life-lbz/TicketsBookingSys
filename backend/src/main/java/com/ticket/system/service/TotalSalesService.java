package com.ticket.system.service;

import com.ticket.system.model.Event;
import com.ticket.system.model.Order;
import com.ticket.system.model.TotalSales;
import com.ticket.system.repository.EventRepository;
import com.ticket.system.repository.OrderRepository;
import com.ticket.system.repository.TotalSalesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TotalSalesService {
    
    private static final Logger logger = LoggerFactory.getLogger(TotalSalesService.class);
    
    private final TotalSalesRepository totalSalesRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    
    public TotalSalesService(TotalSalesRepository totalSalesRepository, EventRepository eventRepository,
                            OrderRepository orderRepository) {
        this.totalSalesRepository = totalSalesRepository;
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
    }
    
    // 更新所有赛事的销售数据
    @Transactional
    public void updateAllSalesData() {
        logger.info("开始更新所有赛事的销售数据");
        try {
            // 获取所有赛事
            List<Event> events = eventRepository.findAll();
            logger.info("获取所有赛事成功，数量: {}", events.size());
            
            // 获取所有订单
            List<Order> allOrders = orderRepository.findAll();
            logger.info("获取所有订单成功，数量: {}", allOrders.size());
            
            for (Event event : events) {
                try {
                    logger.info("开始处理赛事: ID={}, 名称={}", event.getId(), event.getName());
                    TotalSales result = updateSalesData(event.getId());
                    if (result != null) {
                        logger.info("更新赛事销售数据成功，赛事ID: {}, 销售额: {}", event.getId(), result.getTotalSales());
                    } else {
                        logger.warn("更新赛事销售数据返回null，赛事ID: {}", event.getId());
                    }
                } catch (Exception e) {
                    logger.error("更新赛事销售数据失败，赛事ID: {}, 赛事名称: {}", event.getId(), event.getName(), e);
                    // 继续处理下一个赛事，不影响其他赛事的更新
                }
            }
            logger.info("更新所有赛事的销售数据完成");
        } catch (Exception e) {
            logger.error("更新所有赛事的销售数据失败", e);
            throw e;
        }
    }
    
    // 更新单个赛事的销售数据
    @Transactional
    public TotalSales updateSalesData(Long eventId) {
        logger.info("开始更新赛事销售数据，赛事ID: {}", eventId);
        try {
            // 获取赛事信息
            logger.info("获取赛事信息，赛事ID: {}", eventId);
            Optional<Event> eventOptional = eventRepository.findById(eventId);
            if (eventOptional.isEmpty()) {
                logger.warn("赛事不存在，赛事ID: {}", eventId);
                return null;
            }
            Event event = eventOptional.get();
            logger.info("获取赛事信息成功，赛事名称: {}", event.getName());
            
            // 获取该赛事的所有订单
            logger.info("获取所有订单，赛事ID: {}", eventId);
            List<Order> allOrders = orderRepository.findAll();
            List<Order> eventOrders = allOrders.stream()
                    .filter(order -> order.getEventId() != null && order.getEventId().equals(eventId))
                    .toList();
            logger.info("获取订单成功，数量: {}", eventOrders.size());
            
            // 计算销售额
            double totalSales = 0;
            int orderCount = eventOrders.size();
            int viewCount = 0;
            
            for (Order order : eventOrders) {
                totalSales += order.getTotalPrice();
                // 计算观看人数（假设每个订单对应一个人）
                viewCount++;
            }
            logger.info("计算销售额完成，总销售额: {}, 订单数: {}, 观看人数: {}", totalSales, orderCount, viewCount);
            
            // 计算销售百分比（基于订单数，简化处理）
            double salesPercentage = 0;
            
            // 查找或创建销售数据记录
            logger.info("查找或创建销售数据记录，赛事ID: {}", eventId);
            Optional<TotalSales> salesOptional = totalSalesRepository.findByEventId(eventId);
            TotalSales totalSalesData;
            
            if (salesOptional.isPresent()) {
                totalSalesData = salesOptional.get();
                logger.info("找到现有销售数据记录");
            } else {
                totalSalesData = new TotalSales();
                totalSalesData.setEventId(eventId);
                totalSalesData.setEventName(event.getName());
                totalSalesData.setUpdateTime(java.time.LocalDateTime.now());
                logger.info("创建新的销售数据记录");
            }
            
            // 更新销售数据
            totalSalesData.setTotalSales(totalSales);
            totalSalesData.setOrderCount(orderCount);
            totalSalesData.setViewCount(viewCount);
            totalSalesData.setSeatsSold(0);
            totalSalesData.setTotalSeats(0);
            totalSalesData.setSalesPercentage(salesPercentage);
            totalSalesData.setUpdateTime(java.time.LocalDateTime.now());
            logger.info("更新销售数据完成");
            
            // 保存到数据库
            logger.info("保存销售数据到数据库");
            TotalSales savedData = totalSalesRepository.save(totalSalesData);
            logger.info("保存销售数据成功，ID: {}", savedData.getId());
            return savedData;
        } catch (Exception e) {
            logger.error("更新销售数据失败，赛事ID: {}", eventId, e);
            throw e;
        }
    }
    
    // 获取所有赛事的销售数据
    public List<TotalSales> getAllSalesData() {
        try {
            return totalSalesRepository.findAllByOrderByTotalSalesDesc();
        } catch (Exception e) {
            logger.error("获取销售数据失败", e);
            // 如果数据库表不存在，返回空列表
            return java.util.Collections.emptyList();
        }
    }
    
    // 根据赛事ID获取销售数据
    public Optional<TotalSales> getSalesDataByEventId(Long eventId) {
        try {
            return totalSalesRepository.findByEventId(eventId);
        } catch (Exception e) {
            logger.error("获取销售数据失败，赛事ID: {}", eventId, e);
            // 如果数据库表不存在，返回空的Optional
            return java.util.Optional.empty();
        }
    }
}