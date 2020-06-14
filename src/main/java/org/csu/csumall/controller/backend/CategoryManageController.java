package org.csu.csumall.controller.backend;

import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Category;
import org.csu.csumall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private ICategoryService iCategoryService;

    /*
     * 获取子品类
     * */
    @RequestMapping("getchildrencategory.do")
    @ResponseBody
    //todo 把defaultvalue变为常量
    public ServerResponse<List<Category>> getChildrenCategory(
            @RequestParam(name = "categoryId",defaultValue = "0") Integer categoryId){
        return iCategoryService.getChildrenCategory(categoryId);
    }

    /*
     * 获取所有子品类
     * */
    @RequestMapping("getallchildren.do")
    @ResponseBody
    //todo 把defaultvalue变为常量
    public ServerResponse<List<Integer>> getAllChildrenCategory(
            @RequestParam(name = "categoryId",defaultValue = "0") Integer categoryId){
        return  iCategoryService.getAllChildren(categoryId);
    }

    /*
     * 增加品类
     * */
    @RequestMapping("addcategory.do")
    @ResponseBody
    public  ServerResponse addCategory(
            @RequestParam(required = true) Integer parentId,
            @RequestParam(required = true) String categoryName){
        return iCategoryService.addCategory(parentId,categoryName);
    }

    /*
     * 修改品类
     * */
    @RequestMapping("updatecategory.do")
    @ResponseBody
    public ServerResponse updateCategoryName(@RequestParam(required = true) Integer categoryId,
                                              @RequestParam(required = true) String categoryName){
        return iCategoryService.updateCategoryName(categoryId,categoryName);
    }


}
