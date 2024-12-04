package com.wxxy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageBoardContentMessage<T> implements Serializable {
    private T user;
    // 消息内容
    private String content;
}
