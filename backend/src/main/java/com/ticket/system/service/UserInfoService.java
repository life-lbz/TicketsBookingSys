package com.ticket.system.service;

import com.ticket.system.model.UserInfo;
import com.ticket.system.repository.UserInfoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserInfoService {
    
    private final UserInfoRepository userInfoRepository;
    
    public UserInfoService(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }
    
    public UserInfo getUserInfo(String username) {
        return userInfoRepository.findByUsername(username);
    }
    
    public UserInfo updateUserInfo(String username, UserInfo userInfo) {
        UserInfo existingUserInfo = userInfoRepository.findByUsername(username);
        
        if (existingUserInfo == null) {
            existingUserInfo = new UserInfo();
            existingUserInfo.setUsername(username);
        }
        
        if (userInfo.getNickname() != null) {
            existingUserInfo.setNickname(userInfo.getNickname());
        }
        if (userInfo.getGender() != null) {
            existingUserInfo.setGender(userInfo.getGender());
        }
        if (userInfo.getBirthday() != null) {
            existingUserInfo.setBirthday(userInfo.getBirthday());
        }
        if (userInfo.getCity() != null) {
            existingUserInfo.setCity(userInfo.getCity());
        }
        if (userInfo.getLifeStatus() != null) {
            existingUserInfo.setLifeStatus(userInfo.getLifeStatus());
        }
        if (userInfo.getIndustry() != null) {
            existingUserInfo.setIndustry(userInfo.getIndustry());
        }
        if (userInfo.getHobbies() != null) {
            existingUserInfo.setHobbies(userInfo.getHobbies());
        }
        if (userInfo.getSignature() != null) {
            existingUserInfo.setSignature(userInfo.getSignature());
        }
        if (userInfo.getPhone() != null) {
            existingUserInfo.setPhone(userInfo.getPhone());
        }
        if (userInfo.getAvatar() != null) {
            existingUserInfo.setAvatar(userInfo.getAvatar());
        }
        
        existingUserInfo.setUpdateTime(LocalDateTime.now());
        
        return userInfoRepository.save(existingUserInfo);
    }
    
    public UserInfo saveUserInfo(UserInfo userInfo) {
        return userInfoRepository.save(userInfo);
    }
}