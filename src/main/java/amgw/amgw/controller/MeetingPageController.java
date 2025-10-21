package amgw.amgw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MeetingPageController {

    @GetMapping("/meetings")
    public String meetings() {
        return "meetings";
    }
}