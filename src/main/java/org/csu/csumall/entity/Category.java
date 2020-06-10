package org.csu.csumall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {

    private Integer id;

    private Integer parentId;

    private String name;

    private Boolean status;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
