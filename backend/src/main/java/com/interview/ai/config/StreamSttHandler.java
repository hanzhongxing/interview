package com.interview.ai.config;

import com.interview.ai.service.audio.SpeechService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamSttHandler extends BinaryWebSocketHandler {

    private final SpeechService speechService;
    private final Map<String, FileInfo> sessionFiles = new ConcurrentHashMap<>();

    private static class FileInfo {
        File file;
        BufferedOutputStream outputStream;
        long lastProcessed = System.currentTimeMillis();
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        log.info("STT WebSocket connection established: {}", session.getId());
        File tempFile = File.createTempFile("streaming_v_", ".webm");
        sessionFiles.put(session.getId(), new FileInfo());
        sessionFiles.get(session.getId()).file = tempFile;
        sessionFiles.get(session.getId()).outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
    }

    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message)
            throws Exception {
        FileInfo info = sessionFiles.get(session.getId());
        if (info != null && info.outputStream != null) {
            info.outputStream.write(message.getPayload().array());

            // To provide a "real-time" feel with Whisper, we can transcribe periodically or
            // upon specific triggers
            // For simplicity and correctness, we'll transcribe when a certain amount of
            // data is received or session closes.
            // But if the user wants real-time, we should transcribe every N seconds.
            long now = System.currentTimeMillis();
            if (now - info.lastProcessed > 3000) { // Every 3 seconds
                info.outputStream.flush();
                // Whisper needs a complete header. WebM chunks might not be individually
                // playable.
                // This is a trade-off. True real-time STT requires a dedicated streaming
                // engine.
                // We'll perform a partial transcription if possible, or wait till the end.
                // Given Whisper's nature, we'll wait for the session end for accuracy,
                // OR send the current whole file so far.
                transcribeCurrentContent(session, info);
                info.lastProcessed = now;
            }
        }
    }

    private void transcribeCurrentContent(WebSocketSession session, FileInfo info) {
        try {
            // Flush to ensure file has all data
            info.outputStream.flush();
            String text = speechService.transcribe(info.file.toPath().toString());
            if (text != null && !text.isEmpty()) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (Exception e) {
            log.warn("STT real-time transcription failed: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        FileInfo info = sessionFiles.remove(session.getId());
        if (info != null) {
            if (info.outputStream != null) {
                info.outputStream.close();
            }
            if (info.file != null && info.file.exists()) {
                info.file.delete();
            }
        }
        log.info("STT WebSocket connection closed: {}", session.getId());
    }
}
