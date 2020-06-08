package org.csu.csumall.mapper;

import org.csu.csumall.entity.Cart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartMapperTest {

    @Autowired
    private CartMapper cartMapper;

    @Test
    public void selectList()
    {
       List<Cart> cartList = cartMapper.selectList(null);
        System.out.println(cartList.size());
    }

}