// src/main/java/amgw/amgw/controller/ChatingController.java
package amgw.amgw.controller;

import amgw.amgw.service.ChatService;
import amgw.amgw.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserService current;

    @GetMapping("/chat")
    public String chat(Model model) {
        // 로그인 사용자 id (gw.users.id)
        Long meId = current.currentUserId();
        model.addAttribute("meId", meId);

        // 내 방 목록을 SSR로 먼저 렌더링
        model.addAttribute("rooms", chatService.myRooms());

        // templates/chat.html 렌더 (템플릿명이 chating.html이면 "chating"으로 변경)
        return "chat";
    }
}
