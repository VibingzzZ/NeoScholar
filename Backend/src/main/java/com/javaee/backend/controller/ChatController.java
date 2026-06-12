package com.javaee.backend.controller;

import com.javaee.backend.AIService.ConsultantService;
import com.javaee.backend.entity.ChatMessage;
import com.javaee.backend.po.dto.ChatRequest;
import com.javaee.backend.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("api/chat")
public class ChatController {

    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest chatRequest) {
        String chatId = chatRequest.getChatId();

        chatMessageService.save(chatRequest.getQuestion(), chatId, chatRequest.getUserId(), "user");

        StringBuilder fullReply = new StringBuilder();
        return consultantService.chatWithStream(
                        chatRequest.getQuestion(),
                        chatId
                )
                .doOnNext(fullReply::append)
                .doOnComplete(() -> {
                    String assistantReply = fullReply.toString();
                    chatMessageService.save(assistantReply, chatId, chatRequest.getUserId(), "assistant");
                });
    }
}