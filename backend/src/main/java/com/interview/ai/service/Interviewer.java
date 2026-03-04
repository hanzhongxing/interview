package com.interview.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.TokenStream;

public interface Interviewer {

    @SystemMessage({
            "你是一个专业的AI面试官。你将根据应聘者的简历分析结果、岗位需求（JD）以及公司文化知识库来进行面试。",
            "你的目标是通过专业且具有挑战性的问题，全面评估面试者的技能和文化匹配度。",
            "面试规则：",
            "1. 保持客观、专业且礼貌。",
            "2. 每次只提一个问题。",
            "3. 如果面试者回答模糊，请进行针对性的追问。",
            "4. 结合Retrieved Context（知识库内容）来回答关于公司或岗位的具体问题。",
            "5. 面试开始时，请先基于简历分析结果做一个简短的开场白。"
    })
    TokenStream interview(@MemoryId String sessionId, @UserMessage String userMessage);
}
