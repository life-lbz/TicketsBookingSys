package com.ticket.system.controller;

import com.ticket.system.model.UserInfo;
import com.ticket.system.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userinfo")
public class UserInfoController {
    
    @Autowired
    private UserInfoService userInfoService;
    
    @GetMapping("/{username}")
    public ResponseEntity<UserInfo> getUserInfo(@PathVariable String username) {
        UserInfo userInfo = userInfoService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }
    
    @PutMapping
    public ResponseEntity<UserInfo> updateUserInfo(@RequestParam String username, @RequestBody UserInfo userInfo) {
        UserInfo updatedUserInfo = userInfoService.updateUserInfo(username, userInfo);
        return ResponseEntity.ok(updatedUserInfo);
    }
    
    @PostMapping
    public ResponseEntity<UserInfo> createUserInfo(@RequestBody UserInfo userInfo) {
        UserInfo createdUserInfo = userInfoService.saveUserInfo(userInfo);
        return ResponseEntity.ok(createdUserInfo);
    }
}