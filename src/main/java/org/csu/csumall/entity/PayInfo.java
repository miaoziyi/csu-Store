package org.csu.csumall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PayInfo {

    //主键自增
    @TableId(type = IdType.AUTO)

    private Integer id;

    private Integer userId;

    private Long orderNo;

    private Integer payPlatform;

    private String platformNumber;

    private String platformStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
