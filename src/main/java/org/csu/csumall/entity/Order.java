package org.csu.csumall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {

    //主键自增
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value="order_no",exist = true)
    private Long orderNumber;

    private Integer userId;

    private Integer shippingId;

    @TableField(value="payment_price")
    private BigDecimal payment;

    private Integer paymentType;

    private Integer postage;

    private Integer status;

    private LocalDateTime paymentTime;

    private LocalDateTime sendTime;

    private LocalDateTime endTime;

    private LocalDateTime closeTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
