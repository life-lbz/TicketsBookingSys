package com.ticket.system.repository;

import com.ticket.system.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventId(Long eventId);
    List<Seat> findByStatusAndLockExpireTimeBefore(String status, java.time.LocalDateTime lockExpireTime);
    List<Seat> findByEventIdAndRowNumAndColNum(Long eventId, int rowNum, int colNum);
}