package com.interview.ai.service.rtc.component.shengwang.model;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.RtcConnInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtcConnObserver extends DefaultRtcConnObserver {

    private static final Logger logger = LoggerFactory.getLogger(RtcConnObserver.class);

    @Override
    public void onConnected(AgoraRtcConn conn, RtcConnInfo rtcConnInfo, int reason) {
        logger.info("connect success" + rtcConnInfo.getChannelId() + "-" + rtcConnInfo.getId());
    }

    @Override
    public void onUserJoined(AgoraRtcConn agora_rtc_conn, String user_id) {
        logger.info("join success" + user_id);
    }

    @Override
    public void onUserLeft(AgoraRtcConn agora_rtc_conn, String user_id, int reason) {
        logger.info("left success" + user_id + ":" + reason);
    }
}
