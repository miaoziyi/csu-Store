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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        shipping.setCreateTime(LocalDateTime.now());
        shipping.setUpdateTime(LocalDateTime.now());
        int rowCount = shippingMapper.insert(shipping);
        if( rowCount > 0 )
        {
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createForSuccess("新建地址成功", result);
        }
        return ServerResponse.createForError("新建地址失败");
    }

    @Override
    public ServerResponse<String> del(Integer userId, Integer shippingId) {
        int resultCount = shippingMapper.delete(
                Wrappers.<Shipping>lambdaUpdate().eq(Shipping::getUserId, userId).eq(Shipping::getId, shippingId)
        );
        if(resultCount > 0){
            return ServerResponse.createForSuccessMessage("删除地址成功");
        }
        return ServerResponse.createForError("删除地址失败");
    }

    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setUpdateTime(LocalDateTime.now());

        int rowCount = shippingMapper.update(shipping,
                Wrappers.<Shipping>lambdaUpdate().eq(Shipping::getUserId, userId).eq(Shipping::getId, shipping.getId()) );
        if(rowCount > 0){
            return ServerResponse.createForSuccessMessage("更新地址成功");
        }
        return ServerResponse.createForError("更新地址失败");
    }

    @Override
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectOne(
                Wrappers.<Shipping>lambdaQuery().eq(Shipping::getUserId, userId).eq(Shipping::getId, shippingId)
        );
        if(shipping == null){
            return ServerResponse.createForError("无法查询到该地址");
        }
        return ServerResponse.createForSuccess("查询地址成功",shipping);
    }

    @Override
    public ServerResponse<Page> list(Integer userId, int pageNum, int pageSize) {
        Page<Shipping> page = new Page<>(pageNum, pageSize);
        page = shippingMapper.selectPage(page, Wrappers.<Shipping>lambdaQuery().eq(Shipping::getUserId, userId) );
        return ServerResponse.createForSuccess(page);
    }

}
