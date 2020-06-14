package org.csu.csumall.service;

import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Category;

import java.util.List;

public interface ICategoryService {

    ServerResponse addCategory(Integer parentId, String categoryName);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenCategory(Integer categoryId);

    ServerResponse<List<Integer>> getAllChildren(Integer categoryId);

}
