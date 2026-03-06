package com.interview.ai.service.audio;

import com.interview.ai.service.audio.base.BaseAudioService;
import com.interview.ai.service.audio.components.*;
import com.interview.ai.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.InputStream;

@Service
public class SpeechService {
    public final static String ai="ai";
    public final static String vosk="vosk";
    public final static String sherpa="sherpa";
    public final static String freetts="freetts";

    @Autowired
    private FreeTTSService freeTTSService;

    @Autowired
    private VoicesTTSService voicesTTSService;

    @Autowired
    private VoskASRService voskASRService;

    @Autowired
    private OpenAiAudioService openAiAudioService;

    @Autowired
    private SherpaOnnxASRService sherpaOnnxASRService;


    public InputStream generate(String text,String serviceName) {
        BaseAudioService audioService=getAudioService(true,serviceName);
        return audioService.generate(text);
    }

    public InputStream transcribe(InputStream audioStream,String serviceName) {
        BaseAudioService audioService=getAudioService(false,serviceName);
        return audioService.transcribe(audioStream);
    }

    public String transcribe(String filePath) {
        BaseAudioService audioService=getAudioService(false,ai);
        return audioService.transcribe(filePath);
    }

    private BaseAudioService getAudioService(boolean isTTS,String serviceName){
        if(isTTS) {
            if (StringUtils.isEmpty(serviceName) || freetts.equals(serviceName)) {
                return freeTTSService;
            } else if (ai.equals(serviceName)) {
                return openAiAudioService;
            }
            return voicesTTSService;
        }
        if (StringUtils.isEmpty(serviceName) || vosk.equals(serviceName)) {
            return voskASRService;
        } else if (ai.equals(serviceName)) {
            return openAiAudioService;
        }else if(sherpa.equals(serviceName)){
            return sherpaOnnxASRService;
        }
        return voskASRService;
    }
}
