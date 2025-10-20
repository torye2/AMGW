package amgw.amgw.controller;

import amgw.amgw.todo.model.Todo;
import amgw.amgw.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoApiController {
    private final TodoService svc;

    @GetMapping
    public List<Todo> list(){ return svc.myTodos(); }

    @PostMapping
    public Map<String,Object> create(@RequestBody Map<String,String> p){
        String title = Optional.ofNullable(p.get("title")).orElse("").trim();
        if(title.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title required");
        LocalDate due = Optional.ofNullable(p.get("dueDate")).filter(s->!s.isBlank()).map(LocalDate::parse).orElse(null);
        Todo.Priority pr = Optional.ofNullable(p.get("priority")).map(Todo.Priority::valueOf).orElse(Todo.Priority.NORMAL);
        var saved = svc.add(title, due, pr);
        return Map.of("ok", true, "item", saved);
    }

    @PatchMapping("/{id}")
    public Map<String,Object> patch(@PathVariable Long id, @RequestBody Map<String,Object> p){
        if(p.containsKey("done")){
            var updated = svc.toggle(id, Boolean.TRUE.equals(p.get("done")));
            return Map.of("ok",true,"item",updated);
        }
        if(p.containsKey("title")){
            var updated = svc.rename(id, String.valueOf(p.get("title")));
            return Map.of("ok",true,"item",updated);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no supported fields");
    }

    @DeleteMapping("/{id}")
    public Map<String,Object> delete(@PathVariable Long id){
        svc.remove(id);
        return Map.of("ok",true);
    }
}