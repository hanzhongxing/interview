package com.interview.ai.service.rtc.component.tencent;

import com.interview.ai.service.rtc.component.CustomRTCService;
import com.tencentcloudapi.common.Credential;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TRTCService implements CustomRTCService {
    private final static String SECRET_ID="SECRET_ID";
    private final static String SECRET_KEY="SECRET_KEY";
    private static Credential credential =null;
    @PostConstruct
    public void init(){
        try {
            credential = new Credential(SECRET_ID, SECRET_KEY);
            log.info("init tencent rtc success");
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new RuntimeException("腾讯rtc初始化失败");
        }
    }


    @Override
    public boolean startRoom(String roomId) {
        return true;
    }

    @Override
    public void stopRoom(String roomId) {

    }

    @Override
    public void shutDown() {

    }

    @Override
    public boolean isLocalRoom(String roomId) {
        return false;
    }
}
