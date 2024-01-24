package com.wxxy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> implements Serializable {
    private Integer code;
    private T data;
    private String msg;

    public BaseResponse(Integer code, T data) {
        this.code = code;
        this.data = data;
    }
}
