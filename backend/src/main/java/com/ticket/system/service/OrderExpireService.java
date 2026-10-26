package com.ticket.system.service;

import com.ticket.system.model.Order;
import com.ticket.system.model.Seat;
import com.ticket.system.repository.OrderRepository;
import com.ticket.system.repository.SeatRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OrderExpireService {

    private final OrderRepository orderRepository;
    private final SeatRepository seatRepository;

    public OrderExpireService(OrderRepository orderRepository, SeatRepository seatRepository) {
        this.orderRepository = orderRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * 每分钟检查一次过期的订单
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        
        // 查找所有待支付但已过期的订单
        List<Order> expiredOrders = orderRepository.findByStatusAndExpireTimeBefore("PENDING", now);
        
        for (Order order : expiredOrders) {
            // 设置订单状态为超时
            order.setStatus("TIMEOUT");
            orderRepository.save(order);
            
            // 释放座位
            releaseSeats(order);
        }
        
        if (!expiredOrders.isEmpty()) {
            System.out.println("取消了 " + expiredOrders.size() + " 个过期订单");
        }
    }
    
    /**
     * 释放订单关联的座位
     */
    private void releaseSeats(Order order) {
        // 解析座位信息，格式如："5排8座,5排9座"
        String seatsStr = order.getSeats();
        if (seatsStr == null || seatsStr.isEmpty()) {
            return;
        }
        
        Pattern pattern = Pattern.compile("(\\d+)排(\\d+)座");
        Matcher matcher = pattern.matcher(seatsStr);
        
        while (matcher.find()) {
            int rowNum = Integer.parseInt(matcher.group(1));
            int colNum = Integer.parseInt(matcher.group(2));
            
            // 查找对应的座位
            List<Seat> seats = seatRepository.findByEventId(order.getEventId());
            for (Seat seat : seats) {
                if (seat.getRowNum() == rowNum && seat.getColNum() == colNum && "SOLD".equals(seat.getStatus())) {
                    // 只有确认是已售状态才释放（避免已支付的订单被错误释放）
                    // 检查是否还有其他已支付的订单占用此座位
                    boolean hasOtherOrder = checkSeatHasOtherPaidOrder(order.getEventId(), rowNum, colNum, order.getId());
                    if (!hasOtherOrder) {
                        seat.setStatus("AVAILABLE");
                        seatRepository.save(seat);
                    }
                }
            }
        }
    }
    
    /**
     * 检查座位是否有其他已支付的订单
     */
    private boolean checkSeatHasOtherPaidOrder(Long eventId, int rowNum, int colNum, Long excludeOrderId) {
        // 简化处理：检查该赛事下是否有已完成的订单包含此座位
        List<Order> completedOrders = orderRepository.findByEventIdAndStatus(eventId, "COMPLETED");
        String seatStr = rowNum + "排" + colNum + "座";
        
        for (Order order : completedOrders) {
            if (!order.getId().equals(excludeOrderId) && order.getSeats() != null && order.getSeats().contains(seatStr)) {
                return true;
            }
        }
        return false;
    }
}
