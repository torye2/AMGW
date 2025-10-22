package amgw.amgw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocumentsViewController {
    @GetMapping("/docs/list")
    public String list() { return "documents/docsList"; }

    @GetMapping("/docs/edit")
    public String edit() { return "documents/editDocs"; }
}

