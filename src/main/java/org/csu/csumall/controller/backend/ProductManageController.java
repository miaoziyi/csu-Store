package org.csu.csumall.controller.backend;

import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Product;
import org.csu.csumall.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("manage/product/")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    /*
     * 修改商品状态
     * */
    @GetMapping("updateproductstatus.do")
    @ResponseBody
    public ServerResponse updateProductStatus(Integer productId, Integer status){
        return iProductService.updateProductStatus(productId, status);
    }

    @GetMapping("addproduct.do")
    @ResponseBody
    public ServerResponse addProduct(Product product){
        return iProductService.addProduct(product);
    }

    /*
     * 上传文件
     * */
    @GetMapping("uploadfile")
    @ResponseBody
    public ServerResponse upload(@RequestParam(value = "upload_file",required = true) MultipartFile file, HttpServletRequest request){
        return iProductService.upload(file, request);
    }


}
