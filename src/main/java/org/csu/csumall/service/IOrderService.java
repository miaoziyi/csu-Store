//package org.csu.csumall.service;
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import org.csu.csumall.common.ServerResponse;
//import org.csu.csumall.vo.OrderVo;
//
//import java.util.Map;
//
//public interface IOrderService {
//
//    public ServerResponse createOrder(Integer userId, Integer shippingId);
//
//    public ServerResponse getOrderCartProduct(Integer userId);
//
//    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNumber);
//
//    public ServerResponse<Page> getOrderList(Integer userId, int pageNum, int pageSize);
//
//    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNumber);
//
//    public ServerResponse<String> cancel(Integer userId, Long orderNumber);
//
//    public ServerResponse pay(Long orderNumber, Integer userId, String path);
//
//    public ServerResponse aliCallback(Map<String ,String > params);
//
//    public ServerResponse<Page> manageList(int pageNum,int pageSize);
//
//    public ServerResponse<Page> manageSearch(Long orderNumber,int pageNum, int pageSize);
//
//    public ServerResponse<String> manageSendGoods(Long orderNumber);
//
//    public ServerResponse<OrderVo> manageDetail(Long orderNumber);
//
//}
