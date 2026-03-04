package com.interview.ai.model.lazy;

import com.interview.ai.exception.ConfigurationMissingException;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class LazyStreamingChatLanguageModel implements StreamingChatLanguageModel {

    private final Supplier<StreamingChatLanguageModel> modelSupplier;
    private StreamingChatLanguageModel delegate;

    private synchronized StreamingChatLanguageModel getDelegate() {
        if (delegate == null) {
            delegate = modelSupplier.get();
            if (delegate == null) {
                throw new ConfigurationMissingException(
                        "Streaming LLM configuration is missing or inactive. Please configure it in the settings.");
            }
        }
        return delegate;
    }

    public synchronized void reset() {
        this.delegate = null;
    }

    @Override
    public void chat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
        getDelegate().chat(chatRequest, handler);
    }

    @Override
    public void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler) {
        getDelegate().generate(messages, handler);
    }
}
