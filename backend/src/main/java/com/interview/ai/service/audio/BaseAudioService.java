package com.interview.ai.service.audio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public abstract class BaseAudioService extends AbstractAudioService{

    @Value("${interview.speech-path:/opt/web/interview/data/speech}")
    protected String speechPath;

    protected InputStream generate(String text) {
        return null;
    }
    protected InputStream transcribe(InputStream audioStream) {
        return null;
    }
}
