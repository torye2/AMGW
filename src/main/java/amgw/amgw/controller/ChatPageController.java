package amgw.amgw.controller;

import amgw.amgw.service.ChatService;
import amgw.amgw.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ChatPageController {

    private final ChatService chatService;
    private final CurrentUserService current;

    @GetMapping("/chat")
    public String chat(Model model) {
        model.addAttribute("meId", current.currentUserId());
        // 내 방 목록을 SSR로 먼저 렌더링
        model.addAttribute("rooms", chatService.myRooms());
        return "chat";
    }
}