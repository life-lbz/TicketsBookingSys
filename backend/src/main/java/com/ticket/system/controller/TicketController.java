package com.ticket.system.controller;

import com.ticket.system.common.ApiResponse;
import com.ticket.system.model.Event;
import com.ticket.system.model.Order;
import com.ticket.system.model.Seat;
import com.ticket.system.model.User;
import com.ticket.system.repository.EventRepository;
import com.ticket.system.repository.OrderRepository;
import com.ticket.system.repository.SeatRepository;
import com.ticket.system.repository.UserRepository;
import com.ticket.system.service.TotalSalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class TicketController {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TotalSalesService totalSalesService;

    public TicketController(EventRepository eventRepository, SeatRepository seatRepository,
                           OrderRepository orderRepository, UserRepository userRepository,
                           TotalSalesService totalSalesService) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.totalSalesService = totalSalesService;
    }

    @GetMapping("/events/{id}/seats")
    public ResponseEntity<ApiResponse<List<Seat>>> getSeatsByEvent(@PathVariable Long id) {
        List<Seat> seats = seatRepository.findByEventId(id);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        order.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCreateTime(LocalDateTime.now());
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.success("订单创建成功", savedOrder));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderCount() {
        long count = orderRepository.count();
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrdersByStatus(@PathVariable Long userId, @PathVariable String status) {
        List<Order> orders = orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(ApiResponse.success(order)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<String>> payOrder(@PathVariable Long id, @RequestBody Map<String, String> paymentInfo) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOptional.get();
        if (!"PENDING".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "订单状态不正确，无法支付"));
        }
        order.setStatus("COMPLETED");
        order.setPayTime(LocalDateTime.now());
        order.setPaymentMethod(paymentInfo.get("paymentMethod"));
        orderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.success("支付成功"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(@PathVariable Long id) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOptional.get();
        if (!"PENDING".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "订单状态不正确，无法取消"));
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.success("订单已取消"));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<String>> refundOrder(@PathVariable Long id) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOptional.get();
        if (!"COMPLETED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "只有已完成的订单才能申请退票"));
        }
        return ResponseEntity.ok(ApiResponse.success("退票申请已提交"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteOrder(@PathVariable Long id) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        orderRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("订单已删除"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOptional.get();
        order.setStatus(request.get("status"));
        orderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.success("订单状态已更新"));
    }

    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserOrderStats(@PathVariable Long userId) {
        List<Order> allOrders = orderRepository.findByUserIdOrderByCreateTimeDesc(userId);

        long pendingCount = allOrders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        long completedCount = allOrders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
        long cancelledCount = allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allOrders.size());
        stats.put("pending", pendingCount);
        stats.put("completed", completedCount);
        stats.put("cancelled", cancelledCount);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PutMapping("/seats/{id}")
    public ResponseEntity<ApiResponse<Seat>> updateSeat(@PathVariable Long id, @RequestBody Seat seat) {
        Seat existingSeat = seatRepository.findById(id).orElseThrow();
        existingSeat.setPrice(seat.getPrice());
        existingSeat.setStatus(seat.getStatus());
        Seat savedSeat = seatRepository.save(existingSeat);
        return ResponseEntity.ok(ApiResponse.success(savedSeat));
    }

    @DeleteMapping("/seats/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSeat(@PathVariable Long id) {
        seatRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("座位已删除"));
    }

    @GetMapping("/seats")
    public ResponseEntity<ApiResponse<List<Seat>>> getAllSeats() {
        List<Seat> seats = seatRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @PostMapping("/seats/{id}/lock")
    public ResponseEntity<ApiResponse<Object>> lockSeat(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Optional<Seat> seatOptional = seatRepository.findById(id);

        if (seatOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Seat seat = seatOptional.get();
        
        Optional<Event> eventOptional = eventRepository.findById(seat.getEventId());
        if (eventOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "赛事不存在"));
        }
        Event event = eventOptional.get();
        if ("停售".equals(event.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "该赛事已停止售票，无法选择座位"));
        }

        if ("SOLD".equals(seat.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "座位已售出"));
        }

        if ("LOCKED".equals(seat.getStatus())) {
            if (seat.getLockExpireTime() != null && seat.getLockExpireTime().isAfter(LocalDateTime.now())) {
                if (userId.equals(seat.getLockedBy())) {
                    seat.setLockTime(LocalDateTime.now());
                    seat.setLockExpireTime(LocalDateTime.now().plusMinutes(15));
                    seatRepository.save(seat);
                    return ResponseEntity.ok(ApiResponse.success(seat));
                }
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "座位已被其他用户锁定"));
            }
        }

        seat.setStatus("LOCKED");
        seat.setLockedBy(userId);
        seat.setLockTime(LocalDateTime.now());
        seat.setLockExpireTime(LocalDateTime.now().plusMinutes(15));
        seatRepository.save(seat);

        return ResponseEntity.ok(ApiResponse.success(seat));
    }

    @PostMapping("/seats/{id}/unlock")
    public ResponseEntity<ApiResponse<Seat>> unlockSeat(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Optional<Seat> seatOptional = seatRepository.findById(id);

        if (seatOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Seat seat = seatOptional.get();

        if ("LOCKED".equals(seat.getStatus()) && userId.equals(seat.getLockedBy())) {
            seat.setStatus("AVAILABLE");
            seat.setLockedBy(null);
            seat.setLockTime(null);
            seat.setLockExpireTime(null);
            seatRepository.save(seat);
            return ResponseEntity.ok(ApiResponse.success(seat));
        }

        return ResponseEntity.badRequest().body(ApiResponse.error(400, "无法解锁该座位"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Order>> confirmSeats(@RequestBody Map<String, Object> request) {
        Long userId = ((Number) request.get("userId")).longValue();
        Long eventId = ((Number) request.get("eventId")).longValue();
        
        Object seatIdsObj = request.get("seatIds");
        if (!(seatIdsObj instanceof List)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "seatIds必须是列表类型"));
        }
        List<Integer> seatIds = ((List<?>) seatIdsObj).stream()
                .map(obj -> ((Number) obj).intValue())
                .toList();

        if (seatIds.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "请选择至少一个座位"));
        }

        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "赛事不存在"));
        }
        Event event = eventOptional.get();

        if (!"售票中".equals(event.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "该赛事当前状态为[" + event.getStatus() + "]，无法购票"));
        }

        try {
            LocalDateTime eventTime = parseEventTime(event.getTime());
            long daysSinceStart = Duration.between(eventTime, LocalDateTime.now()).toDays();
            if (daysSinceStart >= 2) {
                event.setStatus("停售");
                eventRepository.save(event);
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "该赛事已超过开始时间2天，已停止售票"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "赛事时间格式错误: " + event.getTime()));
        }

        double totalAmount = 0;
        StringBuilder seatNames = new StringBuilder();

        for (Integer seatId : seatIds) {
            Optional<Seat> seatOptional = seatRepository.findById(seatId.longValue());
            if (seatOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "座位不存在: " + seatId));
            }

            Seat seat = seatOptional.get();

            if (!"LOCKED".equals(seat.getStatus()) || !userId.equals(seat.getLockedBy())) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "座位状态已变更，请重新选择: " + seat.getRowNum() + "排" + seat.getColNum() + "座"));
            }

            if (seat.getLockExpireTime() == null || seat.getLockExpireTime().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "座位锁定已过期，请重新选择: " + seat.getRowNum() + "排" + seat.getColNum() + "座"));
            }

            totalAmount += seat.getPrice();
            if (!seatNames.isEmpty()) {
                seatNames.append(", ");
            }
            seatNames.append(seat.getRowNum()).append("排").append(seat.getColNum()).append("座");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "用户不存在"));
        }
        User user = userOptional.get();

        Order order = new Order();
        order.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setUsername(user.getUsername());
        order.setEventId(eventId);
        order.setEventName(event.getName());
        order.setVenue(event.getVenue());
        order.setSeats(seatNames.toString());
        order.setTotalPrice(totalAmount);
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(15));

        Order savedOrder = orderRepository.save(order);

        for (Integer seatId : seatIds) {
            seatRepository.findById(seatId.longValue()).ifPresent(seat -> {
                seat.setStatus("SOLD");
                seat.setLockedBy(null);
                seat.setLockTime(null);
                seat.setLockExpireTime(null);
                seatRepository.save(seat);
            });
        }

        // 异步更新销售数据
        try {
            totalSalesService.updateSalesData(eventId);
        } catch (Exception e) {
            // 销售数据更新失败不影响订单创建
        }

        return ResponseEntity.ok(ApiResponse.success("订单创建成功", savedOrder));
    }

    private LocalDateTime parseEventTime(String timeStr) throws Exception {
        String[] formats = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "yyyy.MM.dd-HH:mm"
        };
        
        for (String format : formats) {
            try {
                return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException e) {
                // 尝试下一个格式
            }
        }
        
        throw new Exception("无法解析时间格式: " + timeStr);
    }
}
