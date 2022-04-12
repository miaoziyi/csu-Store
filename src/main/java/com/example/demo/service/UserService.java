package com.example.demo.service;

import com.example.demo.common.CommonResponse;
import com.example.demo.entity.User;

public interface UserService {
    CommonResponse<User> login(String username, String password);
    //注册时检查字段是否可用
    CommonResponse<String> checkField(String fieldName, String fieldValue);
    //用户注册
    CommonResponse<String> register(User user);
    //获取忘记密码的问题
    CommonResponse<String> getForgetQuestion(String username);
    //校验忘记密码的问题和答案是否正确
    CommonResponse<String> checkForgetAnswer(String username, String question,String answer);
    //通过忘记密码的问题答案重置密码
    CommonResponse<String> resetForgetPassword(String username, String newPassword, String forgetToken);
    //登录状态下重置密码
    CommonResponse<String> resetPassword(String oldPassword, String newPassword, User user);
    //登录状态下更新用户信息
    CommonResponse<String> updateUserInfo(User user);
    //登录状态下获取用户详细信息
    CommonResponse<User> getUserDetail(Integer userId);
}
