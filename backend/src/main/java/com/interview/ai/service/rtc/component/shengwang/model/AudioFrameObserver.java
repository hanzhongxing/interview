package com.interview.ai.service.rtc.component.shengwang.model;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.IAudioFrameObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioFrameObserver implements IAudioFrameObserver {
    private static final Logger logger = LoggerFactory.getLogger(AudioFrameObserver.class);

    String roomId;

    private AudioFrameObserver() {
        throw new RuntimeException("init error");
    }

    public AudioFrameObserver(String roomId) {
        this.roomId=roomId;
    }

    @Override
    public int onRecordAudioFrame(AgoraLocalUser agora_local_user, String channel_id, AudioFrame frame) {
        logger.debug("onRecordAudioFrame success roomId:{}",roomId);
        return 1;
    }

    @Override
    public int onPlaybackAudioFrame(AgoraLocalUser agora_local_user, String channel_id, AudioFrame audioFrame) {
        int writeBytes = audioFrame.getSamplesPerChannel() * audioFrame.getChannels() * audioFrame.getBytesPerSample();
        logger.debug("onPlaybackAudioFrame success roomId:{} writeBytes:{} getSamplesPerChannel:{} getChannels:{} getBytesPerSample:{}",roomId,writeBytes,audioFrame.getSamplesPerChannel(),audioFrame.getChannels(),audioFrame.getBytesPerSample());
        try {
//            if(LocalRoomUserCacheUtil.hasSummaryRoom(roomId)) {
//                byte[] audioByteData = new byte[writeBytes];
//                audioFrame.getBuffer().get(audioByteData);
//                AIAudioTranslateServer.receiveRoomSummaryAudioFile(roomId, audioByteData, System.currentTimeMillis());
//            }
        } catch (Exception e) {
            logger.error(roomId + " onPlaybackAudioFrameBeforeMixing exception", e);
        }
        return 1;
    }

    @Override
    public int onMixedAudioFrame(AgoraLocalUser agora_local_user, String channel_id, AudioFrame audioFrame) {
        int writeBytes = audioFrame.getSamplesPerChannel() * audioFrame.getChannels() * audioFrame.getBytesPerSample();
        logger.debug("onMixedAudioFrame success roomId:{} writeBytes:{} getSamplesPerChannel:{} getChannels:{} getBytesPerSample:{}",roomId,writeBytes,audioFrame.getSamplesPerChannel(),audioFrame.getChannels(),audioFrame.getBytesPerSample());
        return 1;
    }

    @Override
    public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agora_local_user, String channel_id, String uid, AudioFrame audioFrame) {
        int writeBytes = audioFrame.getSamplesPerChannel() * audioFrame.getChannels() * audioFrame.getBytesPerSample();
        logger.debug("onPlaybackAudioFrameBeforeMixing channel_id:{} uid:{} writeBytes:{} getSamplesPerChannel:{} getChannels:{} getBytesPerSample:{}",roomId,uid,writeBytes,audioFrame.getSamplesPerChannel(),audioFrame.getChannels(),audioFrame.getBytesPerSample());
        try {
            byte[] audioByteData=new byte[writeBytes];
            audioFrame.getBuffer().get(audioByteData);
//            AIAudioTranslateServer.submitTranslateTask(roomId,uid,audioByteData,System.currentTimeMillis());
        } catch (Exception e) {
            logger.error(roomId + " onPlaybackAudioFrameBeforeMixing exception", e);
        }
        return 1;
    }

    @Override
    public int onEarMonitoringAudioFrame(AgoraLocalUser agora_local_user, AudioFrame frame) {
        throw new UnsupportedOperationException(roomId + " Unimplemented method 'onEarMonitoringAudioFrame'");
    }

    @Override
    public int getObservedAudioFramePosition() {
        throw new UnsupportedOperationException(roomId + " Unimplemented method 'getObservedAudioFramePosition'");
    }
}
