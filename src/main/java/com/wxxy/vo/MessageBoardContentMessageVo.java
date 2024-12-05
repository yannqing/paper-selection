package com.wxxy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * 返回前端的留言板信息封装
 */
public class MessageBoardContentMessageVo<T> implements Serializable {
    // 消息唯一 id
    private String id;
    // 消息发送者
    private T user;
    // 消息内容
    private String content;
    // 消息发送时间
    private String sendTime;
}
