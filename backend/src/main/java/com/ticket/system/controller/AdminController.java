package com.ticket.system.controller;

import com.ticket.system.common.ApiResponse;
import com.ticket.system.model.Admin;
import com.ticket.system.repository.AdminRepository;
import com.ticket.system.utils.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admins")
public class AdminController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<Admin> adminOptional = adminRepository.findByUsername(username);
        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            if (admin.getActive() && passwordEncoder.matches(password, admin.getPassword())) {
                String token = JwtUtils.generateToken(admin.getId(), admin.getUsername(), admin.getRole());

                Map<String, Object> result = new HashMap<>();
                result.put("id", admin.getId());
                result.put("username", admin.getUsername());
                result.put("role", admin.getRole());
                result.put("phone", admin.getPhone());
                result.put("token", token);

                return ResponseEntity.ok(ApiResponse.success("登录成功", result));
            }
        }

        return ResponseEntity.badRequest().body(ApiResponse.error(401, "用户名或密码错误"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllAdmins() {
        List<Admin> admins = adminRepository.findByActiveTrue();
        List<Map<String, Object>> adminList = new ArrayList<>();
        for (Admin admin : admins) {
            Map<String, Object> adminMap = new HashMap<>();
            adminMap.put("id", admin.getId());
            adminMap.put("username", admin.getUsername());
            adminMap.put("phone", admin.getPhone());
            adminMap.put("role", admin.getRole());
            adminList.add(adminMap);
        }
        return ResponseEntity.ok(ApiResponse.success(adminList));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createAdmin(@RequestBody Admin admin) {
        if (adminRepository.existsByUsername(admin.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名已存在"));
        }
        if (admin.getPhone() != null && adminRepository.existsByPhone(admin.getPhone())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("手机号已存在"));
        }

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setActive(true);
        adminRepository.save(admin);
        return ResponseEntity.ok(ApiResponse.success("管理员创建成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        Optional<Admin> adminOptional = adminRepository.findById(id);
        if (adminOptional.isPresent()) {
            Admin existingAdmin = adminOptional.get();
            existingAdmin.setUsername(admin.getUsername());
            existingAdmin.setPhone(admin.getPhone());
            existingAdmin.setRole(admin.getRole());
            if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
                existingAdmin.setPassword(passwordEncoder.encode(admin.getPassword()));
            }
            adminRepository.save(existingAdmin);
            return ResponseEntity.ok(ApiResponse.success("管理员更新成功"));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAdmin(@PathVariable Long id) {
        Optional<Admin> adminOptional = adminRepository.findById(id);
        if (adminOptional.isPresent()) {
            adminRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("管理员删除成功"));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String password = request.get("password");
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("密码不能为空"));
        }
        
        Optional<Admin> adminOptional = adminRepository.findById(id);
        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            admin.setPassword(passwordEncoder.encode(password));
            adminRepository.save(admin);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
        }
        return ResponseEntity.notFound().build();
    }
}