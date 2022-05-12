package org.csu.csumall.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.User;

public interface IUserService {

    ServerResponse<User> login(String username, String password);
    //注册时检查字段是否可用
    ServerResponse<String> checkField(String fieldName, String fieldValue);
    //用户注册
    ServerResponse<String> register(User user);
    //获取忘记密码的问题
    ServerResponse<String> getForgetQuestion(String username);
    //校验忘记密码的问题和答案是否正确
    ServerResponse<String> checkForgetAnswer(String username, String question,String answer);
    //通过忘记密码的问题答案重置密码
    ServerResponse<String> resetForgetPassword(String username, String newPassword, String forgetToken);
    //登录状态下重置密码
    ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user);
    //登录状态下更新用户信息
//    ServerResponse<String> updateUserInfo(User user);
    //更新用户姓名
    ServerResponse<String> updateUserInfo(Integer id,String type,String edit);
    //登录状态下获取用户详细信息
    ServerResponse<User> getUserDetail(Integer userId);

}
