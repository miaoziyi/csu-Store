package org.csu.csumall.service.impl;

import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.service.ICartService;
import org.csu.csumall.vo.CartVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CartServiceImplTest {

    @Autowired
    private ICartService iCartService;

    @Test
    public void add() {
        int userId = 21;
        int productId = 29;
        int count = 1;
        ServerResponse<CartVo> response = iCartService.add(userId, productId, count);
        assertEquals(response.getData().getCartProductVoList().size(), 3);
    }

    @Test
    public void update() {
        int userId = 21;
        int productId = 27;
        int count = 3;
        ServerResponse<CartVo> response = iCartService.update(userId, productId, count);
        assertEquals(response.getData().getCartProductVoList().size(), 3);
    }

    @Test
    public void deleteProduct() {
        int userId = 21;
        String productIds = "27,29";
        ServerResponse<CartVo> response = iCartService.deleteProduct(userId, productIds);
        assertEquals(response.getData().getCartProductVoList().size(), 2);
    }

    @Test
    public void list() {

    }

    @Test
    public void selectOrUnSelect() {
        int userId = 21;
        int productId = 28;
        int checked = 0;
        ServerResponse<CartVo> response = iCartService.selectOrUnSelect(userId, productId, checked);
        assertEquals(response.getData().getCartProductVoList().size(), 2);
    }

    @Test
    public void getCartProductCount() {
        int userId = 21;
        ServerResponse<Integer> response = iCartService.getCartProductCount(userId);
        System.out.println( response.getData() );
    }


}