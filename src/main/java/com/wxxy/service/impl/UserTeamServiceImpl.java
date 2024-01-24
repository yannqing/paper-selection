package com.wxxy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.domain.UserTeam;
import com.wxxy.service.UserTeamService;
import com.wxxy.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 67121
* @description 针对表【user_team(用户教师队伍关系)】的数据库操作Service实现
* @createDate 2024-01-24 00:58:08
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




