package com.interview.ai.service.rtc.component.shengwang;

import com.interview.ai.service.rtc.component.CustomRTCService;
import com.interview.ai.service.rtc.component.shengwang.model.AudioFrameObserver;
import com.interview.ai.service.rtc.component.shengwang.model.LocalUserObserver;
import com.interview.ai.service.rtc.component.shengwang.model.RtcConnObserver;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.SDK;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class SRTCService implements CustomRTCService {
    private final static Logger logger= LoggerFactory.getLogger(SRTCService.class);

    private static ConcurrentHashMap<String, AgoraRtcConn> roomMap = new ConcurrentHashMap<>();
    public final static int default_sample_rate_hz=16000;
    public final static int default_channels=1;

    private final static String AGORA_APP_ID="AGORA_APP_ID";
    private final static String AGORA_APP_TOKEN="token";
    private final static String DEFAULT_USER_ID="101";
    private static AgoraService service;

    @PostConstruct
    public void init(){
        logger.info("init start");
        try {
            if(service!=null){
                logger.info("sdk has init");
                return;
            }
            SDK.load();
            service = new AgoraService();
            AgoraServiceConfig config = new AgoraServiceConfig();
            config.setAppId(AGORA_APP_ID);
            config.setEnableAudioProcessor(1);
            service.initialize(config);
            logger.info("init end success");
        }catch (Exception e) {
            logger.error(e.getMessage(),e);
            logger.info("init end error",e);
            throw new RuntimeException("声网服务初始化失败");
        }
    }

    @Override
    public boolean startRoom(String roomId) {
        logger.info("startAgoraSdkConn invoked for roomId=" + roomId);
        if (service == null) {
            logger.error("startAgoraSdkConn server init error");
            return false;
        }
        if (roomMap.containsKey(roomId)) {
            logger.info("startAgoraSdkConn has init roomId=" + roomId);
            return true;
        }
        String lock=roomId;
        synchronized (lock.intern()) {
            try {
                if (roomMap.containsKey(roomId)) {
                    logger.info("startAgoraSdkConn has init roomId=" + roomId);
                    return true;
                }
                AgoraRtcConn agoraRtcConn = service.agoraRtcConnCreate(null);
                RtcConnObserver connObserver = new RtcConnObserver();
                agoraRtcConn.registerObserver(connObserver);
                LocalUserObserver localUserObserver = new LocalUserObserver(roomId);
                agoraRtcConn.getLocalUser().registerObserver(localUserObserver);
                AudioFrameObserver pcmFrameObserver = new AudioFrameObserver(roomId);
                agoraRtcConn.connect(AGORA_APP_TOKEN,roomId,DEFAULT_USER_ID);
                Boolean flg=initAudioParams(roomId,agoraRtcConn);
                if(!flg){
                    logger.info("initAudioParams error roomId:{}",roomId);
                    return false;
                }
                int ret = agoraRtcConn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(default_channels, default_sample_rate_hz);
                logger.info("startAgoraSdkConn setPlaybackAudioFrameBeforeMixingParameters roomId:" + roomId + " return ret=" + ret);
                if (ret > 0) {
                    logger.info("startAgoraSdkConn setPlaybackAudioFrameBeforeMixingParameters fail ret=" + ret);
                    return false;
                }
                agoraRtcConn.getLocalUser().subscribeAllAudio();
                agoraRtcConn.getLocalUser().registerAudioFrameObserver(pcmFrameObserver);
                roomMap.put(roomId, agoraRtcConn);
                doActiveRoom(roomId);
                logger.info("startAgoraSdkConn success roomId:" + roomId + " return ret=" + ret );
                return true;
            } catch (Exception e) {
                logger.error("startAgoraSdkConn fail for roomId=" + roomId);
            }
            return false;
        }
    }

    @Override
    public void stopRoom(String roomId) {
        logger.info("removeAgoraRtcConn roomId:{}",roomId);
        if(!roomMap.containsKey(roomId)){
            logger.info("removeAgoraRtcConn room is not local roomId:{}",roomId);
            return;
        }
        String lock=roomId;
        synchronized (lock.intern()) {
            try {
                AgoraRtcConn agoraRtcConn = roomMap.get(roomId);
                roomMap.remove(roomId);
                if (agoraRtcConn == null) {
                    logger.info("removeAgoraRtcConn agoraRtcConn is null roomId:{}",roomId);
                    return;
                }
                agoraRtcConn.getLocalUser().unregisterAudioFrameObserver();
                agoraRtcConn.getLocalUser().unregisterObserver();
                agoraRtcConn.disconnect();
                agoraRtcConn.destroy();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logger.info("removeAgoraRtcConn error roomId:{} e:{}",roomId,e.getMessage());
            }
        }
    }

    @Override
    public void shutDown() {
        logger.info("AgoraSDKService shutdown start");
        for (Map.Entry<String, AgoraRtcConn> entry : roomMap.entrySet()) {
            stopRoom(entry.getKey());
        }
        service.destroy();
        logger.info("AgoraSDKService shutdown success");
    }

    @Override
    public boolean isLocalRoom(String roomId){
        return roomMap.containsKey(roomId);
    }

    public static Set<String> getLocalRoomIds(){
        return roomMap.keySet();
    }

    public static void doActiveRoom(String roomId){
//        String key= MSMeetingAudioTranslateUtil.buildKey(room_audio_translate_active,roomId);
//        RedisUtil.setex(key,System.currentTimeMillis()+"",expire_time);
    }

    public static boolean isRoomActive(String roomId){
//        String key=MSMeetingAudioTranslateUtil.buildKey(room_audio_translate_active,roomId);
//        String v=RedisUtil.getObject(key);
//        if(StringUtil.isNull(v)){
//            return false;
//        }
//        long time=Long.parseLong(v);
//        if((System.currentTimeMillis()-time)>roomExpireTime){
//            return false;
//        }
        return true;
    }

    private static boolean initAudioParams(String roomId,AgoraRtcConn agoraRtcConn){
        try{
            int rest = agoraRtcConn.getLocalUser().setPlaybackAudioFrameParameters(default_channels, default_sample_rate_hz,0,default_sample_rate_hz*default_channels/100);
            logger.info("initAudioParams setPlaybackAudioFrameParameters roomId:" + roomId + " return rest=" + rest);
            if(rest>0){
                logger.info("initAudioParams setPlaybackAudioFrameParameters fail rest=" + rest);
                return false;
            }
            return true;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return false;
    }
}
