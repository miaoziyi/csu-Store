package org.csu.csumall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.csu.csumall.common.CONSTANT;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.common.TokenCache;
import org.csu.csumall.entity.User;
import org.csu.csumall.mapper.UserMapper;
import org.csu.csumall.service.IUserService;
import org.csu.csumall.utils.MD5Util;
import org.csu.csumall.utils.TokenCacheUtil;
import org.csu.csumall.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password){
        String md5Password = MD5Util.md5Encrypt32Upper(password);
        User loginUser = userMapper.selectOne(
                Wrappers.<User>query().eq("username",username).eq("password",md5Password));
        if(loginUser == null){
            return ServerResponse.createForError("用户名或密码错误");
        }
        loginUser.setPassword(StringUtils.EMPTY);//登录成功密码置空
        return ServerResponse.createForSuccess(loginUser);
    }

    @Override
    public ServerResponse<String> checkField(String fieldName, String fieldValue){
        if(CONSTANT.USER_FIELDS.USERNAME.equals(fieldName)){
            long rows = userMapper.selectCount(Wrappers.<User>query().eq(CONSTANT.USER_FIELDS.USERNAME,fieldValue));
            if(rows > 0){
                return ServerResponse.createForError("用户名已存在");
            }
        }
        else if(CONSTANT.USER_FIELDS.PHONE.equals(fieldName)){
            long rows = userMapper.selectCount(Wrappers.<User>query().eq(CONSTANT.USER_FIELDS.PHONE,fieldValue));
            if(rows > 0){
                return ServerResponse.createForError("电话号码已存在");
            }
        }
        else if(CONSTANT.USER_FIELDS.EMAIL.equals(fieldName)){
            long rows = userMapper.selectCount(Wrappers.<User>query().eq(CONSTANT.USER_FIELDS.EMAIL,fieldValue));
            if(rows > 0){
                return ServerResponse.createForError("邮箱已存在");
            }
        }
        else{
            return ServerResponse.createForError("参数错误");
        }
        return ServerResponse.createForSuccessMessage("参数校验通过");
    }

    @Override
    public ServerResponse<String> register(User user){
        ServerResponse<String> checkResult = checkField(CONSTANT.USER_FIELDS.USERNAME, user.getUsername());
        if(!checkResult.isSuccess()){
            return checkResult;
        }
        checkResult = checkField(CONSTANT.USER_FIELDS.EMAIL, user.getEmail());
        if(!checkResult.isSuccess()){
            return checkResult;
        }
        checkResult = checkField(CONSTANT.USER_FIELDS.PHONE, user.getPhone());
        if(!checkResult.isSuccess()){
            return checkResult;
        }
        user.setPassword(MD5Util.md5Encrypt32Upper(user.getPassword()));
        user.setRole(CONSTANT.ROLE.CUSTOMER);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        int rows = userMapper.insert(user);
        if(rows == 0){
            return ServerResponse.createForError("注册用户失败");
        }
        return ServerResponse.createForSuccessMessage("注册用户成功");
    }

    @Override
    public ServerResponse<String> getForgetQuestion(String username){
        ServerResponse<String> checkResult = this.checkField(CONSTANT.USER_FIELDS.USERNAME,username);
        if(checkResult.isSuccess()){
            return ServerResponse.createForError("用户名不存在");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);

        String question = userMapper.selectOne(Wrappers.<User>query().eq("username",username)).getQuestion();
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createForSuccess(question);
        }
        return ServerResponse.createForError("密码问题为空");
    }

    @Override
    public ServerResponse<String> checkForgetAnswer(String username, String question,String answer){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username).eq("question", question).eq("answer",answer);
        long rows = userMapper.selectCount(queryWrapper);
        if(rows > 0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCacheUtil.setToken(username, forgetToken);
            System.out.println(username+":"+forgetToken);
            return ServerResponse.createForSuccess(forgetToken);
        }
        return ServerResponse.createForError("密码保护问题答案错误");
    }

    @Override
    public ServerResponse<String> resetForgetPassword(String username, String newPassword, String forgetToken){
        ServerResponse<String> checkResult = this.checkField(CONSTANT.USER_FIELDS.USERNAME,username);
        if(checkResult.isSuccess()){
            return ServerResponse.createForError("用户名不存在");
        }
        String token = TokenCacheUtil.getToken(username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createForError("token无效或已过期");
        }
        if(StringUtils.equals(token, forgetToken)){
            String md5Password = MD5Util.md5Encrypt32Upper(newPassword);
            User user = new User();
            user.setUsername(username);
            user.setPassword(md5Password);

            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("username", username);
            updateWrapper.set("password", user.getPassword());
            int rows = userMapper.update(user, updateWrapper);

            if(rows > 0){
                return ServerResponse.createForSuccessMessage("通过忘记密码问题答案，重置密码成功");
            }
            return ServerResponse.createForError("通过忘记密码问题答案，重置密码失败,请重新获取token");
        }else{
            return ServerResponse.createForError("token错误，请重新获取token");
        }
    }

    @Override
    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",user.getId());
        queryWrapper.eq("password", MD5Util.md5Encrypt32Upper(oldPassword));
        long rows = userMapper.selectCount(queryWrapper);
        if(rows == 0){
            return ServerResponse.createForError("旧密码错误");
        }

        user.setPassword(MD5Util.md5Encrypt32Upper(newPassword));

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",user.getId());
        updateWrapper.set("password",user.getPassword());

        rows = userMapper.update(user,updateWrapper);

        if(rows > 0 ){
            return ServerResponse.createForSuccessMessage("密码更新成功");
        }
        return ServerResponse.createForError("密码更新失败");
    }

//    @Override
//    public ServerResponse<String> updateUserInfo(User user){
//
//        //检查更新的email是否可用
//        ServerResponse<String> checkResult = checkField(CONSTANT.USER_FIELDS.EMAIL, user.getEmail());
//        if(!checkResult.isSuccess()){
//            return checkResult;
//        }
//        //检查更新的phone是否可用
//        checkResult = checkField(CONSTANT.USER_FIELDS.PHONE, user.getPhone());
//        if(!checkResult.isSuccess()){
//            return checkResult;
//        }
//
//        user.setUpdateTime(LocalDateTime.now());
//        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("id",user.getId());
//        updateWrapper.set("email",user.getEmail());
//        updateWrapper.set("phone", user.getPhone());
//        updateWrapper.set("question", user.getQuestion());
//        updateWrapper.set("answer", user.getAnswer());
//        updateWrapper.set("update_time", user.getUpdateTime());
//        long rows = userMapper.update(user,updateWrapper);
//
//        if(rows > 0){
//            return ServerResponse.createForSuccessMessage("更新用户信息成功");
//        }
//        return ServerResponse.createForError("更新用户信息失败");
//    }

    @Override
    public ServerResponse<String> updateUserInfo(Integer id,String type,String edit) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        User user = new User();
        user.setId(id);
        updateWrapper.eq("id",id);
        updateWrapper.set(type,edit);
        long rows = userMapper.update(user,updateWrapper);
        if(rows > 0){
            return ServerResponse.createForSuccessMessage("更新用户"+type+"成功");
        }
        return ServerResponse.createForError("更新用户"+type+"失败");
    }

    @Override
    public ServerResponse<User> getUserDetail(Integer userId){
        User user = userMapper.selectById(userId);
        if(user == null){
            return ServerResponse.createForError("找不到当前用户信息");
        }
//        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createForSuccess(user);
    }

}
