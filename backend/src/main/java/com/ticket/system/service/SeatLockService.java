package com.ticket.system.service;

import com.ticket.system.model.Seat;
import com.ticket.system.repository.SeatRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatLockService {

    private final SeatRepository seatRepository;

    public SeatLockService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    /**
     * 每30秒检查一次过期的座位锁定
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void releaseExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();
        
        // 查找所有锁定已过期但状态仍为LOCKED的座位
        List<Seat> expiredSeats = seatRepository.findByStatusAndLockExpireTimeBefore("LOCKED", now);
        
        for (Seat seat : expiredSeats) {
            seat.setStatus("AVAILABLE");
            seat.setLockedBy(null);
            seat.setLockTime(null);
            seat.setLockExpireTime(null);
            seatRepository.save(seat);
        }
        
        if (!expiredSeats.isEmpty()) {
            System.out.println("释放了 " + expiredSeats.size() + " 个过期锁定的座位");
        }
    }
}
