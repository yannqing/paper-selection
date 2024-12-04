package com.wxxy.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wxxy.common.UserLoginState;
import com.wxxy.domain.MessageBoard;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.MessageBoardMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.MessageBoardService;
import com.wxxy.utils.RedisCache;
import com.wxxy.vo.MessageBoardContentMessage;
import com.wxxy.vo.TeacherVo;
import com.wxxy.vo.UserVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.message.Message;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;
import static com.wxxy.utils.CheckLoginUtils.checkTeacherLoginStatus;
import static com.wxxy.utils.CheckLoginUtils.checkUserLoginStatus;

@Service
public class MessageBoardServiceImpl implements MessageBoardService {

    @Resource
    private RedisCache redisCache;

    @Resource
    private MessageBoardMapper messageBoardMapper;

    @Resource
    private UserTeamMapper userTeamMapper;

    @Override
    public void studentSendMessage(String message, HttpServletRequest request) {
        // 鉴权
        User loginUser = checkUserLoginStatus(request, redisCache);

        // 判断学生是否入队
        UserTeam userTeam = userTeamMapper.selectOne(new QueryWrapper<UserTeam>().eq("userId", loginUser.getId()).eq("isJoin", 1));
        if (userTeam == null) {
            throw new IllegalArgumentException("您还未加入队伍，无法进行留言");
        }

        // 取出原来的消息内容
        MessageBoard messageBoardBefore = messageBoardMapper.selectOne(new QueryWrapper<MessageBoard>().eq("teacherId", userTeam.getTeacherId()));
        List<MessageBoardContentMessage> messageBoardContentMessages = null;
        if (messageBoardBefore == null) {
            messageBoardContentMessages = new ArrayList<>();
        } else {
            messageBoardContentMessages = JSON.parseArray(messageBoardBefore.getContent(), MessageBoardContentMessage.class);
        }

        // 构建消息
        MessageBoardContentMessage<UserVo> messageBoardContentMessage = new MessageBoardContentMessage<>();
        UserVo userVo = UserVo.userToVo(loginUser);
        messageBoardContentMessage.setUser(userVo);
        messageBoardContentMessage.setContent(message);
        messageBoardContentMessage.setSendTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        messageBoardContentMessages.add(messageBoardContentMessage);
        String messageContent = JSON.toJSONString(messageBoardContentMessages);

        // 构建数据
        MessageBoard messageBoard = new MessageBoard();
        messageBoard.setContent(messageContent);
        messageBoard.setTeacherid(userTeam.getTeacherId());
        messageBoard.setCreatetime(new Date());
        messageBoard.setUpdatetime(new Date());

        // 存储数据
        if (messageBoardBefore == null) {
            messageBoardMapper.insert(messageBoard);
        } else {
            messageBoardMapper.update(new UpdateWrapper<MessageBoard>().eq("teacherId", userTeam.getTeacherId()).set("content", messageContent));
        }
    }

    @Override
    public void teacherSendMessage(String message, HttpServletRequest request) {
        // 1. 鉴权
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);

        // 取出原来的消息内容
        List<MessageBoardContentMessage> messageBoardContentMessages = null;
        MessageBoard messageBoardBefore = messageBoardMapper.selectOne(new QueryWrapper<MessageBoard>().eq("teacherId", loginTeacher.getId()));
        if (messageBoardBefore == null) {
            messageBoardContentMessages = new ArrayList<>();
        } else {
            messageBoardContentMessages = JSON.parseArray(messageBoardBefore.getContent(), MessageBoardContentMessage.class);
        }

        // 构建消息
        MessageBoardContentMessage<TeacherVo> messageBoardContentMessage = new MessageBoardContentMessage<>();
        TeacherVo teacherVo = TeacherVo.teacherToVo(loginTeacher);
        messageBoardContentMessage.setUser(teacherVo);
        messageBoardContentMessage.setContent(message);
        messageBoardContentMessage.setSendTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        messageBoardContentMessages.add(messageBoardContentMessage);
        String messageContent = JSON.toJSONString(messageBoardContentMessages);

        // 构建数据
        MessageBoard messageBoard = new MessageBoard();
        messageBoard.setContent(messageContent);
        messageBoard.setTeacherid(loginTeacher.getId());
        messageBoard.setCreatetime(new Date());
        messageBoard.setUpdatetime(new Date());

        // 存储数据
        if (messageBoardBefore == null) {
            messageBoardMapper.insert(messageBoard);
        } else {
            messageBoardMapper.update(new UpdateWrapper<MessageBoard>().eq("teacherId", loginTeacher.getId()).set("content", messageContent));
        }
    }

    @Override
    public List<MessageBoardContentMessage> getMessageBoard(HttpServletRequest request, String symbol) {
        // 登录鉴权
        Long teacherId = null;

        if (symbol.equals("1")) {
            Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);
            teacherId = loginTeacher.getId();
        } else {
            User user = checkUserLoginStatus(request, redisCache);
            UserTeam userTeam = userTeamMapper.selectOne(new QueryWrapper<UserTeam>().eq("userId", user.getId()).eq("isJoin", 1));
            teacherId = userTeam.getTeacherId();
        }

        // 查看信息
        MessageBoard messageBoard = messageBoardMapper.selectOne(new QueryWrapper<MessageBoard>().eq("teacherId", teacherId));
        if (messageBoard == null) {
            return null;
        }
        List<MessageBoardContentMessage> messageBoardContentMessages = JSON.parseArray(messageBoard.getContent(), MessageBoardContentMessage.class);

        return messageBoardContentMessages;

    }
}
