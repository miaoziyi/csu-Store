package com.example.demo.common;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    ARGUMENTILEGAL(10,"ARGUMENTILEGAL");

    private final int code;
    private final String description;

    ResponseCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

}
