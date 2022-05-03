package com.example.demo.controller;

import com.example.demo.common.CONSTANT;
import com.example.demo.common.CommonResponse;
import com.example.demo.dto.UpdateUserDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
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

    @PostMapping("check_field")
    public CommonResponse<String> checkField(
            @RequestParam String fieldName,
            @RequestParam String fieldValue) {
        return userService.checkField(fieldName, fieldValue);
    }

    @PostMapping("register")
    @ResponseBody
    public CommonResponse<String> register(User user){
        System.out.println("注册");
        return userService.register(user);
    }

    @PostMapping("get_forget_question")
    public CommonResponse<String> getForgetQuestion(
            @RequestParam @Validated @NotBlank(message = "用户名不能为空") String username){
        return userService.getForgetQuestion(username);
    }

    @PostMapping("check_forget_answer")
    public CommonResponse<String> checkForgetAnswer(
            @RequestParam @Validated @NotBlank(message = "用户名不能为空") String username,
            @RequestParam @Validated @NotBlank(message = "忘记密码问题不能为空") String question,
            @RequestParam @Validated @NotBlank(message = "忘记密码问题答案不能为空") String answer){
        return userService.checkForgetAnswer(username,question,answer);
    }

    @PostMapping("reset_forget_password")
    public CommonResponse<String> resetForgetPassword(
            @RequestParam @Validated @NotBlank(message = "用户名不能为空") String username,
            @RequestParam @Validated @NotBlank(message = "新密码不能为空") String newPassword,
            @RequestParam @Validated @NotBlank(message = "重置密码token不能为空") String forgetToken){
        return userService.resetForgetPassword(username,newPassword,forgetToken);
    }

    @PostMapping("reset_password")
    public CommonResponse<String> resetPassword(
            @RequestParam @Validated @NotBlank(message = "旧密码不能为空") String oldPassword,
            @RequestParam @Validated @NotBlank(message = "新密码不能为空") String newPassword,
            HttpSession session){
        User loginUser = (User) session.getAttribute(CONSTANT.LOGIN_USER);
        if(loginUser == null){
            return CommonResponse.createForError("用户未登录");
        }
        return userService.resetPassword(oldPassword, newPassword,loginUser);
    }

    @PostMapping("get_user_detail")
    public CommonResponse<User> getUserDetail(HttpSession session){
        User loginUser = (User) session.getAttribute(CONSTANT.LOGIN_USER);
        if(loginUser == null){
            return CommonResponse.createForError("用户未登录");
        }
        return userService.getUserDetail(loginUser.getId());
    }
    @PostMapping("update_user_info")
    public CommonResponse<String> updateUserInfo(@RequestParam String type,@RequestParam String edit,HttpSession session){
        System.out.println(edit);
        User loginUser = (User) session.getAttribute(CONSTANT.LOGIN_USER);
        System.out.println(loginUser);
        CommonResponse<String> result = userService.updateUserName(loginUser.getId(),type,edit);
        if(result.isSuccess()){
            return CommonResponse.createForSuccessMessage("更新"+type+"成功");
        }
        return CommonResponse.createForError(result.getMessage());
    }

//    @PostMapping("update_user_info")
//    public CommonResponse<User> updateUserInfo(@RequestBody @Valid UpdateUserDTO updateUser,
//                                               HttpSession session){
//        User loginUser = (User) session.getAttribute(CONSTANT.LOGIN_USER);
//        if(loginUser == null){
//            return CommonResponse.createForError("用户未登录");
//        }
//        loginUser.setEmail(updateUser.getEmail());
//        loginUser.setPhone(updateUser.getPhone());
//        loginUser.setQuestion(updateUser.getQuestion());
//        loginUser.setAnswer(updateUser.getAnswer());
//
//        CommonResponse<String> result = userService.updateUserInfo(loginUser);
//        if(result.isSuccess()){
//            loginUser = userService.getUserDetail(loginUser.getId()).getData();
//            session.setAttribute(CONSTANT.LOGIN_USER, loginUser);
//            return CommonResponse.createForSuccess(loginUser);
//        }
//        return CommonResponse.createForError(result.getMessage());
//    }

    @GetMapping("logout")
    public CommonResponse<String> logout(HttpSession session){
        session.removeAttribute(CONSTANT.LOGIN_USER);
        return CommonResponse.createForSuccessMessage("退出登录成功");
    }

}
