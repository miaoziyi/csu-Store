package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.CommonResponse;
import com.example.demo.entity.User;
import com.example.demo.persistence.UserMapper;
import com.example.demo.service.UserService;
import com.example.demo.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public CommonResponse<User> login(String username, String password){
        String md5Password = MD5Util.md5Encrypt32Upper(password);
        User loginUser = userMapper.selectOne(
                Wrappers.<User>query().eq("username",username).eq("password",md5Password));
        if(loginUser == null){
            return CommonResponse.createForError("用户名或密码错误");
        }
        loginUser.setPassword(StringUtils.EMPTY);//登录成功密码置空
        return CommonResponse.createForSuccess(loginUser);
    }

}
