package com.interview.ai.service.rtc.component;

public interface CustomRTCService {


    boolean startRoom(String roomId);

    void stopRoom(String roomId);

    void shutDown();

    boolean isLocalRoom(String roomId);

}
