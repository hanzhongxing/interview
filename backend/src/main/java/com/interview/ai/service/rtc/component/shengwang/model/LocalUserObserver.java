package com.interview.ai.service.rtc.component.shengwang.model;

import io.agora.rtc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalUserObserver implements ILocalUserObserver {

    private static final Logger logger = LoggerFactory.getLogger(LocalUserObserver.class);

    private String roomId;

    public LocalUserObserver(String roomId) {
        super();
        this.roomId = roomId;
    }

    private LocalUserObserver() {
        throw new RuntimeException("init error");
    }

    @Override
    public void onAudioTrackPublishSuccess(AgoraLocalUser agora_local_user,
                                           AgoraLocalAudioTrack agora_local_audio_track) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onAudioTrackPublishSuccess");
    }

    @Override
    public void onAudioSubscribeStateChanged(AgoraLocalUser agora_local_user, String channel, String user_id,
                                             int old_state, int new_state, int elapse_since_last_state) {
        logger.info(roomId + "onAudioSubscribeStateChanged"+",user_id:"+user_id+",new_state:"+new_state );
        if(new_state==3){
            // 开启订阅
//            AIAudioTranscriptionServer.prepareSendClient(roomId,user_id);
        }else if (new_state ==1){
            // 停止订阅
//            AIAudioTranscriptionServer.closeSendClient(roomId,user_id,"声网客户端退出订阅");
        }
    }

    @Override
    public void onAudioTrackPublicationFailure(AgoraLocalUser agora_local_user,
                                               AgoraLocalAudioTrack agora_local_audio_track, int error) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onAudioTrackPublicationFailure");
    }

    @Override
    public void onLocalAudioTrackStateChanged(AgoraLocalUser agora_local_user,
                                              AgoraLocalAudioTrack agora_local_audio_track, int state, int error) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onLocalAudioTrackStateChanged");
    }

    @Override
    public void onLocalAudioTrackStatistics(AgoraLocalUser agora_local_user, LocalAudioStats stats) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onLocalAudioTrackStatistics");
    }

    @Override
    public void onRemoteAudioTrackStatistics(AgoraLocalUser agora_local_user,
                                             AgoraRemoteAudioTrack agora_remote_audio_track, RemoteAudioTrackStats stats) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onRemoteAudioTrackStatistics");
    }

    @Override
    public void onUserAudioTrackSubscribed(AgoraLocalUser agora_local_user, String user_id,
                                           AgoraRemoteAudioTrack agora_remote_audio_track) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onUserAudioTrackSubscribed");
    }

    @Override
    public void onUserAudioTrackStateChanged(AgoraLocalUser agora_local_user, String user_id,
                                             AgoraRemoteAudioTrack agora_remote_audio_track, int state, int reason, int elapsed) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onUserAudioTrackStateChanged");
    }



    @Override
    public void onAudioPublishStateChanged(AgoraLocalUser agora_local_user, String channel, int old_state,
                                           int new_state, int elapse_since_last_state) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onAudioPublishStateChanged");
    }

    @Override
    public void onFirstRemoteAudioFrame(AgoraLocalUser agora_local_user, String user_id, int elapsed) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onFirstRemoteAudioFrame");
    }

    @Override
    public void onFirstRemoteAudioDecoded(AgoraLocalUser agora_local_user, String user_id, int elapsed) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onFirstRemoteAudioDecoded");
    }

    @Override
    public void onVideoTrackPublishSuccess(AgoraLocalUser agora_local_user,
                                           AgoraLocalVideoTrack agora_local_video_track) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onVideoTrackPublishSuccess");
    }

    @Override
    public void onVideoTrackPublicationFailure(AgoraLocalUser agora_local_user,
                                               AgoraLocalVideoTrack agora_local_video_track, int error) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onVideoTrackPublicationFailure");
    }

    @Override
    public void onLocalVideoTrackStateChanged(AgoraLocalUser agora_local_user,
                                              AgoraLocalVideoTrack agora_local_video_track, int state, int error) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onLocalVideoTrackStateChanged");
    }

    @Override
    public void onLocalVideoTrackStatistics(AgoraLocalUser agora_local_user,
                                            AgoraLocalVideoTrack agora_local_video_track, LocalVideoTrackStats stats) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onLocalVideoTrackStatistics");
    }

    @Override
    public void onUserVideoTrackSubscribed(AgoraLocalUser agora_local_user, String user_id, VideoTrackInfo info,
                                           AgoraRemoteVideoTrack agora_remote_video_track) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onUserVideoTrackSubscribed");
    }

    @Override
    public void onUserVideoTrackStateChanged(AgoraLocalUser agora_local_user, String user_id,
                                             AgoraRemoteVideoTrack agora_remote_video_track, int state, int reason, int elapsed) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onUserVideoTrackStateChanged");
    }

    @Override
    public void onRemoteVideoTrackStatistics(AgoraLocalUser agora_local_user,
                                             AgoraRemoteVideoTrack agora_remote_video_track, RemoteVideoTrackStats stats) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onRemoteVideoTrackStatistics");
    }

    @Override
    public void onAudioVolumeIndication(AgoraLocalUser agora_local_user, AudioVolumeInfo speakers, int speaker_number,
                                        int total_volume) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onAudioVolumeIndication");
    }

    @Override
    public void onActiveSpeaker(AgoraLocalUser agora_local_user, String userId) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onActiveSpeaker");
    }

    @Override
    public void onRemoteVideoStreamInfoUpdated(AgoraLocalUser agora_local_user, RemoteVideoStreamInfo info) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onRemoteVideoStreamInfoUpdated");
    }

    @Override
    public void onVideoSubscribeStateChanged(AgoraLocalUser agora_local_user, String channel, String user_id,
                                             int old_state, int new_state, int elapse_since_last_state) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onVideoSubscribeStateChanged");
    }

    @Override
    public void onVideoPublishStateChanged(AgoraLocalUser agora_local_user, String channel, int old_state,
                                           int new_state, int elapse_since_last_state) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onVideoPublishStateChanged");
    }

    @Override
    public void onFirstRemoteVideoFrame(AgoraLocalUser agora_local_user, String user_id, int width, int height,
                                        int elapsed) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onFirstRemoteVideoFrame");
    }

    @Override
    public void onFirstRemoteVideoDecoded(AgoraLocalUser agora_local_user, String user_id, int width, int height,
                                          int elapsed) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onFirstRemoteVideoDecoded");
    }

    @Override
    public void onFirstRemoteVideoFrameRendered(AgoraLocalUser agora_local_user, String user_id, int width, int height,
                                                int elapsed) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onFirstRemoteVideoFrameRendered");
    }

    @Override
    public void onVideoSizeChanged(AgoraLocalUser agora_local_user, String user_id, int width, int height,
                                   int rotation) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onVideoSizeChanged");
    }

    @Override
    public void onUserInfoUpdated(AgoraLocalUser agora_local_user, String user_id, int msg, int val) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onUserInfoUpdated");
    }

    @Override
    public void onIntraRequestReceived(AgoraLocalUser agora_local_user) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onIntraRequestReceived");
    }

    @Override
    public void onRemoteSubscribeFallbackToAudioOnly(AgoraLocalUser agora_local_user, String user_id,
                                                     int is_fallback_or_recover) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onRemoteSubscribeFallbackToAudioOnly");
    }

    @Override
    public void onStreamMessage(AgoraLocalUser agora_local_user, String user_id, int stream_id, String data,
                                long length) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onStreamMessage");
    }

    @Override
    public void onUserStateChanged(AgoraLocalUser agora_local_user, String user_id, int state) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onUserStateChanged");
    }

    @Override
    public void onAudioTrackPublishStart(AgoraLocalUser agora_local_user,
                                         AgoraLocalAudioTrack agora_local_audio_track) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onAudioTrackPublishStart");
    }

    @Override
    public void onAudioTrackUnpublished(AgoraLocalUser agora_local_user, AgoraLocalAudioTrack agora_local_audio_track) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onAudioTrackUnpublished");
    }

    @Override
    public void onVideoTrackPublishStart(AgoraLocalUser agora_local_user,
                                         AgoraLocalVideoTrack agora_local_video_track) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onVideoTrackPublishStart");
    }

    @Override
    public void onVideoTrackUnpublished(AgoraLocalUser agora_local_user, AgoraLocalVideoTrack agora_local_video_track) {
        // TODO Auto-generated method stub
        logger.info(roomId + "onVideoTrackUnpublished");
    }
}
