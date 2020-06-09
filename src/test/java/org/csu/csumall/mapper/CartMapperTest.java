package org.csu.csumall.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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

    @Test
    public void pagination()
    {
        int userId = 21;
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Page<Cart> cartPage = new Page<>(1,2);
        IPage<Cart> cartIPage = cartMapper.selectPage(cartPage, queryWrapper);
        System.out.println("总记录：" + cartIPage.getTotal() );
        System.out.println("总页数：" + cartIPage.getPages() );
        List<Cart> cartList = cartIPage.getRecords();
        cartList.forEach(System.out::println);
    }

}