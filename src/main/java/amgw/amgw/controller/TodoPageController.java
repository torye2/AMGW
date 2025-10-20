package amgw.amgw.controller;

import amgw.amgw.todo.model.Todo;
import amgw.amgw.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TodoPageController {

    private final TodoService svc;

    @GetMapping("/todos/new")
    public String newPage() {
        return "todos_new";
    }

    @PostMapping("/todos/new")
    public String submit(@RequestParam String title,
                         @RequestParam(required = false) String dueDate,
                         @RequestParam(defaultValue = "NORMAL") Todo.Priority priority) {

        svc.add(title,
                (dueDate == null || dueDate.isBlank()) ? null : java.time.LocalDate.parse(dueDate),
                priority);

        return "redirect:/";
    }
}