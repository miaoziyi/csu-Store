package org.csu.csumall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;

import java.time.LocalDateTime;

@Data
public class Cart {
    @TableId
    private Integer id;

    private Integer userId;

    private Integer productId;

    private Integer quantity;

    private Integer checked;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
