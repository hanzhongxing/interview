package com.interview.ai.service.audio;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class VoskASRServiceTest {
    private final static String test_wav="https://github.com/hanzhongxing/interview/blob/main/0.wav";

    @Resource
    VoskASRService voskASRService;

    @Test
    public void doTest(){
        String txt=voskASRService.transcribe(test_wav);
        System.out.println(txt);
    }

}
