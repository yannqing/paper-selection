package com.wxxy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库留言板内容存储（json 格式）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageBoardContent {
    // 消息唯一 id
    private String id;
    // 学生 id，默认 null
    private Long userId;
    // 教师 id，默认 null
    private Long teacherId;
    // 消息具体内容
    private String content;
    // 消息发送时间
    private String sendTime;
}
