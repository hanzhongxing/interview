package com.interview.ai.service.audio.base;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public abstract class BaseAudioService extends AbstractAudioService{

    @Value("${interview.speech-path:/opt/web/interview/data/speech}")
    protected String speechPath;

    public InputStream generate(String text) {
        return null;
    }
    public InputStream transcribe(InputStream audioStream) {
        return null;
    }
    public String transcribe(String filePath) {
        return null;
    }
}
