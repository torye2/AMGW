package amgw.amgw.controller;

import amgw.amgw.todo.model.Todo;
import amgw.amgw.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TodoPageController {

    @GetMapping("/todos/new")
    public String newPage(){ return "todos_new"; }

    // 폼 제출을 간단히 처리해서 대시보드로 리다이렉트 (REST 대신 폼 사용하고 싶을 때)
    @PostMapping("/todos/new")
    public String submit(@RequestParam String title,
                         @RequestParam(required=false) String dueDate,
                         @RequestParam(defaultValue="NORMAL") Todo.Priority priority,
                         TodoService svc) {
        svc.add(title, (dueDate==null||dueDate.isBlank())? null : java.time.LocalDate.parse(dueDate), priority);
        return "redirect:/";
    }
}