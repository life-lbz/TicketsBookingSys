package com.ticket.system.controller;

import com.ticket.system.common.ApiResponse;
import com.ticket.system.model.User;
import com.ticket.system.model.UserInfo;
import com.ticket.system.service.UserService;
import com.ticket.system.utils.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        try {
            User user = userService.login(username, password);

            String role = user.isAdmin() ? "ADMIN" : "USER";
            String token = JwtUtils.generateToken(user.getId(), user.getUsername(), role);

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("phone", user.getPhone());
            response.put("isAdmin", user.isAdmin());
            response.put("token", token);

            UserInfo userInfo = userService.getUserInfo(username);
            if (userInfo != null) {
                response.put("avatar", userInfo.getAvatar());
            }

            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(401, e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody Map<String, String> registerData) {
        String username = registerData.get("username");
        String phone = registerData.get("phone");
        String password = registerData.get("password");

        try {
            User user = userService.register(username, phone, password);
            return ResponseEntity.ok(ApiResponse.success("注册成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody Map<String, String> resetData) {
        String username = resetData.get("username");
        String phone = resetData.get("phone");
        String newPassword = resetData.get("newPassword");

        try {
            userService.resetPassword(username, phone, newPassword);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/username")
    public ResponseEntity<ApiResponse<User>> updateUsername(@RequestParam String oldUsername, @RequestParam String newUsername) {
        try {
            User updatedUser = userService.updateUsername(oldUsername, newUsername);
            return ResponseEntity.ok(ApiResponse.success("用户名修改成功", updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(ApiResponse.success("用户名已存在", false));
        } else {
            return ResponseEntity.ok(ApiResponse.success("用户名可用", true));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<String>> updateProfile(@RequestBody Map<String, Object> profileData) {
        try {
            userService.updateProfile(profileData);
            return ResponseEntity.ok(ApiResponse.success("个人信息更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/info/{username}")
    public ResponseEntity<ApiResponse<UserInfo>> getUserInfo(@PathVariable String username) {
        try {
            UserInfo userInfo = userService.getUserInfo(username);
            return ResponseEntity.ok(ApiResponse.success(Objects.requireNonNullElseGet(userInfo, UserInfo::new)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllUsers() {
        try {
            return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, userData)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String password = request.get("password");
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("密码不能为空"));
        }
        
        try {
            userService.resetPassword(id, password);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("手机号不能为空"));
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("手机号格式不正确"));
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("原密码不能为空"));
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("新密码不能为空"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(ApiResponse.error("密码长度不能少于6位"));
        }
        
        try {
            userService.changePassword(id, phone, oldPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}