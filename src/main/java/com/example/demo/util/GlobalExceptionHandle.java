package com.example.demo.util;

import com.example.demo.common.CommonResponse;
import com.example.demo.common.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResponse<String> handleValidatedException(ConstraintViolationException exception){
        return CommonResponse.createForError(ResponseCode.ARGUMENTILEGAL.getCode(),exception.getMessage());
    }
}
