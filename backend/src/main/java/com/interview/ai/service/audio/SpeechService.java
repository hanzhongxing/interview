package com.interview.ai.service.audio;

import com.interview.ai.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.io.InputStream;

@Service
public class SpeechService {
    public final static String freetts="freetts";
    public final static String ai="ai";

    @Resource
    private FreeTTSService freeTTSService;

    @Resource
    private VoicesService voicesService;

    @Resource
    private OpenAiAudioService openAiAudioService;


    public InputStream generate(String text,String serviceName) {
        BaseAudioService audioService=getAudioService(serviceName);
        return audioService.generate(text);
    }

    public InputStream transcribe(InputStream audioStream,String serviceName) {
        BaseAudioService audioService=getAudioService(serviceName);
        return audioService.transcribe(audioStream);
    }

    public String transcribe(String filePath) {
        BaseAudioService audioService=getAudioService(ai);
        return audioService.transcribe(filePath);
    }

    private BaseAudioService getAudioService(String serviceName){
        if(StringUtils.isEmpty(serviceName)||freetts.equals(serviceName)){
            return freeTTSService;
        }else if(ai.equals(serviceName)){
            return openAiAudioService;
        }
        return voicesService;
    }
}
