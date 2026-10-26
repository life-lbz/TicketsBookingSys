package com.ticket.system.controller;

import com.ticket.system.common.ApiResponse;
import com.ticket.system.model.Event;
import com.ticket.system.model.Seat;
import com.ticket.system.repository.EventRepository;
import com.ticket.system.repository.SeatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/events")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}) // Allow frontend access
public class EventController {

    private static final Logger logger = Logger.getLogger(EventController.class.getName());

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public EventController(EventRepository eventRepository, SeatRepository seatRepository) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }
    
    // 获取所有赛事
    @GetMapping
    public ResponseEntity<ApiResponse<List<Event>>> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(events));
    }
    
    // 根据ID获取赛事
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> getEventById(@PathVariable Long id) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        return eventOptional.map(event -> ResponseEntity.ok(ApiResponse.success(event)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // 创建赛事
    @PostMapping
    public ResponseEntity<ApiResponse<Event>> createEvent(@RequestBody Event event) {
        // 自动分配固定ID
        if (event.getId() == null) {
            // 查询当前最大ID
            List<Event> allEvents = eventRepository.findAll();
            long maxId = 0;
            for (Event e : allEvents) {
                if (e.getId() > maxId) {
                    maxId = e.getId();
                }
            }
            // 新ID = 最大ID + 1
            event.setId(maxId + 1);
        }
        
        // 设置默认状态为"未发售"
        if (event.getStatus() == null || event.getStatus().isEmpty()) {
            event.setStatus("未发售");
        }
        
        Event savedEvent = eventRepository.save(event);
        return ResponseEntity.ok(ApiResponse.success("赛事创建成功", savedEvent));
    }
    
    // 更新赛事
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isPresent()) {
            Event existingEvent = eventOptional.get();
            
            // 检查是否从停售状态改为售票中
            if ("停售".equals(existingEvent.getStatus()) && "售票中".equals(event.getStatus())) {
                // 检查赛事时间是否已过期
                try {
                    LocalDateTime eventTime = parseEventTime(existingEvent.getTime());
                    if (eventTime.isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("该赛事时间已过，无法重新发售"));
                    }
                } catch (Exception e) {
                    logger.warning("解析赛事时间失败: " + e.getMessage());
                    return ResponseEntity.badRequest().body(ApiResponse.error("无法解析赛事时间"));
                }
            }
            
            // 检查是否从未发售状态改为售票中（首次发布）
            if ("未发售".equals(existingEvent.getStatus()) && "售票中".equals(event.getStatus())) {
                // 检查赛事时间是否已过期
                try {
                    LocalDateTime eventTime = parseEventTime(existingEvent.getTime());
                    if (eventTime.isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("该赛事时间已过，无法发布"));
                    }
                } catch (Exception e) {
                    logger.warning("解析赛事时间失败: " + e.getMessage());
                    return ResponseEntity.badRequest().body(ApiResponse.error("无法解析赛事时间"));
                }
            }
            
            // 手动停售时保留座位状态，不释放锁定座位
            
            existingEvent.setName(event.getName());
            existingEvent.setTime(event.getTime());
            existingEvent.setVenue(event.getVenue());
            existingEvent.setMinPrice(event.getMinPrice());
            existingEvent.setCover(event.getCover());
            existingEvent.setHot(event.isHot());
            existingEvent.setCategory(event.getCategory());
            existingEvent.setCity(event.getCity());
            existingEvent.setStatus(event.getStatus());
            Event updatedEvent = eventRepository.save(existingEvent);
            return ResponseEntity.ok(ApiResponse.success("赛事更新成功", updatedEvent));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 删除赛事
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isPresent()) {
            // 先删除该赛事的所有座位
            List<Seat> seats = seatRepository.findByEventId(id);
            if (!seats.isEmpty()) {
                seatRepository.deleteAll(seats);
                seatRepository.flush();
                logger.info("已删除赛事 " + id + " 的全部 " + seats.size() + " 个座位");
            }
            // 再删除赛事
            eventRepository.deleteById(id);
            return ResponseEntity.ok("赛事删除成功");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 上传赛事封面图片
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCover(@RequestParam("file") MultipartFile file) {
        try {
            // 创建上传目录
            String uploadDir = "uploads/events/images/";
            File directory = new File(uploadDir);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("无法创建上传目录: " + uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID() + extension;

            // 保存文件
            Path filePath = Paths.get(uploadDir + newFilename);
            Files.write(filePath, file.getBytes());

            // 返回文件访问URL
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/events/images/" + newFilename);
            result.put("filename", newFilename);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }

    // 获取赛事座位列表
    @GetMapping(value = "/{id}/seats", produces = "application/json")
    public ResponseEntity<ApiResponse<List<Seat>>> getSeatsForEvent(@PathVariable Long id) {
        List<Seat> seats = seatRepository.findByEventId(id);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    // 生成座位
    @PostMapping("/{id}/seats/generate")
    @Transactional
    public ResponseEntity<?> generateSeats(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        logger.info("开始生成座位: 赛事ID=" + id);
        
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isEmpty()) {
            logger.warning("赛事不存在: ID=" + id);
            return ResponseEntity.notFound().build();
        }

        Integer rows = (Integer) request.get("rows");
        Integer cols = (Integer) request.get("cols");
        Double price = request.get("price") != null ? ((Number) request.get("price")).doubleValue() : null;

        logger.info("座位参数: 行数=" + rows + ", 列数=" + cols + ", 票价=" + price);

        if (rows == null || cols == null || rows < 1 || rows > 50 || cols < 1 || cols > 50 || price == null) {
            return ResponseEntity.badRequest().body("行数、列数和票价必须有效");
        }

        // 检查是否已有座位
        List<Seat> existingSeats = seatRepository.findByEventId(id);
        logger.info("该赛事已有座位数量: " + existingSeats.size());
        if (!existingSeats.isEmpty()) {
            logger.warning("该赛事已生成座位，不允许重复生成");
            return ResponseEntity.badRequest().body("该赛事已生成座位，请先删除全部座位后再重新生成");
        }

        // 生成新座位
        List<Seat> newSeats = new ArrayList<>();
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                Seat seat = new Seat();
                seat.setEventId(id);
                seat.setRowNum(r);
                seat.setColNum(c);
                seat.setPrice(price);
                seat.setStatus("AVAILABLE");
                newSeats.add(seat);
            }
        }

        logger.info("准备保存座位数量: " + newSeats.size());
        List<Seat> savedSeats = seatRepository.saveAll(newSeats);
        seatRepository.flush();
        logger.info("成功保存座位数量: " + savedSeats.size());
        
        // 验证保存结果
        List<Seat> verifySeats = seatRepository.findByEventId(id);
        logger.info("验证查询: 该赛事现有座位数量 = " + verifySeats.size());

        Map<String, Object> result = new HashMap<>();
        result.put("count", savedSeats.size());
        result.put("message", "成功生成 " + savedSeats.size() + " 个座位");
        return ResponseEntity.ok(result);
    }

    // 删除赛事的全部座位
    @DeleteMapping("/{id}/seats")
    @Transactional
    public ResponseEntity<?> deleteAllSeats(@PathVariable Long id) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = eventOptional.get();
        // 检查赛事状态，如果已发售（售票中或售罄）则不允许删除座位
        if ("售票中".equals(event.getStatus()) || "售罄".equals(event.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "该赛事已发售，无法删除座位"));
        }

        List<Seat> existingSeats = seatRepository.findByEventId(id);
        if (existingSeats.isEmpty()) {
            return ResponseEntity.ok("该赛事没有座位可删除");
        }

        int count = existingSeats.size();
        seatRepository.deleteAll(existingSeats);
        seatRepository.flush();

        logger.info("已删除赛事 " + id + " 的全部 " + count + " 个座位");
        return ResponseEntity.ok("成功删除 " + count + " 个座位");
    }

    // 手动触发停售检查
    @PostMapping("/stop-sales-check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerStopSalesCheck() {
        logger.info("=== 手动触发赛事停售检查 ===");
        
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByStatus("售票中");
        int stoppedCount = 0;
        
        for (Event event : events) {
            try {
                // 尝试多种时间格式解析
                LocalDateTime eventTime = parseEventTime(event.getTime());
                long daysSinceStart = Duration.between(eventTime, now).toDays();
                
                logger.info("赛事 [" + event.getName() + "] 时间: " + event.getTime() + ", 已过去: " + daysSinceStart + "天");
                
                if (daysSinceStart >= 2) {
                    event.setStatus("停售");
                    eventRepository.save(event);
                    stoppedCount++;
                }
            } catch (Exception e) {
                logger.warning("处理赛事 [" + event.getName() + "] 失败: " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("checkedCount", events.size());
        result.put("stoppedCount", stoppedCount);
        result.put("message", "已检查 " + events.size() + " 个赛事，停止 " + stoppedCount + " 个赛事售票");
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // 解析赛事时间（支持多种格式）
    private LocalDateTime parseEventTime(String timeStr) throws Exception {
        // 定义支持的时间格式（包括单数字月份和小时）
        String[] formats = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "yyyy.MM.dd-HH:mm",
            "yyyy/M/d H:mm:ss",
            "yyyy/M/d H:mm",
            "yyyy/M/dd H:mm:ss",
            "yyyy/M/dd H:mm",
            "yyyy/MM/d H:mm:ss",
            "yyyy/MM/d H:mm"
        };
        
        for (String format : formats) {
            try {
                return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern(format));
            } catch (Exception e) {
                // 尝试下一个格式
            }
        }
        
        throw new Exception("无法解析时间格式: " + timeStr);
    }
}
