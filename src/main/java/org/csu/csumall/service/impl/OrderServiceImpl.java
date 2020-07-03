package org.csu.csumall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.csu.csumall.common.Const;
import org.csu.csumall.common.ServerResponse;
import org.csu.csumall.entity.*;
import org.csu.csumall.mapper.*;
import org.csu.csumall.service.IOrderService;
import org.csu.csumall.utils.BigDecimalUtil;
import org.csu.csumall.utils.DateTimeUtil;
import org.csu.csumall.utils.FTPUtil;
import org.csu.csumall.utils.PropertiesUtil;
import org.csu.csumall.vo.OrderItemVo;
import org.csu.csumall.vo.OrderProductVo;
import org.csu.csumall.vo.OrderVo;
import org.csu.csumall.vo.ShippingVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    private static AlipayTradeService tradeService;
    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         并会打印一些信息如:pid,appid等
         */
        Configs.init("zfbinfo.properties");
        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    /**
     * 根据用户和收货地址创建订单
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse createOrder(Integer userId,Integer shippingId){
        //从购物车中获取数据
        //写了上面lambdaQuery()就不需要对应数据库  而是对应了那个Cart对象 下面则需要和数据库的的列名一一对应
        //List<Cart> cartList = cartMapper.selectList(Wrappers.<Cart>lambdaQuery().eq(Cart::getUserId,userId));
        //List<Cart> cartList = cartMapper.selectList(Wrappers.<Cart>query().eq("user_id",userId));
        QueryWrapper<Cart> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("checked", Const.Cart.CHECKED);
        List<Cart> cartList = cartMapper.selectList(query);
        //计算这个订单的总价
        ServerResponse ServerResponse = this.getCartOrderItem(userId, cartList); //this可省略
        if( !ServerResponse.isSuccess() ) {
            return ServerResponse;
        }
        //System.out.println(ServerResponse.getData());
        List<OrderItem> orderItemList= (List<OrderItem>)ServerResponse.getData();//从json格式强转回来
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);

        //生成订单
        Order order = this.assembleOrder(userId, shippingId, payment);
        if(order == null){
            return ServerResponse.createByErrorMessage("生成订单错误");
        }
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for(OrderItem orderItem : orderItemList){
            orderItem.setOrderNumber(order.getOrderNumber());
        }
        //mybatis 批量插入 //todo 批量插入方法需要写
        for (OrderItem orderItem: orderItemList){
            orderItemMapper.insert(orderItem);
        }

        //生成成功，我们要减少我们产品的库存
        this.reduceProductStock(orderItemList);

        //清空购物车
        this.cleanCart(cartList);

        //返回给前端的数据
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 获取用户订单基本信息
     * @param userId
     * @return
     */
    @Override
    public ServerResponse getOrderCartProduct(Integer userId){//可能有多个返回值域对象 ServerResponse 或 orderProductVo所以不好写返回值域对象
        OrderProductVo orderProductVo = new OrderProductVo();
        //从购物车获取勾选了的商品
        QueryWrapper<Cart> query = new QueryWrapper<>();
        query.eq("user_id",userId).eq("checked",Const.Cart.CHECKED);
        List<Cart> cartList= cartMapper.selectList(query);
        ServerResponse ServerResponse = this.getCartOrderItem(userId,cartList);
        if(!ServerResponse.isSuccess()){
            return ServerResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)ServerResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem: orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }

    /**
     * 获取单个订单详情
     * @param userId
     * @param orderNumber
     * @return
     */
    @Override
    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNumber){ //只有一个返回就应该标上范型
        QueryWrapper<Order> query = new QueryWrapper<>();
        query.eq("order_no",orderNumber).eq("user_id",userId);
        Order order = orderMapper.selectOne(query);
        if(order != null){
            QueryWrapper<OrderItem> queryOrderItem = new QueryWrapper<>();
            queryOrderItem.eq("order_no", orderNumber).eq("user_id", userId);
            List<OrderItem> orderItemList = orderItemMapper.selectList(queryOrderItem);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("没有找到该订单");
    }

    /**
     * 获取用户的所有订单
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<Page> getOrderList(Integer userId, int pageNum, int pageSize){
        Page<Order> page = new Page<>(pageNum, pageSize);
        page = orderMapper.selectPage(page, Wrappers.<Order>query().eq("user_id",userId));
        List<Order> orderList = page.getRecords();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);
        Page<OrderVo> pageResult = new Page<>();
        // set page中的数据
        pageResult.setCurrent(page.getCurrent());
        pageResult.setTotal(page.getTotal());
        pageResult.setPages(page.getPages());
        pageResult.setSize(page.getSize());
        pageResult.setRecords(orderVoList);

        return ServerResponse.createBySuccess(pageResult);
     }


    /**
     * 查看订单支付状态
     * @param userId
     * @param orderNumber
     * @return
     */
    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNumber){
        QueryWrapper<Order> query = new QueryWrapper<>();
        query.eq("order_no",orderNumber).eq("user_id",userId);
        Order order = orderMapper.selectOne(query);
        if(order ==null ){
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    /**
     * 根据用户ID和订单号取消订单
     * @param userId
     * @param orderNumber
     * @return
     */
    @Override
    public ServerResponse<String> cancel(Integer userId, Long orderNumber){
        QueryWrapper<Order> query = new QueryWrapper<>();
        query.eq("order_no",orderNumber).eq("user_id",userId);
        Order order = orderMapper.selectOne(query);
        if(order == null ){
            return ServerResponse.createByErrorMessage("该用户此订单不存在");
        }
        if(order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("已付款，无法取消订单");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

        int row = orderMapper.updateById(updateOrder);
        if(row > 0){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    /**
     * 封装订单vo对象
     * @param orderList
     * @param userId
     * @return
     */
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if(userId == null){
                //管理员查询的时候不需要传userId
                orderItemList = orderItemMapper.selectList(Wrappers.<OrderItem>query().eq("order_no",order.getOrderNumber()));
            }else {
                QueryWrapper<OrderItem> query = new QueryWrapper<>();
                query.eq("order_no",order.getOrderNumber()).eq("user_id",userId);
                orderItemList = orderItemMapper.selectList(query);
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /**
     * 减少商品库存
     * @param orderItemList
     */
    private void reduceProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem : orderItemList){
            //Product product = productMapper.selectOne(Wrappers.<Product>lambdaQuery().eq(Product::getId,orderItem.getProductId()));
            Product product = productMapper.selectOne(Wrappers.<Product>query().eq("id",orderItem.getProductId()));
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateById(product);
        }
    }

    /**
     * 清空购物车
     * @param cartList
     */
    private void cleanCart(List<Cart> cartList){
        QueryWrapper<Cart> query = new QueryWrapper<>();
        for(Cart cart : cartList){
            query.eq("id",cart.getId()).eq("checked",Const.Cart.CHECKED);
            cartMapper.delete(query);
        }
    }

    /**
     * 组装订单信息
     * @param userId
     * @param shippingId
     * @param payment
     * @return
     */
    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order = new Order();
        long orderNo = this.generateOrderNumber();
        order.setOrderNumber(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);

        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        //发货时间等等
        //付款时间
        int rowCount = orderMapper.insert(order);
        if(rowCount > 0){
            return order;
        }
        return null;
    }

    /**
     * 生成订单号
     * @return
     */
    private long generateOrderNumber(){
        //生成订单号，后面的随机数是为了避免订单号重复而导致的下单失败
        long currentTime = System.currentTimeMillis();
        return currentTime+new Random().nextInt(100);
    }

    /**
     * 组装订单VO对象
     * @param order
     * @param orderItemList
     * @return
     */
    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNumber());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectOne(Wrappers.<Shipping>query().eq("id",order.getShippingId()));
        if(shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;

    }

    /**
     * 组装订单中一项商品的VO
     * @param orderItem
     * @return
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNumber());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));

        return orderItemVo;
    }

    /**
     * 组装收货信息VO
     * @param shipping
     * @return
     */
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return shippingVo;
    }

    /**
     * 获取订单总价
     * @param orderItemList
     * @return
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    /**
     * 从购物车生成OrderItem
     * @param userId
     * @param cartList
     * @return
     */
    private ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList = Lists.newArrayList();
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for(Cart cartItem:cartList){
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectOne(Wrappers.<Product>query().eq("id",cartItem.getProductId()));
            if(Const.ProductStatusEnum.ON_SALE.getCode()!= product.getStatus()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"已经下架");
            }
            //检验库存
            if(cartItem.getQuantity()>product.getStock()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItem.setCreateTime(LocalDateTime.now());
            orderItem.setUpdateTime(LocalDateTime.now());
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 支付订单
     * @param orderNumber
     * @param userId
     * @param path
     * @return
     */
    public ServerResponse pay(Long orderNumber, Integer userId, String path) {
        Map<String, String> resultMap = Maps.newHashMap();
        QueryWrapper<Order> queryorderNoByuserId = new QueryWrapper<>();
        queryorderNoByuserId.eq("user_id", userId).eq("order_no", orderNumber);
        Order order = orderMapper.selectOne(queryorderNoByuserId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNumber()));
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNumber().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("CSUMall扫码支付，订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        QueryWrapper<OrderItem> queryOrderNoUserId = new QueryWrapper<>();
        queryOrderNoUserId.eq("order_no",orderNumber).eq("user_id",userId);
        List<OrderItem> orderItemList = orderItemMapper.selectList(queryOrderNoUserId);
        for (OrderItem orderItem : orderItemList) {
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods);
        }
        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String qrPath = String.format( path + "/qr-%s.png", response.getOutTradeNo() );
                String qrFileName = String.format( "qr-%s.png", response.getOutTradeNo() );
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path, qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码异常", e);
                    e.printStackTrace();
                }

                logger.info("qrPath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);
                return ServerResponse.createBySuccess(resultMap);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }

    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    /**
     * 支付宝回调
     * @param params
     * @return
     */
    public ServerResponse aliCallback(Map<String ,String > params){
        Long orderNumber = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectOne(Wrappers.<Order>query().eq("order_no",orderNumber));
        if(order == null){
            return ServerResponse.createByErrorMessage("非本商城的订单，回调忽略");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateById(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNumber());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }


    //backend

    /**
     *  后台查看订单
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<Page> manageList(int pageNum, int pageSize){
        Page<Order> page = new Page<>(pageNum, pageSize);
        page = orderMapper.selectPage( page,null);
        List<Order> orderList = page.getRecords();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,null);
        Page<OrderVo> pageResult= new Page<>();
        // 组装vo page
        pageResult.setCurrent(page.getCurrent());
        pageResult.setTotal(page.getTotal());
        pageResult.setPages(page.getPages());
        pageResult.setSize(page.getSize());
        pageResult.setRecords(orderVoList);

        return ServerResponse.createBySuccess(pageResult);
    }


    /**
     * 管理员查看订单详情
     * @param orderNumber
     * @return
     */
    public ServerResponse<OrderVo> manageDetail(Long orderNumber){
        Order order = orderMapper.selectOne(Wrappers.<Order>query().eq("order_no",orderNumber));
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.selectList(Wrappers.<OrderItem>query().eq("order_no",orderNumber));
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    /**
     *  根据订单号查看订单
     * @param orderNumber
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<Page> manageSearch(Long orderNumber, int pageNum, int pageSize){
        //这里实际是根据Order查询分页的，现在只是精准查询所以只有一页，以后需要进行模糊匹配所以会有很多页，因此要根据Order分页
        Page<Order> orderPage = new  Page<>(pageNum,pageSize);;
        //以后查询条件改为模糊查询
        List<Order> orderList = orderMapper.selectPage(orderPage,Wrappers.<Order>query().eq("order_no",orderNumber)).getRecords();

        Page<OrderVo> orderVoresult = new Page<>();
        if(!orderList.isEmpty()) {
            for (Order orderitem : orderList) {
                List<OrderItem> orderItemList = orderItemMapper.selectList(Wrappers.<OrderItem>query().eq("order_no", orderNumber));
                OrderVo orderVo = assembleOrderVo(orderitem, orderItemList);
                List<OrderVo> orderVoList = Lists.newArrayList(orderVo);
                orderVoresult.setRecords(orderVoList);
            }
            orderVoresult.setCurrent(pageNum);
            orderVoresult.setSize(pageSize);
            orderVoresult.setTotal(orderPage.getTotal());
            orderVoresult.getRecords().forEach(System.out::println);
            return ServerResponse.createBySuccess(orderVoresult);
        }
        return ServerResponse.createByErrorMessage("没有符合条件的订单");
    }

    /**
     * 订单发货
     * @param orderNumber
     * @return
     */
    public ServerResponse<String> manageSendGoods(Long orderNumber){
        Order order = orderMapper.selectOne(Wrappers.<Order>query().eq("order_no",orderNumber));
        if(order != null){
            if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(LocalDateTime.now());
                orderMapper.updateById(order);
                return ServerResponse.createBySuccess("发货成功");
            }
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }


}
