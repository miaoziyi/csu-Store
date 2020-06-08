package org.csu.csumall.entity;

import lombok.*;

import java.time.LocalDateTime;

@Data
public class Cart {

    private Integer id;

    private Integer userId;

    private Integer productId;

    private Integer quantity;

    private Integer checked;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
