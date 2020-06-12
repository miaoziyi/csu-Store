package org.csu.csumall.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Order;
import org.csu.csumall.service.IOrderService;
import org.csu.csumall.vo.OrderItemVo;
import org.csu.csumall.vo.OrderVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderServiceImplTest {

    @Autowired
    private IOrderService iOrderService;

    @Test
    public void createOrder() {
    }

    @Test
    public void getOrderCartProduct() {
    }

    @Test
    public void getOrderDetail() {
    }

    @Test
    public void getOrderList() {
        int userId = 1;
        int pageNum = 3;
        int pageSize = 3;
        ServerResponse<Page> response = iOrderService.getOrderList(userId, pageNum, pageSize);
        Page pageResult = response.getData();
        System.out.println("总记录:" + pageResult.getTotal());
        System.out.println("总页数:" + pageResult.getPages());
        List<OrderVo> orderList = pageResult.getRecords();
        orderList.forEach(System.out::println);
    }

    @Test
    public void queryOrderPayStatus() {
    }

    @Test
    public void cancel() {
    }

    @Test
    public void pay() {
    }

    @Test
    public void aliCallback() {
    }

    @Test
    public void manageList() {
        int pageNum = 2;
        int pageSize = 3;
        ServerResponse<Page> response = iOrderService.manageList(pageNum, pageSize);
        Page pageResult = response.getData();
        System.out.println("总记录:" + pageResult.getTotal());
        System.out.println("总页数:" + pageResult.getPages());
        List<OrderVo> orderList = pageResult.getRecords();
        orderList.forEach(System.out::println);
    }

    @Test
    public void manageDetail() {
    }

    @Test
    public void manageSearch() {
        Long orderNumber = 1492091141269L;
        int pageNum = 2;
        int pageSize = 2;
        ServerResponse<Page> response = iOrderService.manageSearch(orderNumber, pageNum, pageSize);
        Page pageResult = response.getData();
        System.out.println("总记录:" + pageResult.getTotal());
        System.out.println("总页数:" + pageResult.getPages());
        List<OrderVo> orderList = pageResult.getRecords();
        for (OrderVo orderVo : orderList) {
           List<OrderItemVo> itemVoList = orderVo.getOrderItemVoList();
           itemVoList.forEach(System.out::println);
        }
    }

    @Test
    public void manageSendGoods() {
    }
}