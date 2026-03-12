package com.interview.ai.service.rtc.component.tencent;

import com.interview.ai.service.rtc.component.CustomRTCService;
import com.tencentcloudapi.common.Credential;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Deprecated
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
//        TrtcClient client = new TrtcClient(credential, "ap-guangzhou");
//
//        var client = client.createClient({sdkAppId: 1400000000, userId: 'user_id'}); // 替换为你的SDKAppID和UserID
//        client.setListener({
//                onClientRoleChanged: function (msg) {
//            console.log('角色变更', msg);
//        },
//        onClientEvent: function (type, data, detail) {
//            if (type === 'client-publish-audio') { // 当有音频流发布时触发
//                console.log('有音频流发布', data, detail);
//            } else if (type === 'client-subscribe-audio') { // 当订阅音频流时触发
//                console.log('订阅音频流', data, detail);
//            }
//        }
//});
//        client.join({roomId: 12345});
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
