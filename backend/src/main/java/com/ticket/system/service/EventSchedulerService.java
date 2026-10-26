package com.ticket.system.service;

import com.ticket.system.model.Event;
import com.ticket.system.repository.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

@Service
public class EventSchedulerService {

    private static final Logger logger = Logger.getLogger(EventSchedulerService.class.getName());
    
    // 赛事开始时间格式
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 停止售票的天数（赛事开始后X天停止售票）
    private static final int STOP_SALE_DAYS_AFTER_START = 2;

    private final EventRepository eventRepository;
    private final TotalSalesService totalSalesService;

    public EventSchedulerService(EventRepository eventRepository, TotalSalesService totalSalesService) {
        this.eventRepository = eventRepository;
        this.totalSalesService = totalSalesService;
    }

    /**
     * 每天凌晨1点执行自动停售检查
     * 当赛事开始时间超过2天后，自动将状态改为"停售"
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoStopSales() {
        logger.info("=== 开始执行赛事自动停售检查 ===");
        
        LocalDateTime now = LocalDateTime.now();
        logger.info("当前时间: " + now.format(DATE_TIME_FORMATTER));
        
        // 查询所有状态为"售票中"的赛事
        List<Event> events = eventRepository.findByStatus("售票中");
        logger.info("查询到售票中的赛事数量: " + events.size());
        
        int stoppedCount = 0;
        
        for (Event event : events) {
            try {
                LocalDateTime eventTime = LocalDateTime.parse(event.getTime(), DATE_TIME_FORMATTER);
                
                // 计算赛事开始时间与当前时间的天数差
                long daysSinceStart = Duration.between(eventTime, now).toDays();
                logger.info("赛事 [" + event.getName() + "] 开始时间: " + event.getTime() + ", 已过去天数: " + daysSinceStart);
                
                // 如果赛事开始时间已超过2天，停止售票
                if (daysSinceStart >= STOP_SALE_DAYS_AFTER_START) {
                    // 自动停售检查时保留座位状态
                    event.setStatus("停售");
                    eventRepository.save(event);
                    stoppedCount++;
                    logger.info("赛事 [" + event.getName() + "] 已自动停止售票，保留座位状态");
                }
            } catch (Exception e) {
                logger.warning("处理赛事 [" + event.getName() + "] 时出错: " + e.getMessage());
            }
        }
        
        logger.info("=== 自动停售检查完成，共停止 " + stoppedCount + " 个赛事的售票 ===");
    }
    
    /**
     * 每分钟执行一次销售数据更新
     * 确保销售数据实时同步
     */
    @Scheduled(cron = "0 * * * * ?")
    public void autoUpdateSalesData() {
        logger.info("=== 开始执行销售数据自动更新 ===");
        try {
            totalSalesService.updateAllSalesData();
            logger.info("=== 销售数据自动更新完成 ===");
        } catch (Exception e) {
            logger.warning("销售数据自动更新失败: " + e.getMessage());
        }
    }
    
}
