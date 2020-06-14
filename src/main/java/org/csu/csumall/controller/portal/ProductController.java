package org.csu.csumall.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.service.IProductService;
import org.csu.csumall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    /*
     * 获取商品详情
     * */
    @GetMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        return iProductService.getProductDetail(productId);
    }

    /*
     * 获取商品列表
     * */
    @GetMapping("getproductlist.do")
    @ResponseBody
    public ServerResponse<Page> getProduceList(
            @RequestParam(name = "categoryId",defaultValue = "0") Integer categoryId,
            @RequestParam(name = "keyword",required = false)String keyword,
            @RequestParam(name = "pagNum",defaultValue = "1")Integer pageNum,
            @RequestParam(name = "pagSize",defaultValue = "10")Integer pageSize,
            @RequestParam(name = "orderBy",defaultValue = "price_asc" )String orderBy){
        return iProductService.getProductList(categoryId,keyword,pageNum,pageSize,orderBy);
    }


}
