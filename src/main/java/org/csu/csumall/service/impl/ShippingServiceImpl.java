package org.csu.csumall.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Shipping;
import org.csu.csumall.mapper.ShippingMapper;
import org.csu.csumall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if( rowCount > 0 )
        {
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功", result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    @Override
    public ServerResponse<String> del(Integer userId, Integer shippingId) {
        int resultCount = shippingMapper.delete(
                Wrappers.<Shipping>lambdaUpdate().eq(Shipping::getUserId, userId).eq(Shipping::getId, shippingId)
        );
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        int rowCount = shippingMapper.update(shipping,
                Wrappers.<Shipping>lambdaUpdate().eq(Shipping::getUserId, userId).eq(Shipping::getId, shipping.getId()) );
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    @Override
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectOne(
                Wrappers.<Shipping>lambdaQuery().eq(Shipping::getUserId, userId).eq(Shipping::getId, shippingId)
        );
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess("查询地址成功",shipping);
    }

    @Override
    public ServerResponse<Page> list(Integer userId, int pageNum, int pageSize) {
        Page<Shipping> page = new Page<>(pageNum, pageSize);
        page = shippingMapper.selectPage(page, Wrappers.<Shipping>lambdaQuery().eq(Shipping::getUserId, userId) );
        return ServerResponse.createBySuccess(page);
    }

}
