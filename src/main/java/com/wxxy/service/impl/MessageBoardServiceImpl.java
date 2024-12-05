package com.wxxy.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wxxy.domain.MessageBoard;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.MessageBoardMapper;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.MessageBoardService;
import com.wxxy.utils.RedisCache;
import com.wxxy.vo.MessageBoardContent;
import com.wxxy.vo.MessageBoardContentMessageVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

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
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public void studentSendMessage(String message, HttpServletRequest request) {
        // 1. 鉴权
        User loginUser = checkUserLoginStatus(request, redisCache);

        // 2. 判断学生是否入队
        UserTeam userTeam = userTeamMapper.selectOne(new QueryWrapper<UserTeam>().eq("userId", loginUser.getId()).eq("isJoin", 1));
        if (userTeam == null) {
            throw new IllegalArgumentException("您还未加入队伍，无法进行留言");
        }

        // 3. 获取所有留言
        MessageBoard messageBoardBefore = messageBoardMapper.selectOne(new QueryWrapper<MessageBoard>().eq("teacherId", userTeam.getTeacherId()));

        // 4. 新增并序列化
        String messageContent = getMessageBoardContent(message, 0, loginUser.getId(), userTeam.getTeacherId(), messageBoardBefore);

        // 5. 构建数据
        MessageBoard messageBoard = new MessageBoard();
        messageBoard.setContent(messageContent);
        messageBoard.setTeacherid(userTeam.getTeacherId());

        // 6. 存储数据
        if (messageBoardBefore == null) {
            messageBoard.setCreatetime(new Date());
            messageBoardMapper.insert(messageBoard);
        } else {
            messageBoardMapper.update(new UpdateWrapper<MessageBoard>().eq("teacherId", userTeam.getTeacherId()).set("content", messageContent));
        }
    }

    @Override
    public void teacherSendMessage(String message, HttpServletRequest request) {
        // 1. 鉴权
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);

        // 2. 获取所有留言
        MessageBoard messageBoardBefore = messageBoardMapper.selectOne(new QueryWrapper<MessageBoard>().eq("teacherId", loginTeacher.getId()));

        // 3. 新增并序列化
        String messageContent = getMessageBoardContent(message, 1, null, loginTeacher.getId(), messageBoardBefore);

        // 4. 构建数据
        MessageBoard messageBoard = new MessageBoard();
        messageBoard.setContent(messageContent);
        messageBoard.setTeacherid(loginTeacher.getId());

        // 5. 存储数据
        if (messageBoardBefore == null) {
            messageBoard.setCreatetime(new Date());
            messageBoardMapper.insert(messageBoard);
        } else {
            messageBoardMapper.update(new UpdateWrapper<MessageBoard>().eq("teacherId", loginTeacher.getId()).set("content", messageContent));
        }
    }

    @Override
    public List<MessageBoardContentMessageVo> getMessageBoard(HttpServletRequest request, String symbol) {
        // 登录鉴权
        Long teacherId = null;

        if (symbol.equals("1")) {
            Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);
            teacherId = loginTeacher.getId();
        } else {
            User user = checkUserLoginStatus(request, redisCache);
            UserTeam userTeam = userTeamMapper.selectOne(new QueryWrapper<UserTeam>().eq("userId", user.getId()).eq("isJoin", 1));
            if (userTeam == null) {
                throw new IllegalArgumentException("您还未入队，请加入队伍后使用此功能");
            }
            teacherId = userTeam.getTeacherId();
        }

        // 查看信息
        MessageBoard messageBoard = messageBoardMapper.selectOne(new QueryWrapper<MessageBoard>().eq("teacherId", teacherId));
        if (messageBoard == null) {
            return null;
        }
        List<MessageBoardContent> messageBoardContentMessages = JSON.parseArray(messageBoard.getContent(), MessageBoardContent.class);

        // 构建结果
        List<MessageBoardContentMessageVo> messageBoardContentMessageVos = new ArrayList<>();
        for (MessageBoardContent messageBoardContent : messageBoardContentMessages) {
            MessageBoardContentMessageVo messageBoardContentMessageVo = new MessageBoardContentMessageVo();
            if (messageBoardContent.getUserId() != null) {
                User sendUser = userMapper.selectById(messageBoardContent.getUserId());
                messageBoardContentMessageVo.setUser(sendUser);
            } else {
                Teacher sendTeacher = teacherMapper.selectById(messageBoardContent.getTeacherId());
                messageBoardContentMessageVo.setUser(sendTeacher);
            }
            messageBoardContentMessageVo.setContent(messageBoardContent.getContent());
            messageBoardContentMessageVo.setSendTime(messageBoardContent.getSendTime());
            messageBoardContentMessageVos.add(messageBoardContentMessageVo);
        }

        return messageBoardContentMessageVos;

    }

    /**
     * 获取原留言板消息内容，新增新的一条留言，返回 json 序列化后的结果
     * @param message 新的留言
     * @param type 学生为0，教师为1
     * @param userId 学生 id，如果是教师则为 null
     * @param teacherId 教师 id，必传
     * @return json 序列化后的结果
     */
    private String getMessageBoardContent(String message, int type, Long userId, Long teacherId, MessageBoard messageBoardBefore) {
        // 取出原来的消息内容
        List<MessageBoardContent> messageBoardContentMessages = null;
        if (messageBoardBefore == null) {
            messageBoardContentMessages = new ArrayList<>();
        } else {
            messageBoardContentMessages = JSON.parseArray(messageBoardBefore.getContent(), MessageBoardContent.class);
        }

        // 构建消息
        MessageBoardContent messageBoardContentMessage = new MessageBoardContent();
        messageBoardContentMessage.setId(UUID.randomUUID().toString());
        if (type == 1) {
            messageBoardContentMessage.setUserId(null);
            messageBoardContentMessage.setTeacherId(teacherId);
        } else {
            messageBoardContentMessage.setTeacherId(null);
            messageBoardContentMessage.setUserId(userId);
        }
        messageBoardContentMessage.setContent(message);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        messageBoardContentMessage.setSendTime(sdf.format(new Date()));
        messageBoardContentMessages.add(messageBoardContentMessage);
        return JSON.toJSONString(messageBoardContentMessages);
    }
}
