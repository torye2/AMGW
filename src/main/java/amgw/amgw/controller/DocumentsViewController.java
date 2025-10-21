package amgw.amgw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocumentsViewController {
    @GetMapping("/docs/list")
    public String list() { return "documents/docsList"; }

    // edit.html은 이미 templates/documents/edit.html로 직접 접근 가능하면 생략
    @GetMapping("/docs/edit")
    public String edit() { return "documents/editDocs"; }
}

