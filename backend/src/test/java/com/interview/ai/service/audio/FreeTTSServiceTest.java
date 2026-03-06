package com.interview.ai.service.audio;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class FreeTTSServiceTest {

    @Resource
    FreeTTSService freeTTSService;

    @Test
    public void doTest(){
        freeTTSService.generate("Hello, how's the weather today? Is there anything I can help you with?");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("asd");
    }
}
