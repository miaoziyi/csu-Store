package org.csu.csumall.controller;

import org.csu.csumall.common.CONSTANT;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.User;
import org.csu.csumall.service.IUserService;
import org.csu.csumall.utils.CookieUtil;
import org.csu.csumall.utils.JSONUtil;
import org.csu.csumall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("login")
    public ServerResponse<User> login(@RequestParam @Validated String username,
                                      @RequestParam @Validated String password,
                                      HttpSession session,
                                      HttpServletResponse response) {

        ServerResponse<User> result = userService.login(username, password);
        if (result.isSuccess()) {
            System.out.println("session: "+ session.getId());
            String userJson = JSONUtil.obj2String(result.getData());
            redisUtil.getRedisTemplate().opsForValue().set(session.getId(),userJson,60, TimeUnit.MINUTES);
            CookieUtil.writeLoginToken(response,session.getId());

//            session.setAttribute(CONSTANT.CURRENT_USER, result.getData());
        }
        return result;
    }

    @PostMapping("check_field")
    public ServerResponse<String> checkField(
            @RequestParam String fieldName,
            @RequestParam String fieldValue) {
        return userService.checkField(fieldName, fieldValue);
    }

    @PostMapping("register")
    @ResponseBody
    public ServerResponse<String> register(User user) {
        System.out.println("注册");
        return userService.register(user);
    }

    @PostMapping("get_forget_question")
    public ServerResponse<String> getForgetQuestion(
            @RequestParam @Validated String username) {
        return userService.getForgetQuestion(username);
    }

    @PostMapping("check_forget_answer")
    public ServerResponse<String> checkForgetAnswer(
            @RequestParam @Validated String username,
            @RequestParam @Validated String question,
            @RequestParam @Validated String answer) {
        return userService.checkForgetAnswer(username, question, answer);
    }

    @PostMapping("reset_forget_password")
    public ServerResponse<String> resetForgetPassword(
            @RequestParam @Validated String username,
            @RequestParam @Validated String newPassword,
            @RequestParam @Validated String forgetToken) {
        return userService.resetForgetPassword(username, newPassword, forgetToken);
    }

    @PostMapping("reset_password")
    public ServerResponse<String> resetPassword(
            @RequestParam @Validated String oldPassword,
            @RequestParam @Validated String newPassword,
            HttpSession session,
            HttpServletRequest request) {
        String sessionId = CookieUtil.readLoginToken(request);
        User loginUser =JSONUtil.string2Obj((String) redisUtil.getRedisTemplate().opsForValue().get(sessionId),User.class);
//        User loginUser = (User) session.getAttribute(CONSTANT.CURRENT_USER);
        if (loginUser == null) {
            return ServerResponse.createForError("用户未登录");
        }
        return userService.resetPassword(oldPassword, newPassword, loginUser);
    }

    @PostMapping("get_user_detail")
    public ServerResponse<User> getUserDetail(HttpSession session,HttpServletRequest request) {
//        User loginUser = (User) session.getAttribute(CONSTANT.CURRENT_USER);
        String sessionId = CookieUtil.readLoginToken(request);
        System.out.println(sessionId);
        User loginUser =JSONUtil.string2Obj((String) redisUtil.getRedisTemplate().opsForValue().get(sessionId),User.class);
        System.out.println(loginUser);
        if (loginUser == null) {
            return ServerResponse.createForError("用户未登录");
        }
        return userService.getUserDetail(loginUser.getId());
    }

    @PostMapping("update_user_info")
    public ServerResponse<String> updateUserInfo(@RequestParam String type, @RequestParam String edit, HttpSession session,HttpServletRequest request) {
        System.out.println(edit);
        String sessionId = CookieUtil.readLoginToken(request);
        User loginUser =JSONUtil.string2Obj((String) redisUtil.getRedisTemplate().opsForValue().get(sessionId),User.class);
//        User loginUser = (User) session.getAttribute(CONSTANT.CURRENT_USER);
        System.out.println(loginUser);
        ServerResponse<String> result = userService.updateUserInfo(loginUser.getId(), type, edit);
        if (result.isSuccess()) {
            return ServerResponse.createForSuccess("更新" + type + "成功");
        }
        return ServerResponse.createForError(result.getMessage());
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
    public ServerResponse<String> logout(HttpServletRequest request,HttpSession session,HttpServletResponse response) {
//        session.removeAttribute(CONSTANT.CURRENT_USER);
        CookieUtil.delLoginToken(request,response);
        return ServerResponse.createForSuccess("退出登录成功");
    }
}
