package com.wxxy.vo;

import lombok.Data;

@Data
public class CountOfTeamVo {
    //申请的人数
    Long selectedNum;
    //加入队伍的人数
    Long joinedNum;
    //队伍最大人数
    int maxNum;
}
