package com.ticket.system.service;

import com.ticket.system.model.Collection;
import com.ticket.system.model.Order;
import com.ticket.system.model.User;
import com.ticket.system.model.UserInfo;
import com.ticket.system.repository.CollectionRepository;
import com.ticket.system.repository.OrderRepository;
import com.ticket.system.repository.UserInfoRepository;
import com.ticket.system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Map;

import java.util.List;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;
    private final OrderRepository orderRepository;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, CollectionRepository collectionRepository,
                      OrderRepository orderRepository, UserInfoRepository userInfoRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.collectionRepository = collectionRepository;
        this.orderRepository = orderRepository;
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // 根据用户名获取用户信息
    public UserInfo getUserInfo(String username) {
        return userInfoRepository.findByUsername(username);
    }
    
    // 获取所有用户列表
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            // 尝试通过手机号查找用户
            user = userRepository.findByPhone(username);
            if (user == null) {
                throw new RuntimeException("用户名不存在");
            }
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }
    
    @Transactional
    public User register(String username, String phone, String password) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已被注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setAdmin(false);
        user = userRepository.save(user);
        
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPhone(phone);
        userInfoRepository.save(userInfo);
        
        return user;
    }
    
    public void resetPassword(String username, String phone, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (!user.getPhone().equals(phone)) {
            throw new RuntimeException("手机号错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void changePassword(Long id, String phone, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 验证手机号
        if (!user.getPhone().equals(phone)) {
            throw new RuntimeException("手机号与账户不匹配");
        }
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Transactional
    public User updateUsername(String oldUsername, String newUsername) {
        // 检查新用户名是否已存在
        if (userRepository.findByUsername(newUsername) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 1. 更新用户表
        User user = userRepository.findByUsername(oldUsername);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setUsername(newUsername);
        userRepository.save(user);
        
        // 2. 更新收藏表
        List<Collection> collections = collectionRepository.findByUsername(oldUsername);
        for (Collection collection : collections) {
            collection.setUsername(newUsername);
            collectionRepository.save(collection);
        }
        
        // 3. 更新订单表
        List<Order> orders = orderRepository.findByUsername(oldUsername);
        for (Order order : orders) {
            order.setUsername(newUsername);
            orderRepository.save(order);
        }
        
        // 4. 更新个人信息表
        UserInfo userInfo = userInfoRepository.findByUsername(oldUsername);
        if (userInfo != null) {
            userInfo.setUsername(newUsername);
            userInfoRepository.save(userInfo);
        }
        
        return user;
    }
    
    @Transactional
    public void updateProfile(Map<String, Object> profileData) {
        String username = (String) profileData.get("username");
        if (username == null) {
            throw new RuntimeException("用户名不能为空");
        }
        
        // 查找或创建UserInfo记录
        UserInfo userInfo = userInfoRepository.findByUsername(username);
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setUsername(username);
        }
        
        // 更新用户信息
        if (profileData.containsKey("nickname")) {
            userInfo.setNickname((String) profileData.get("nickname"));
        }
        if (profileData.containsKey("gender")) {
            userInfo.setGender((String) profileData.get("gender"));
        }
        if (profileData.containsKey("birthday")) {
            String birthdayStr = (String) profileData.get("birthday");
            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                userInfo.setBirthday(LocalDate.parse(birthdayStr));
            }
        }
        if (profileData.containsKey("city")) {
            userInfo.setCity((String) profileData.get("city"));
        }
        if (profileData.containsKey("life_status")) {
            userInfo.setLifeStatus((String) profileData.get("life_status"));
        }
        if (profileData.containsKey("industry")) {
            userInfo.setIndustry((String) profileData.get("industry"));
        }
        if (profileData.containsKey("hobbies")) {
            userInfo.setHobbies((String) profileData.get("hobbies"));
        }
        if (profileData.containsKey("signature")) {
            userInfo.setSignature((String) profileData.get("signature"));
        }
        if (profileData.containsKey("phone")) {
            userInfo.setPhone((String) profileData.get("phone"));
        }
        if (profileData.containsKey("avatar")) {
            userInfo.setAvatar((String) profileData.get("avatar"));
        }
        if (profileData.containsKey("motions")) {
            userInfo.setMotions((String) profileData.get("motions"));
        }
        
        // 保存更新
        userInfoRepository.save(userInfo);
    }
    
    // 更新用户信息
    public User updateUser(Long id, Map<String, Object> userData) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (userData.containsKey("username")) {
            user.setUsername((String) userData.get("username"));
        }
        if (userData.containsKey("phone")) {
            user.setPhone((String) userData.get("phone"));
        }
        
        return userRepository.save(user);
    }
    
    // 删除用户
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 删除用户相关数据
        String username = user.getUsername();
        
        // 删除收藏记录
        List<Collection> collections = collectionRepository.findByUsername(username);
        collectionRepository.deleteAll(collections);
        
        // 删除订单记录
        List<Order> orders = orderRepository.findByUsername(username);
        orderRepository.deleteAll(orders);
        
        // 删除个人信息记录
        UserInfo userInfo = userInfoRepository.findByUsername(username);
        if (userInfo != null) {
            userInfoRepository.delete(userInfo);
        }
        
        // 删除用户记录
        userRepository.delete(user);
    }
}