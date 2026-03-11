package com.interview.ai.service.rtc;

import com.interview.ai.service.rtc.component.shengwang.SRTCService;
import com.interview.ai.service.rtc.component.tencent.TRTCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RtcService {
    private final static String rtc_service="agroa";

    @Autowired
    private TRTCService trtcService;

    @Autowired
    private SRTCService srtcService;


    public void startRoom(String roomId){

    }

    public void stopRoom(String roomId){

    }
}
