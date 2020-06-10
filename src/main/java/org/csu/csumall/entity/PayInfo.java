package org.csu.csumall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PayInfo {

    private Integer id;

    private Integer userId;

    private Long orderNo;

    private Integer payPlatform;

    private String platformNumber;

    private String platformStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
