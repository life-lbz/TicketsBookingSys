package com.ticket.system.controller;

import com.ticket.system.common.ApiResponse;
import com.ticket.system.model.Event;
import com.ticket.system.model.Seat;
import com.ticket.system.repository.EventRepository;
import com.ticket.system.repository.SeatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/seats")
public class SeatController {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;

    public SeatController(SeatRepository seatRepository, EventRepository eventRepository) {
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<ApiResponse<List<Seat>>> getSeatsByEvent(@PathVariable Long id) {
        List<Seat> seats = seatRepository.findByEventId(id);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    // 兼容前端调用的路径
    @GetMapping("/events/{id}/seats")
    public ResponseEntity<ApiResponse<List<Seat>>> getSeatsByEventAlternative(@PathVariable Long id) {
        return getSeatsByEvent(id);
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Seat>>> getAllSeats() {
        List<Seat> seats = seatRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Seat>> updateSeat(@PathVariable Long id, @RequestBody Seat seat) {
        Seat existingSeat = seatRepository.findById(id).orElseThrow();
        existingSeat.setPrice(seat.getPrice());
        existingSeat.setStatus(seat.getStatus());
        Seat savedSeat = seatRepository.save(existingSeat);
        return ResponseEntity.ok(ApiResponse.success(savedSeat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSeat(@PathVariable Long id) {
        seatRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("座位已删除"));
    }

    @PostMapping("/{id}/lock")
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

    @PostMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Seat>> unlockSeat(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Optional<Seat> seatOptional = seatRepository.findById(id);

        if (seatOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Seat seat = seatOptional.get();

        if (!"LOCKED".equals(seat.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "座位未被锁定"));
        }

        if (seat.getLockedBy() != null && !seat.getLockedBy().equals(userId)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "只能解锁自己锁定的座位"));
        }

        seat.setStatus("AVAILABLE");
        seat.setLockedBy(null);
        seat.setLockTime(null);
        seat.setLockExpireTime(null);
        seatRepository.save(seat);

        return ResponseEntity.ok(ApiResponse.success(seat));
    }
}