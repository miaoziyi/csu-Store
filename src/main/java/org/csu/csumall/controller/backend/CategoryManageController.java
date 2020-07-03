package org.csu.csumall.controller.backend;

import org.csu.csumall.common.Const;
import org.csu.csumall.common.ResponseCode;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Category;
import org.csu.csumall.entity.User;
import org.csu.csumall.service.ICategoryService;
import org.csu.csumall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private ICategoryService iCategoryService;

    @Autowired
    private IUserService iUserService;
    /*
     * 获取子品类
     * */
    @RequestMapping("get_category.do")
    @ResponseBody
    //todo 把defaultvalue变为常量
    public ServerResponse<List<Category>> getChildrenCategory(HttpSession session,
            @RequestParam(name = "categoryId",defaultValue = "0") Integer categoryId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.getChildrenCategory(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /*
     * 获取所有子品类
     * */
    @RequestMapping("get_all_category.do")
    @ResponseBody
    //todo 把defaultvalue变为常量
    public ServerResponse<List<Integer>> getAllChildrenCategory(HttpSession session,
            @RequestParam(name = "categoryId",defaultValue = "0") Integer categoryId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return  iCategoryService.getAllChildren(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /*
     * 增加品类
     * */
    @RequestMapping("add_category.do")
    @ResponseBody
    public  ServerResponse addCategory(HttpSession session, Category category)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.addCategory(category.getParentId(), category.getName());
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /*
     * 修改品类
     * */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse updateCategoryName(HttpSession session, Category category)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.updateCategoryName(category.getId(), category.getName());
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }


}
