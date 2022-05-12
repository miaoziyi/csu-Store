package org.csu.csumall.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Shipping;
import org.csu.csumall.service.IShippingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ShippingServiceImplTest {

    @Autowired
    private IShippingService shippingService;

    @Test
    public void add() {
        int userId = 1;
        Shipping shipping = new Shipping();
        shipping.setReceiverMobile("6789");
        shipping.setReceiverPhone("12345");
        shipping.setReceiverName("测试");
        shipping.setReceiverProvince("湖南");
        shipping.setReceiverCity("长沙");
        shipping.setReceiverDistrict("天心区");
        shipping.setReceiverAddress("铁道学院");
        shipping.setReceiverZip("10010");
        shipping.setUpdateTime(LocalDateTime.now());
        shipping.setCreateTime(LocalDateTime.now());
        ServerResponse response = shippingService.add(userId, shipping);
        System.out.println(  response.getMessage()  );
    }

    @Test
    public void del() {
        int userId = 1;
        int shippingId = 33;
        ServerResponse response = shippingService.del(userId, shippingId);
        System.out.println(response.getMessage());
    }

    @Test
    public void update() {
        int userId = 1;
        Shipping shipping = new Shipping();
        shipping.setId(33);
        shipping.setReceiverMobile("13100001234");
        shipping.setReceiverName("测试2");
        shipping.setReceiverZip("2222");
        shipping.setUpdateTime(LocalDateTime.now());
        ServerResponse response = shippingService.update(userId, shipping);
        System.out.println(response.getMessage());
    }

    @Test
    public void select() {
        int userId = 1;
        int shippingId = 33;
        ServerResponse response = shippingService.select(userId, shippingId);
        System.out.println(response.getMessage());
        Shipping shipping = (Shipping) response.getData();
        System.out.println( shipping.getReceiverAddress() );
    }

    @Test
    public void list() {
        int userId = 1;
        int pageNum = 1;
        int pageSize = 1;
        ServerResponse<Page> response = shippingService.list(userId, pageNum, pageSize);
        Page pageResult = response.getData();
        System.out.println("总记录:" + pageResult.getTotal());
        System.out.println("总页数:" + pageResult.getPages());
        List<Shipping> shippingList = pageResult.getRecords();
        shippingList.forEach(System.out::println);
    }

}