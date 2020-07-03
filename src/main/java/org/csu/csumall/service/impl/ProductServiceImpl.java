package org.csu.csumall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.csu.csumall.common.Const;
import org.csu.csumall.common.ResponseCode;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Category;
import org.csu.csumall.entity.Product;
import org.csu.csumall.mapper.CategoryMapper;
import org.csu.csumall.mapper.ProductMapper;
import org.csu.csumall.service.ICategoryService;
import org.csu.csumall.service.IFileService;
import org.csu.csumall.service.IProductService;
import org.csu.csumall.utils.DateTimeUtil;
import org.csu.csumall.utils.PropertiesUtil;
import org.csu.csumall.vo.ProductDetailVo;
import org.csu.csumall.vo.ProductListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private IFileService fileService;
    //todo 解决横向调用的问题
    @Autowired
    private ICategoryService categoryService;

    /*
     * 根据产品ID，获取产品详情
     * @productId
     * @return
     * */
    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectById(productId);
//        System.out.println(product.getId());
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("商品不在售，下架或其他情况");
        }
        //DTO data transfer object :用于数据传输的数据对象都可以叫DTO
        //BO business object :对和server层交互的数据，进行封装修改
        //Vo view object :对数据进行进一步的封装
        ProductDetailVo productDetailVO = entityToVo(product);
//        System.out.println(productDetailVO);
        return ServerResponse.createBySuccess(productDetailVO);
    }

    /*
     * 将产品实例数据进行包装
     * @product
     * @return
     * */
    private ProductDetailVo entityToVo(Product product){
        ProductDetailVo productDetailVO = new ProductDetailVo();

        productDetailVO.setId(product.getId());
        productDetailVO.setCategoryId((product.getCategoryId()));
        productDetailVO.setStatus(product.getStatus());
        productDetailVO.setName(product.getName());
        productDetailVO.setSubtitle(product.getSubtitle());
        productDetailVO.setMainImage(product.getMainImage());
        productDetailVO.setSubImages(product.getSubImages());
        productDetailVO.setPrice(product.getPrice());
        productDetailVO.setStock(product.getStock());
        productDetailVO.setDetail(product.getDetail());

        productDetailVO.setCreateTime( DateTimeUtil.dateToStr(product.getCreateTime()) );
        productDetailVO.setUpdateTime( DateTimeUtil.dateToStr(product.getUpdateTime()) );

        Category category = categoryMapper.selectById(product.getCategoryId());
        productDetailVO.setParentCategoryId(category.getParentId());

        productDetailVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://csu.edu.cn/"));
        return productDetailVO;
    }

    /**
     * 产品上下架
     * @param productId
     * @param status
     * @return
     */
    @Override
    public ServerResponse updateProductStatus(Integer productId, Integer status) {
        Product product = productMapper.selectById(productId);
        if(product == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),"没有此产品");
        }
        if(Const.ProductStatusEnum.isExist(status)){
            product.setStatus(status);
            try {
                productMapper.updateById(product);
                return ServerResponse.createBySuccessMessage("修改状态成功");
            }catch (Exception e){
                return ServerResponse.createByErrorMessage("修改状态失败");
            }
        }else {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),"状态参数错误");
        }
    }

    /**
     * 新增产品或者更新产品
     * @param product
     * @return
     */
    @Override
    public ServerResponse addProduct(Product product) {
        product.setUpdateTime(LocalDateTime.now());
        try {
            if(productMapper.selectById( product.getId() ) != null){
                productMapper.updateById(product);
            }else {
                product.setCreateTime(LocalDateTime.now());
                productMapper.insert(product);
            }
            return ServerResponse.createBySuccessMessage("新增产品成功");
        }catch (Exception e){
            return ServerResponse.createByErrorMessage("新增产品失败");
        }
    }

    /**
     * 上传图片
     * @param file
     * @param request
     * @return
     */
    @Override
    public ServerResponse upload(MultipartFile file, HttpServletRequest request) {
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = fileService.upLoadFile(file,path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
        Map fileMap = Maps.newHashMap();
        fileMap.put("uri",targetFileName);
        fileMap.put("url",url);
        return ServerResponse.createBySuccess("上传文件成功", fileMap);
    }

    /**
     * 根据品类Id，和关键字 查询产品列表
     * @param categoryId
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @Override
    public ServerResponse<Page> getProductList(Integer categoryId, String keyword, Integer pageNum, Integer pageSize, String orderBy) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Product> query = new QueryWrapper<>();
//        查询条件中加入分类查询
        List<Integer> categoryIdList = categoryService.getAllChildren(categoryId).getData();
        System.out.println(categoryIdList);
        query.in("category_id",categoryIdList);
//        List<Product> list = productMapper.selectList(query);
//        System.out.println(list);
        //关键字查询
        if (!StringUtils.isBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
            query.like("name",keyword);
        }
        //排序
        if(!StringUtils.isBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String [] orderbystring = orderBy.split("_");
                if(StringUtils.equals(orderbystring[1],"asc")){
                    query.orderByAsc(orderbystring[0]);
                }else if(StringUtils.equals(orderbystring[1],"desc")){
                    query.orderByDesc(orderbystring[0]);
                }
            }
        }
        page = productMapper.selectPage(page, query);
//        System.out.println(page.getRecords());

        List<ProductListVo> productListVOS = Lists.newArrayList();
        for(Product product : page.getRecords()){
            ProductListVo productListVo = entityToProductListVo(product);
            productListVOS.add(productListVo);
        }

        Page<ProductListVo> pageResult = new Page<>();
        pageResult.setRecords(productListVOS);
        pageResult.setSize(page.getSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setPages(page.getPages());
        pageResult.setCurrent(page.getCurrent());

        return ServerResponse.createBySuccess( pageResult );
    }


    private ProductListVo entityToProductListVo(Product product){
        ProductListVo productListVO = new ProductListVo();
        productListVO.setCategoryId(product.getCategoryId());
        productListVO.setId(product.getId());
        productListVO.setMainImage(product.getMainImage());
        productListVO.setStatus(product.getStatus());
        productListVO.setName(product.getName());
        productListVO.setPrice(product.getPrice());
        productListVO.setSubtitle(product.getSubtitle());
        productListVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return productListVO;
    }


}
