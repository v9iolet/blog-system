package com.blog.controller;

import com.blog.entity.Message;
import com.blog.mapper.MessageMapper;
import com.blog.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageMapper messageMapper;

    @PostMapping
    public Result<Void> leaveMessage(@RequestBody Message message) {
        message.setType(Message.TYPE_GUESTBOOK);
        message.setTitle("用户留言");
        message.setStatus(Message.STATUS_UNREAD);
        messageMapper.insert(message);
        return Result.success();
    }
}
