package com.example.demo.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
//非空的数据才会被序列化成json
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {
    private int status;
    private String message;
    private T data;

    private CommonResponse(int status) {
        this.status = status;
    }

    private CommonResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public CommonResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    private CommonResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    @JsonIgnore//不要客户端看到
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public static <T> CommonResponse<T> createForSuccess() {
        return new CommonResponse<>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> CommonResponse<T> createForSuccess(T data) {
        return new CommonResponse<>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> CommonResponse<T> createForSuccessMessage(String msg) {
        return new CommonResponse<>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> CommonResponse<T> createForSuccess(String msg, T data) {
        return new CommonResponse<>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> CommonResponse<T> createForError(){
        return new CommonResponse<>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDescription());
    }
    public static <T> CommonResponse<T> createForError(String msg){
        return new CommonResponse<>(ResponseCode.ERROR.getCode(), msg);
    }
    public static <T> CommonResponse<T> createForError(int code, String msg){
        return new CommonResponse<>(code, msg);
    }

}
