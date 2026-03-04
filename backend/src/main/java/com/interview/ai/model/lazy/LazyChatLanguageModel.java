package com.interview.ai.model.lazy;

import com.interview.ai.exception.ConfigurationMissingException;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class LazyChatLanguageModel implements ChatLanguageModel {

    private final Supplier<ChatLanguageModel> modelSupplier;
    private ChatLanguageModel delegate;

    private synchronized ChatLanguageModel getDelegate() {
        if (delegate == null) {
            delegate = modelSupplier.get();
            if (delegate == null) {
                throw new ConfigurationMissingException(
                        "LLM configuration is missing or inactive. Please configure it in the settings.");
            }
        }
        return delegate;
    }

    public synchronized void reset() {
        this.delegate = null;
    }

    @Override
    public String generate(String userMessage) {
        return getDelegate().generate(userMessage);
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        return getDelegate().generate(messages);
    }
}
