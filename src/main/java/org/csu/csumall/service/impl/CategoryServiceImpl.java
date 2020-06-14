package org.csu.csumall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.csu.csumall.common.Const;
import org.csu.csumall.common.ResponseCode;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Category;
import org.csu.csumall.mapper.CategoryMapper;
import org.csu.csumall.service.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service("categoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    /*
     * 添加产品分类
     * @param  parentId
     * @param categoryName
     * @return
     * */
    @Override
    public ServerResponse addCategory(Integer parentId, String categoryName) {
        //TODO 验证管理员权限
        //todo sortorder插入问题
        if( parentId == null || StringUtils.isBlank(categoryName) )
        {
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(Const.ProductStatusEnum.ON_SALE.getCode());
        category.setSortOrder(Const.SortOrderCode.First_category.getCode());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        try {
            categoryMapper.insert(category);
            return ServerResponse.createBySuccess("新增商品分类成功");
        }catch (Exception e){
            return ServerResponse.createByErrorMessage("新增商品分类失败");
        }
    }

    /*
     * 根据分类ID 获取品类子节点
     * @param  categoryId
     * @return
     * */
    public ServerResponse<List<Category>> getChildrenCategory(Integer categoryId){
        //todo 验证管理员权限，登录
        QueryWrapper<Category> query = new QueryWrapper<>();
        query.eq("parent_id",categoryId);
        List<Category> categoryList = categoryMapper.selectList(query);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("查找子分类时，没有找到该分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /*
     * 根据分类ID 获取品类所有子节点ID
     * @param  categoryId
     * @return
     * */
    public ServerResponse<List<Integer>> getAllChildren(Integer categoryId){
        //todo 验证管理员权限，登录
        Category category = categoryMapper.selectById(categoryId);
        if(category != null || categoryId == Const.root_category){
            Set<Category> categorySet = Sets.newHashSet();
            findAllChildrenCategory(categoryId, categorySet);
            List<Integer> categoryList = Lists.newArrayList();
            if (categoryId != null) {
                for (Category categoryItem : categorySet) {
                    categoryList.add(categoryItem.getId());
                }
            }
            return ServerResponse.createBySuccess(categoryList);
        }else {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),"此分类不存在");
        }
    }

    /*
     * 根据分类ID 递归查询所有子节点
     * @param  categoryId
     * @param categorySet
     * @return
     * */
    private Set<Category> findAllChildrenCategory(Integer categoryId, Set<Category> categorySet){
        //todo 验证管理员权限,登录
        Category category = categoryMapper.selectById(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        QueryWrapper<Category> query = new QueryWrapper<>();
        query.eq("parent_id",categoryId);
        List<Category> categoryList = categoryMapper.selectList(query);
        for (Category categoryItem : categoryList){
            findAllChildrenCategory( categoryItem.getId(), categorySet );
        }
        return categorySet;
    }

    /*
     * 根据分类ID 修改品类名字
     * @param  categoryId
     * @param categoryName
     * @return
     * */
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName){
        //todo 验证管理员权限,登录
        Category category = categoryMapper.selectById(categoryId);
        if(category != null){
            category.setName(categoryName);
            try {
                categoryMapper.updateById(category);
                return ServerResponse.createBySuccessMessage("修改品类名称成功");
            }catch (Exception e){
                return ServerResponse.createByErrorMessage("修改品类名称失败");
            }
        }else {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),"没有此分类");
        }
    }

    /*
     * 根据分类ID 删除品类
     * @param  categoryId
     * @return
     * */
    public ServerResponse deleteCategory(Integer categoryId,Integer deleteType){
        //todo 验证管理员权限,登录
        if(categoryMapper.selectById(categoryId) == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),"没有此分类");
        }
        QueryWrapper<Category> query = new QueryWrapper<>();
        query.eq("parent_id",categoryId);
        List<Category> categoryList = categoryMapper.selectList(query);

        if(!CollectionUtils.isEmpty(categoryList) && deleteType == Const.DeleteTypeCode.cascade.getCode()){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ERROR.getCode(),"该品类有子品类，请选择级联删除");
        }
        try{
            if(deleteType !=  Const.DeleteTypeCode.cascade.getCode()){
                categoryMapper.deleteById(categoryId);
            }else {
                //todo 删除品类未完成
                categoryMapper.deleteById(categoryId);
            }
            return ServerResponse.createBySuccessMessage("删除品类成功");
        }catch (Exception e){
            return ServerResponse.createByErrorMessage("删除品类失败");
        }
    }



}
