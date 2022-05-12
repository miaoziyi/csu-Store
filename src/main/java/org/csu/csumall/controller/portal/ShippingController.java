package org.csu.csumall.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.csumall.common.CONSTANT;
import org.csu.csumall.common.ResponseCode;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Shipping;
import org.csu.csumall.entity.User;
import org.csu.csumall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    @RequestMapping("add")
    @ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping)
    {
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null)
        {
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.add(user.getId(), shipping);
    }

    @RequestMapping("del")
    @ResponseBody
    public ServerResponse del(HttpSession session,Integer shippingId){
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.del(user.getId(),shippingId);
    }

    @RequestMapping("update")
    @ResponseBody
    public ServerResponse update(HttpSession session,Shipping shipping){
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.update(user.getId(),shipping);
    }

    @RequestMapping("select")
    @ResponseBody
    public ServerResponse<Shipping> select(HttpSession session, Integer shippingId){
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.select(user.getId(),shippingId);
    }

    @RequestMapping("list")
    @ResponseBody
    public ServerResponse<Page> list(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                     HttpSession session)
    {
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(user.getId(),pageNum,pageSize);
    }

}
