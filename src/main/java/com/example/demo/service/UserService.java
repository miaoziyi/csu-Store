package com.example.demo.service;

import com.example.demo.common.CommonResponse;
import com.example.demo.entity.User;

public interface UserService {
    CommonResponse<User> login(String username, String password);
}
