package com.interview.ai.controller;

import com.interview.ai.service.Interviewer;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InterviewChatController {

    private final Interviewer interviewer;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void chat(Map<String, String> payload) {
        String sessionId = payload.get("sessionId");
        String message = payload.get("message");

        log.info("Received message from session {}: {}", sessionId, message);

        TokenStream tokenStream = interviewer.interview(sessionId, message);

        tokenStream
                .onNext(token -> {
                    messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
                            Map.of("type", "delta", "content", token));
                })
                .onComplete(response -> {
                    messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
                            Map.of("type", "complete", "content", ""));
                })
                .onError(error -> {
                    log.error("Error in AI stream", error);
                    messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
                            Map.of("type", "error", "content", error.getMessage()));
                })
                .start();
    }
}
