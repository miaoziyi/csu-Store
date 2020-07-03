package org.csu.csumall.controller.backend;

import org.csu.csumall.common.Const;
import org.csu.csumall.common.ResponseCode;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Product;
import org.csu.csumall.entity.User;
import org.csu.csumall.service.IProductService;
import org.csu.csumall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IUserService iUserService;

    /*
     * 修改商品状态
     * */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse updateProductStatus(HttpSession session, Integer productId, Integer status){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if( iUserService.checkAdminRole(user).isSuccess() )
        {
            return iProductService.updateProductStatus(productId, status);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse addProduct(HttpSession session, Product product){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if( iUserService.checkAdminRole(user).isSuccess() )
        {
            return iProductService.addProduct(product);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /*
     * 上传文件
     * */
    @RequestMapping("uploadfile.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file",required = true) MultipartFile file, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if( iUserService.checkAdminRole(user).isSuccess() )
        {
            return iProductService.upload(file, request);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }


    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if( iUserService.checkAdminRole(user).isSuccess() )
        {
            return iProductService.getProductDetail(productId);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session,
                                  @RequestParam(name = "categoryId", defaultValue = "0") Integer categoryId,
                                  @RequestParam(name = "keyword", required = false)String keyword,
                                  @RequestParam(name = "pageNum", defaultValue = "1")Integer pageNum,
                                  @RequestParam(name = "pageSize", defaultValue = "10")Integer pageSize,
                                  @RequestParam(name = "orderBy", defaultValue = "price_asc" )String orderBy)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if( iUserService.checkAdminRole(user).isSuccess() )
        {
            return iProductService.getProductList(categoryId,keyword,pageNum,pageSize,orderBy);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }


}
