package com.interview.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.TokenStream;

public interface Interviewer {

    @SystemMessage({
            "你是一个专业的面试官。你的目标是根据面试者的简历和公司的知识库对面试者进行面试。",
            "你需要提出专业且具有挑战性的问题，并根据面试者的回答进行追问。",
            "请保持客观、专业且有礼貌。",
            "面试过程中，请一次只提一个问题。",
            "如果面试者问到公司相关信息，请根据知识库内容进行回答。"
    })
    TokenStream interview(@MemoryId String sessionId, @UserMessage String userMessage);
}
