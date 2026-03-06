package com.interview.ai.service.audio.components;

import com.interview.ai.service.audio.base.BaseAudioService;
import com.interview.ai.utils.StringUtils;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.sound.sampled.AudioFileFormat;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FreeTTSService extends BaseAudioService {

    private final static String voiceName="kevin16";

    private List<String> voiceClass= Arrays.asList(
//            "de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory",
            "com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory",
            "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory"
    );

    @PostConstruct
    public void init(){
        System.setProperty("freetts.voices", StringUtils.parseArrayString(voiceClass));
    }

    public InputStream generate(String text) {
        VoiceManager voiceManager=VoiceManager.getInstance();
        Voice[] voices=voiceManager.getVoices();
        if(voices==null||voices.length==0){
            log.error("Voice not found");
            return null;
        }
        String audioPath=speechPath+"/temp_audio/free_tts_"+System.currentTimeMillis();
        try {
            Files.createFile(Path.of(audioPath));
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return null;
        }
        SingleFileAudioPlayer audioPlayer =new SingleFileAudioPlayer(audioPath, AudioFileFormat.Type.WAVE);

        Voice voice = voiceManager.getVoice(voiceName);
        if(voice==null){
            log.error("Voice:{} not found",voiceName);
            return null;
        }
        try {
            voice.setAudioPlayer(audioPlayer);
            voice.allocate();
            voice.speak(text);
            return new ByteArrayInputStream(new FileInputStream(audioPath).readAllBytes());
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }finally {
            voice.deallocate();
            audioPlayer.close();
        }
        return null;
    }
}
