package amgw.amgw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/approvals")
public class ApprovalPageController {

    @GetMapping
    public String list() { return "approvals"; }        // templates/approvals.html

    @GetMapping("/new")
    public String compose() { return "approvalsNew"; } // templates/approvals-new.html

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("docId", id);
        return "approvalDetail";
    }
}
