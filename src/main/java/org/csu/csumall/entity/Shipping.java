package org.csu.csumall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Shipping {
    private Integer id;

    private Integer userId;

    private String receiverName;

    private String receiverPhone;

    private String receiverMobile;

    private String receiverProvince;

    private String receiverCity;

    private String receiverDistrict;

    private String receiverAddress;

    private String receiverZip;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
