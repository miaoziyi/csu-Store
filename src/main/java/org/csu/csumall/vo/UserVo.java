package org.csu.csumall.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVo {

    private Integer id;
    private String username;
    private String email;
    private String phone;
    private LocalDateTime createTime;

}
