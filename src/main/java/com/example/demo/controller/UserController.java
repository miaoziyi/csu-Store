package com.example.demo.controller;

import com.example.demo.common.CONSTANT;
import com.example.demo.common.CommonResponse;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/user/")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("login")
    public CommonResponse<User> login(@RequestParam @Validated @NotBlank(message = "用户名不能为空") String username,
                                      @RequestParam @Validated @NotBlank(message = "密码不能为空") String password,
                                      HttpSession session){

        CommonResponse<User> result = userService.login(username,password);
        if (result.isSuccess()){
            session.setAttribute(CONSTANT.LOGIN_USER,result.getData());
        }

        return result;
    }

}
