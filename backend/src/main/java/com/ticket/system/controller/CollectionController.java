package com.ticket.system.controller;

import com.ticket.system.common.ApiResponse;
import com.ticket.system.model.Collection;
import com.ticket.system.model.Event;
import com.ticket.system.repository.CollectionRepository;
import com.ticket.system.repository.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/collections")
public class CollectionController {

    private final CollectionRepository collectionRepository;
    private final EventRepository eventRepository;

    public CollectionController(CollectionRepository collectionRepository, EventRepository eventRepository) {
        this.collectionRepository = collectionRepository;
        this.eventRepository = eventRepository;
    }

    // 获取用户的收藏列表
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCollections(@PathVariable String username) {
        List<Collection> collections = collectionRepository.findByUsername(username);
        // 转换为包含赛事信息的响应
        List<Map<String, Object>> result = collections.stream().map(collection -> {
            Event event = eventRepository.findById(collection.getEventId()).orElse(null);
            if (event != null) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", event.getId());
                map.put("name", event.getName());
                map.put("time", event.getTime());
                map.put("venue", event.getVenue());
                map.put("price", event.getMinPrice());
                map.put("cover", event.getCover());
                map.put("collectionId", collection.getId());
                map.put("collectionTime", collection.getCreateTime());
                return map;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 检查收藏状态
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkCollection(
            @RequestParam String username, 
            @RequestParam Long eventId) {
        List<Collection> collections = collectionRepository.findByUsername(username);
        boolean isCollected = collections.stream()
                .anyMatch(col -> col.getEventId().equals(eventId));
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("isCollected", isCollected);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 添加收藏
    @PostMapping
    public ResponseEntity<ApiResponse<String>> addCollection(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        Long eventId = ((Number) request.get("eventId")).longValue();

        // 检查赛事是否存在
        if (!eventRepository.existsById(eventId)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("赛事不存在"));
        }

        // 检查是否已经收藏
        List<Collection> existing = collectionRepository.findByUsername(username);
        boolean alreadyCollected = existing.stream()
                .anyMatch(col -> col.getEventId().equals(eventId));

        if (alreadyCollected) {
            return ResponseEntity.badRequest().body(ApiResponse.error("已经收藏过该赛事"));
        }

        // 创建新收藏
        Collection collection = new Collection();
        collection.setUsername(username);
        collection.setEventId(eventId);
        collection.setCreateTime(LocalDateTime.now());

        collectionRepository.save(collection);
        return ResponseEntity.ok(ApiResponse.success("收藏成功"));
    }

    // 取消收藏
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> removeCollection(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        Long eventId = ((Number) request.get("eventId")).longValue();

        // 查找并删除收藏
        List<Collection> collections = collectionRepository.findByUsername(username);
        Collection toDelete = collections.stream()
                .filter(col -> col.getEventId().equals(eventId))
                .findFirst()
                .orElse(null);

        if (toDelete == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("未找到收藏记录"));
        }

        collectionRepository.delete(toDelete);
        return ResponseEntity.ok(ApiResponse.success("取消收藏成功"));
    }
}