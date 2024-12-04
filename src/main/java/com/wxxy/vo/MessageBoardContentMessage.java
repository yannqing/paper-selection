package com.wxxy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageBoardContentMessage<T> implements Serializable {
    // 消息发送者
    private T user;
    // 消息内容
    private String content;
    // 消息发送时间
    private String sendTime;
}
