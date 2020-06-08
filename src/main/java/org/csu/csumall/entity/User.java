package org.csu.csumall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private Integer id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String question;

    private String answer;

    private Integer role;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
