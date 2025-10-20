package amgw.amgw.service;

import amgw.amgw.service.CurrentUserService;
import amgw.amgw.todo.model.Todo;
import amgw.amgw.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service @RequiredArgsConstructor
public class TodoService {
    private final TodoRepository repo;
    private final CurrentUserService current;

    public List<Todo> myTodos(){
        return repo.findByUserIdOrderByDoneAscSortOrderAscCreatedAtAsc(current.currentUserId());
    }

    @Transactional
    public Todo add(String title, LocalDate due, Todo.Priority prio){
        var t = Todo.builder()
                .userId(current.currentUserId())
                .title(title)
                .done(false)
                .dueDate(due)
                .priority(prio!=null?prio: Todo.Priority.NORMAL)
                .sortOrder((int)(System.currentTimeMillis()/1000))
                .build();
        return repo.save(t);
    }

    @Transactional public Todo toggle(Long id, boolean done){
        var t = repo.findById(id).orElseThrow();
        requireMine(t);
        t.setDone(done);
        return t;
    }
    @Transactional public Todo rename(Long id, String title){
        var t = repo.findById(id).orElseThrow();
        requireMine(t);
        t.setTitle(title);
        return t;
    }
    @Transactional public void remove(Long id){
        var t = repo.findById(id).orElseThrow();
        requireMine(t);
        repo.delete(t);
    }
    private void requireMine(Todo t){
        if(!Objects.equals(t.getUserId(), current.currentUserId()))
            throw new AccessDeniedException("본인 항목만 수정/삭제 가능");
    }
}