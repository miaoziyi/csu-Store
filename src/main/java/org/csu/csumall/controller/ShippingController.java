package org.csu.csumall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.csumall.common.CONSTANT;
import org.csu.csumall.common.ResponseCode;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Shipping;
import org.csu.csumall.entity.User;
import org.csu.csumall.service.IShippingService;
import org.csu.csumall.utils.CookieUtil;
import org.csu.csumall.utils.JSONUtil;
import org.csu.csumall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;
    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping("add")
    @ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping, HttpServletRequest request){
        String sessionId = CookieUtil.readLoginToken(request);
        User user = JSONUtil.string2Obj((String) redisUtil.getRedisTemplate().opsForValue().get(sessionId),User.class);
        if(user ==null)
        {
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.add(user.getId(), shipping);
    }

    @RequestMapping("del")
    @ResponseBody
    public ServerResponse del(HttpSession session,Integer shippingId, HttpServletRequest request){
        String sessionId = CookieUtil.readLoginToken(request);
        User user = JSONUtil.string2Obj((String) redisUtil.getRedisTemplate().opsForValue().get(sessionId),User.class);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.del(user.getId(),shippingId);
    }

    @RequestMapping("update")
    @ResponseBody
    public ServerResponse update(HttpSession session,Shipping shipping, HttpServletRequest request){
        String sessionId = CookieUtil.readLoginToken(request);
        User user = JSONUtil.string2Obj((String) redisUtil.getRedisTemplate().opsForValue().get(sessionId),User.class);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.update(user.getId(),shipping);
    }

    @RequestMapping("select")
    @ResponseBody
    public ServerResponse<Shipping> select(HttpSession session, Integer shippingId, HttpServletRequest request){
        String sessionId = CookieUtil.readLoginToken(request);
        User user = JSONUtil.string2Obj((String) redisUtil.getRedisTemplate().opsForValue().get(sessionId),User.class);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.select(user.getId(),shippingId);
    }

    @RequestMapping("list")
    @ResponseBody
    public ServerResponse<Page> list(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                     HttpSession session, HttpServletRequest request){
        String sessionId = CookieUtil.readLoginToken(request);
        User user = JSONUtil.string2Obj((String) redisUtil.getRedisTemplate().opsForValue().get(sessionId),User.class);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(user.getId(),pageNum,pageSize);
    }

}
