package com.interview.ai.service.audio;

import lombok.extern.slf4j.Slf4j;
import org.pitest.voices.Chorus;
import org.pitest.voices.ChorusConfig;
import org.pitest.voices.alba.Alba;
import org.pitest.voices.audio.Audio;
import org.pitest.voices.g2p.core.dictionary.Dictionaries;
import org.pitest.voices.openvoice.OpenVoiceSupplier;
import org.pitest.voices.uk.EnUkDictionary;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class VoicesTTSService extends BaseAudioService{

    public InputStream generate(String text) {
        //EnUkDictionary.en_uk()
        ChorusConfig config = ChorusConfig.chorusConfig(Dictionaries.empty()).withModel(new OpenVoiceSupplier());
        // 2. 创建 Chorus 实例（建议复用）
        try (Chorus chorus = new Chorus(config)) {
            // 3. 获取声音模型
            var voice = chorus.voice(Alba.albaMedium());

            // 4. 合成文本
            Audio audio = voice.say("This is the Voices library speaking. It sounds much more natural.");

            // 5. 保存为音频文件
            Path outputPath = Paths.get("voices_output.wav");
            audio.save(outputPath);
            System.out.println("Audio saved to: " + outputPath.toAbsolutePath());
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return null;
    }
}
