package amgw.amgw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebRtcSignalController {
    private final SimpMessagingTemplate messaging;

    // 클라이언트 -> /app/webrtc/{room} 로 보내면
    // 서버 -> /topic/webrtc/{room} 으로 중계
    @MessageMapping("/webrtc/{room}")
    public void relay(@DestinationVariable String room, @Payload String message) {
        // 필요하면 senderId 검증/필터링/권한 체크 추가
        messaging.convertAndSend("/topic/webrtc/" + room, message);
    }
}