package org.csu.csumall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.csu.csumall.common.Const;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.common.TokenCache;
import org.csu.csumall.entity.User;
import org.csu.csumall.mapper.UserMapper;
import org.csu.csumall.service.IUserService;
import org.csu.csumall.utils.MD5Util;
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

    /**
     * 根据用户名和密码进行登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = checkUsername(username);//userMapper.checkUsername(username);
        if(resultCount == 0 ){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);

        User user  = userMapper.selectOne(Wrappers.<User>query()
                .eq("username",username)
                .eq("password",md5Password));//selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);
    }

    /**
     * 提交注册信息,进行注册
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user){
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 检查参数是否有效
     * @param str 待检查参数
     * @param type 参数类型
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type){
        if(StringUtils.isNotBlank(type)){
            //开始校验
            if(Const.USERNAME.equals(type)){
                int resultCount = checkUsername(str);
                if(resultCount > 0 ){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = checkEmail(str);
                if(resultCount > 0 ){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 查询密保问题
     * @param username
     * @return
     */
    @Override
    public ServerResponse selectQuestion(String username){

        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //检查成功,说明该用户名不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String question = userMapper.selectOne(
                Wrappers.<User>query().eq("username",username)).getQuestion();
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    /**
     * 根据用户名和密保问题验证密保答案正确性
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int resultCount = userMapper.selectCount(
                Wrappers.<User>query()
                        .eq("username",username)
                        .eq("question",question)
                        .eq("answer",answer));//checkAnswer(username,question,answer);
        if(resultCount>0){
            //说明问题及问题答案是这个用户的,并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }


    /**
     *根据用户名和token重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        if(StringUtils.equals(forgetToken, token)){
            String md5Password  = MD5Util.MD5EncodeUtf8(passwordNew);

            User user = new User();
            user.setPassword(md5Password);
            int rowCount = userMapper.update(user, Wrappers.<User>lambdaUpdate().eq(User::getUsername,username));//updatePasswordByUsername(username,md5Password);

            if(rowCount > 0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    /**
     * 修改密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        int resultCount = userMapper.selectCount(
                Wrappers.<User>query()
                        .eq("password",MD5Util.MD5EncodeUtf8(passwordOld))
                        .eq("id",user.getId()));//checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    /**
     * 修改个人信息
     * @param user
     * @return
     */
    @Override
    public ServerResponse<User> updateInformation(User user){
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCount = userMapper.selectCount(
                Wrappers.<User>query()
                        .eq("email",user.getEmail())
                        .ne("id",user.getId()));//checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已存在,请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    /**
     * 根据用户ID获取用户信息
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectById(userId);//selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 检查用户名是否存在
     * @param username
     * @return
     */
    private Integer checkUsername(String username){
        int rows = userMapper.selectCount(Wrappers.<User>query().eq("username",username));
        return rows;
    }

    /**
     * 检查邮箱是否存在
     * @param email
     * @return
     */
    private Integer checkEmail(String email){
        int rows = userMapper.selectCount(Wrappers.<User>query().eq("email",email));
        return rows;
    }

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    private Integer updateByPrimaryKeySelective(User user){
        User updateUser = new User();

        updateUser.setId(user.getId());

        if (user.getUsername() != null){
            updateUser.setUsername(user.getUsername());
        }
        if (user.getPassword() != null){
            updateUser.setPassword(user.getPassword());
        }
        if (user.getEmail() != null){
            updateUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null){
            updateUser.setPhone(user.getPhone());
        }
        if (user.getQuestion() != null){
            updateUser.setQuestion(user.getQuestion());
        }
        if (user.getAnswer() != null){
            updateUser.setAnswer(user.getAnswer());
        }
        if (user.getQuestion() != null){
            updateUser.setQuestion(user.getQuestion());
        }
        if (user.getRole() != null){
            updateUser.setRole(user.getRole());
        }
        if (user.getCreateTime() != null){
            updateUser.setCreateTime(user.getCreateTime());
        }
        if (user.getUpdateTime() != null){
            updateUser.setUpdateTime(LocalDateTime.now());
        }
        int rows = userMapper.update(updateUser,new QueryWrapper<User>().eq("id",updateUser.getId()));//updateById(updateUser);

        return rows;
    }



    //backend
    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse<Page> getUserList(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        page = userMapper.selectPage(page,null);
        List<User> userList = page.getRecords();
        List<UserVo> userVoList = this.assembleUserVoList(userList);
        Page<UserVo> pageResult = new Page<>();

        pageResult.setCurrent(page.getCurrent());
        pageResult.setTotal(page.getTotal());
        pageResult.setPages(page.getPages());
        pageResult.setSize(page.getSize());
        pageResult.setRecords(userVoList);

        return ServerResponse.createBySuccess(pageResult);
    }

    private List<UserVo> assembleUserVoList(List<User> userList)
    {
        List<UserVo> userVoList = new ArrayList<>();
        for (User user : userList) {
            UserVo userVo = new UserVo();
            userVo.setId(user.getId());
            userVo.setUsername(user.getUsername());
            userVo.setEmail(user.getEmail());
            userVo.setPhone(user.getPhone());
            userVo.setCreateTime(user.getCreateTime());
            userVoList.add(userVo);
        }
        return userVoList;
    }


}
