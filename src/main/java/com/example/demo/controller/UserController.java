package com.example.demo.controller;

import com.example.demo.common.CommonResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/user/")
@Validated
public class UserController {

    @PostMapping("login")
    public CommonResponse<String> login(@RequestParam @Validated @NotBlank(message = "用户名不能为空") String username, @RequestParam String password){
        return CommonResponse.createForSuccessMessage("登录成功");
    }

}
