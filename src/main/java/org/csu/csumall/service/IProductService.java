package org.csu.csumall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Product;
import org.csu.csumall.vo.ProductDetailVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface IProductService {

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse updateProductStatus(Integer productId, Integer status);

    ServerResponse addProduct(Product product);

    ServerResponse upload(MultipartFile file, HttpServletRequest request);

    ServerResponse<Page> getProductList(Integer categoryId, String keyword , Integer pageNum, Integer pageSize, String orderBy);

}
