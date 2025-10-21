package amgw.amgw.controller;

import amgw.amgw.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AttendancePageController {
    private final CurrentUserService current;

    @GetMapping("/attendance")
    public String page(Model model){
        model.addAttribute("meId", current.currentUserId());
        return "attendance"; // templates/attendance.html
    }
}