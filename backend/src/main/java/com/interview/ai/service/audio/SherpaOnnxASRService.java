package com.interview.ai.service.audio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.k2fsa.sherpa.onnx.*;
import javax.sound.sampled.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.InputStream;

/*
* macOS 13+
* sherpa-onnx https://github.com/k2-fsa/sherpa-onnx/releases
* onnx resource  https://huggingface.co/csukuangfj/models
* zipformer-bilingual https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-streaming-zipformer-bilingual-zh-en-2023-02-20.tar.bz2
*
* */

@Slf4j
@Service
public class SherpaOnnxASRService extends BaseAudioService{

    private final static String resource_folder="sherpa";
    private final static String sense_voice_folder="sense_voice";
    private final static String model_name="model.int8.onnx";
    private final static String tokens_name="tokens.txt";
    private final static String zip_former_folder="zipformer";
    private final static String encoder="encoder-epoch-99-avg-1.int8.onnx";
    private final static String decoder="decoder-epoch-99-avg-1.int8.onnx";
    private final static String joiner="joiner-epoch-99-avg-1.int8.onnx";

    protected InputStream transcribe(InputStream audioStream) {
        try {
            System.out.println("正在加载流式模型...");
            // 2. 配置流式 Transducer 模型
            OnlineTransducerModelConfig transducerConfig = OnlineTransducerModelConfig.builder()
                    .setEncoder(speechPath+"/"+resource_folder+"/"+zip_former_folder+"/"+encoder)
                    .setDecoder(speechPath+"/"+resource_folder+"/"+zip_former_folder+"/"+decoder)
                    .setJoiner(speechPath+"/"+resource_folder+"/"+zip_former_folder+"/"+joiner)
                    .build();

            OnlineModelConfig modelConfig = OnlineModelConfig.builder()
                    .setTransducer(transducerConfig)
                    .setTokens(speechPath+"/"+resource_folder+"/"+zip_former_folder+"/"+tokens_name)
                    .setNumThreads(4)
                    .build();

            // 配置端点检测 (VAD)：如果停顿时间超过阈值，就自动断句
            EndpointRule rule1 = EndpointRule.builder().setMustContainNonSilence(false).setMinTrailingSilence(2.4f).build();
            EndpointRule rule2 = EndpointRule.builder().setMustContainNonSilence(true).setMinTrailingSilence(1.2f).build();
            EndpointRule rule3 = EndpointRule.builder().setMustContainNonSilence(true).setMinUtteranceLength(20.0f).build();
            EndpointConfig endpointConfig = EndpointConfig.builder().setRule1(rule1).setRule2(rule2).setRule3(rule3).build();

            OnlineRecognizerConfig config = OnlineRecognizerConfig.builder()
                    .setOnlineModelConfig(modelConfig)
                    .setEndpointConfig(endpointConfig)
                    .setEnableEndpoint(true) // 开启自动断句
                    .build();

            // 3. 初始化流式识别器和音频流
            OnlineRecognizer recognizer = new OnlineRecognizer(config);
            OnlineStream stream = recognizer.createStream();

            // 4. 配置 Java 原生麦克风采集 (16kHz, 16-bit, 单声道)
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("麦克风不支持 16000Hz 16-bit 单声道录音！");
                return null;
            }

            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            System.out.println("====== 模型加载完毕，请对着麦克风说话 ======");

            // 每次读取 0.1 秒的音频数据 (16000采样率 * 2字节 * 0.1秒 = 3200字节)
            byte[] audioBuffer = new byte[3200];
            int segmentIndex = 1;

            // 5. 核心：死循环读取麦克风 -> 喂给模型 -> 获取临时识别结果
            while (true) {
                int bytesRead = microphone.read(audioBuffer, 0, audioBuffer.length);
                if (bytesRead > 0) {
                    // 将 byte[] 转为 Sherpa-onnx 需要的 float[] (-1.0 到 1.0)
                    float[] samples = convertBytesToFloat(audioBuffer, bytesRead);

                    // 喂入音频流
                    stream.acceptWaveform(samples, 16000);

                    // 如果模型准备好了解码，就进行解码
                    while (recognizer.isReady(stream)) {
                        recognizer.decode(stream);
                    }

                    // 获取当前的实时识别结果 (边说边出的字)
                    String partialResult = recognizer.getResult(stream).getText();

                    // \r 表示回到行首覆盖打印，实现“字越来越长”的视觉效果
                    System.out.print("\r正在说: " + partialResult);

                    // 6. 判断是否检测到静音断句 (Endpoint)
                    if (recognizer.isEndpoint(stream)) {
                        String finalResult = recognizer.getResult(stream).getText();
                        if (!finalResult.trim().isEmpty()) {
                            System.out.println("\n[第 " + segmentIndex + " 句 最终结果]: " + finalResult);
                            segmentIndex++;
                        }
                        // 断句后重置 Stream，开始听下一句话
                        recognizer.reset(stream);
                    }
                }
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return null;
    }

    protected String transcribe(String filePath) {
        try {
            OfflineModelConfig modelConfig = OfflineModelConfig.builder()
                    .setSenseVoice(
                            OfflineSenseVoiceModelConfig.builder()
                                    .setModel(speechPath+"/"+resource_folder+"/"+sense_voice_folder+"/"+model_name)
                                    .build()
                    )
                    .setTokens(speechPath+"/"+resource_folder+"/"+sense_voice_folder+"/"+tokens_name)
                    .setNumThreads(4)      // 设置推理使用的 CPU 线程数
                    .setDebug(false)       // 是否打印底层 C++ 调试信息
                    .build();

            // 3. 构建离线识别器配置
            OfflineRecognizerConfig config = OfflineRecognizerConfig.builder()
                    .setOfflineModelConfig(modelConfig)
                    .build();

            System.out.println("初始化识别器实例...");
            // 4. 初始化识别器实例
            OfflineRecognizer recognizer = new OfflineRecognizer(config);

            System.out.println("模型加载完成，开始读取音频并识别...");

            // 5. 创建数据流并喂入音频数据
            OfflineStream stream = recognizer.createStream();
            float[] audioSamples =readWavFromFile(filePath);

            // 传入音频采样率 (16000) 和 float 数组
            stream.acceptWaveform(audioSamples, 16000);

            // 6. 执行解码 (识别)
            recognizer.decode(stream);

            // 7. 获取并打印识别结果
            OfflineRecognizerResult result = recognizer.getResult(stream);
            System.out.println("=====================================");
            System.out.println("识别结果: " + result.getText());
            System.out.println("=====================================");

            // 8. 释放资源
            stream.release();
            recognizer.release();
            return result.getText();
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 工具方法：读取 16kHz, 16-bit, Mono 的 WAV 文件，并将其转换为 float 数组 (-1.0 到 1.0)
     */
    private static float[] readWavFromFile(String filePath) throws Exception {
        File wavFile = new File(filePath);
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile)) {
            // 检查音频格式
            if (audioStream.getFormat().getSampleRate() != 16000.0f) {
                System.err.println("警告：音频采样率不是 16000Hz！可能会导致识别乱码。");
            }

            byte[] bytes = audioStream.readAllBytes();
            float[] samples = new float[bytes.length / 2];

            // 16-bit PCM 转 float
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(new short[samples.length]);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < samples.length; i++) {
                short sample = byteBuffer.getShort();
                samples[i] = sample / 32768.0f; // 归一化到 [-1.0, 1.0]
            }
            return samples;
        }
    }

    /**
     * 工具方法：将 16-bit PCM 小端序的 byte 数组转换为归一化的 float 数组
     */
    private static float[] convertBytesToFloat(byte[] audioBuffer, int bytesRead) {
        int sampleCount = bytesRead / 2;
        float[] samples = new float[sampleCount];
        ByteBuffer byteBuffer = ByteBuffer.wrap(audioBuffer, 0, bytesRead).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < sampleCount; i++) {
            short sample = byteBuffer.getShort();
            samples[i] = sample / 32768.0f;
        }
        return samples;
    }
}
