package com.interview.ai.service.audio;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.FileInputStream;
import java.io.InputStream;


/**
 * 官网 https://alphacephei.com/cn/
 * 模型库 https://alphacephei.com/vosk/models
 *
 *
 */
@Slf4j
@Service
public class VoskASRService extends BaseAudioService{

    private final static String folder="vosk";
    private final static String dylib_name="libvosk.dylib";
    private final static String model_folder="model-small-cn-0.22";
    private final static String en_model_folder="model-small-cn-0.22";

    @PostConstruct
    public void init(){
        String dylib_path=speechPath+"/"+folder+"/"+dylib_name;
        log.info("pre load dylib:{} start ",dylib_path);
        System.load(dylib_path);
        log.info("load dylib success ");
    }

    protected String transcribe(String filePath) {
        try{
            Model model = new Model(speechPath+"/"+folder+"/"+model_folder);

            // 2. 读取音频文件
            InputStream ais = new FileInputStream(filePath);

            // 3. 初始化识别器 (设置采样率，需与音频文件一致)
            Recognizer recognizer = new Recognizer(model, 16000.0f);

            byte[] buffer = new byte[4096];
            int nbytes;
            while ((nbytes = ais.read(buffer)) >= 0) {
                // 4. 将音频数据送入识别器
                if (recognizer.acceptWaveForm(buffer, nbytes)) {
                    System.out.println(recognizer.getResult()); // 打印识别出的完整句子
                }else{
                    System.out.println(recognizer.getPartialResult());
                }
            }

            // 5. 获取最后剩余的结果
            System.out.println(recognizer.getFinalResult());
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return null;
    }

    protected InputStream transcribe(InputStream audioStream) {
        try{
            Model model = new Model(speechPath+"/"+folder+"/"+model_folder);
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            Recognizer recognizer = new Recognizer(model, 16000.0f);

            byte[] buffer = new byte[4096];
            while (true) {
                int nbytes = line.read(buffer, 0, buffer.length);
                if (recognizer.acceptWaveForm(buffer, nbytes)) {
                    System.out.println(recognizer.getResult()); // 实时输出识别结果
                } else {
                    // 如果想获取中间的部分识别结果，可以使用：
                     System.out.println(recognizer.getPartialResult());
                }
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return null;
    }
}
