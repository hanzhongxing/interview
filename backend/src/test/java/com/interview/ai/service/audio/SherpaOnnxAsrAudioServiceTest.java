package com.interview.ai.service.audio;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class SherpaOnnxAsrAudioServiceTest {

    private final static String test_wav="https://github.com/hanzhongxing/interview/blob/main/0.wav";


    @Resource
    SherpaOnnxAsrAudioService service;

    @Test
    public void doTest(){
        String txt=service.transcribe(test_wav);
        System.out.println(txt);
    }

}
