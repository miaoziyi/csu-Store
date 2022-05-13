package org.csu.csumall.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItem {

    private Integer id;

    private Integer userId;
    @TableField(value="order_no",exist = true)
    private Long orderNumber;

    private Integer productId;

    private String productName;

    private String productImage;

    @TableField(value="current_price")
    private BigDecimal currentUnitPrice;

    private Integer quantity;

    private BigDecimal totalPrice;

    private LocalDateTime createTime;

    private LocalDateTime  updateTime;


}
