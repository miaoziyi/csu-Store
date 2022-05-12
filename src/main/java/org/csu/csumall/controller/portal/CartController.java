package org.csu.csumall.controller.portal;

import org.csu.csumall.common.CONSTANT;
import org.csu.csumall.common.ResponseCode;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.User;
import org.csu.csumall.service.ICartService;
import org.csu.csumall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    @RequestMapping("add")
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session, Integer count, Integer productId)
    {
        User user = (User) session.getAttribute(CONSTANT.CURRENT_USER);
        if( user == null )
        {
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(), productId, count);
    }

    @RequestMapping("update")
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session, Integer count, Integer productId)
    {
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(),productId,count);
    }

    @RequestMapping("delete_product")
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(HttpSession session,String productIds){
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProduct(user.getId(),productIds);
    }

    @RequestMapping("list")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.list(user.getId());
    }

    @RequestMapping("select_all")
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session)
    {
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),null, CONSTANT.Cart.CHECKED);
    }

    @RequestMapping("un_select_all")
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session)
    {
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),null, CONSTANT.Cart.UN_CHECKED);
    }

    @RequestMapping("select")
    @ResponseBody
    public ServerResponse<CartVo> select(HttpSession session,Integer productId){
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),productId, CONSTANT.Cart.CHECKED);
    }

    @RequestMapping("un_select")
    @ResponseBody
    public ServerResponse<CartVo> unSelect(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),productId, CONSTANT.Cart.UN_CHECKED);
    }

    @RequestMapping("get_cart_product_count")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session){
        User user = (User)session.getAttribute(CONSTANT.CURRENT_USER);
        if(user == null){
            return ServerResponse.createForSuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }


}
