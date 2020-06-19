package org.csu.csumall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.csu.csumall.common.Const;
import org.csu.csumall.common.ResponseCode;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.Cart;
import org.csu.csumall.entity.Product;


import org.csu.csumall.mapper.CartMapper;
import org.csu.csumall.mapper.ProductMapper;
import org.csu.csumall.service.ICartService;
import org.csu.csumall.utils.BigDecimalUtil;
import org.csu.csumall.utils.PropertiesUtil;
import org.csu.csumall.vo.CartProductVo;
import org.csu.csumall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加商品到购物车，如果购物车中已经有此商品，则更改数量即可
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if(productId == null || count == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectOne(
                Wrappers.<Cart>lambdaUpdate().eq(Cart::getUserId, userId).eq(Cart::getProductId, productId)
        );
        if( cart == null )
        {
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartItem.setCreateTime(LocalDateTime.now());
            cartItem.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cartItem);
        }
        else
        {
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateById(cart);
        }
        return this.list(userId);
    }

    /**
     * 更新购物车中某项商品的数量
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectOne(
                Wrappers.<Cart>lambdaUpdate().eq(Cart::getUserId, userId).eq(Cart::getProductId, productId)
        );
        if( cart != null )
        {
            cart.setQuantity(count);
            cart.setUpdateTime( LocalDateTime.now() );
        }
        cartMapper.updateById(cart);
        return this.list(userId);
    }

    /**
     * 根据用户ID 批量删除购物车中的商品
     * @param userId
     * @param productIds
     * @return
     */
    @Override
    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) {
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList))
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.delete(
                Wrappers.<Cart>lambdaUpdate().eq(Cart::getUserId, userId).in(Cart::getProductId, productList)
        );
        return this.list(userId);
    }

    /**
     * 展示购物车
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 根据用户ID和商品ID修改选中状态
     * @param userId
     * @param productId
     * @param checked
     * @return
     */
    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked) {
        Cart cart = new Cart();
        cart.setChecked(checked);
        LambdaUpdateWrapper<Cart> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Cart::getUserId, userId);
        if( productId != null )
        {
            lambdaUpdateWrapper.eq(Cart::getProductId, productId);
        }
        cartMapper.update(cart, lambdaUpdateWrapper);
        return this.list(userId);
    }

    /**
     * 根据用户ID获取购物车中所有商品的总数量
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if(userId == null){
            return ServerResponse.createByError();
        }
        QueryWrapper<Cart> QueryWrapper = new QueryWrapper<>();
        QueryWrapper.eq("user_id", userId).select("IFNULL(sum(quantity),0) as count");
        // todo 看看查询的结果能否从list修改为Int
        List<Map<String, Object>> resultList = cartMapper.selectMaps( QueryWrapper );
        return ServerResponse.createBySuccess( Integer.parseInt(resultList.get(0).get("count").toString()) );
    }

    /**
     * 根据userId, 判断该用户购物车是否全选
     * @param userId
     * @return
     */
    private boolean getAllCheckedStatus(Integer userId)
    {
        if(userId == null)
        {
            return false;
        }
        LambdaQueryWrapper<Cart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Cart::getChecked, 0).eq(Cart::getUserId, userId);
        return cartMapper.selectCount(lambdaQueryWrapper) == 0;
    }

    /**
     *  根据用户ID获取购物车VO信息
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId)
    {
        CartVo cartVo = new CartVo();

        List<Cart> cartList = cartMapper.selectList(
                Wrappers.<Cart>lambdaQuery().eq(Cart::getUserId, userId)
        );

        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");
        if( CollectionUtils.isNotEmpty(cartList) )
        {
            for(Cart cartItem : cartList)
            {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectById( cartItem.getProductId() );
                if( product != null )
                {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存
                    int buyLimitCount = 0;
                    if(product.getStock() >= cartItem.getQuantity()){
                        //库存充足的时候
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateById(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                if(cartItem.getChecked() == Const.Cart.CHECKED)
                {
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked( this.getAllCheckedStatus(userId) );
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

}
