package com.interview.ai.service.audio;

import java.io.InputStream;

public abstract class AbstractAudioService {
    abstract InputStream generate(String text);
    abstract InputStream transcribe(InputStream audioStream);
}
