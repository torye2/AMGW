package amgw.amgw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchPageController {
    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false, defaultValue = "") String q,
                             Model model) {
        model.addAttribute("q", q);
        return "search"; // templates/search.html
    }
}