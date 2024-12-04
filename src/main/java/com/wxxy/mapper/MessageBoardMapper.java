package com.wxxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxxy.domain.MessageBoard;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageBoardMapper extends BaseMapper<MessageBoard> {
}