package amgw.amgw.repository;

import amgw.amgw.todo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByUserIdOrderByDoneAscSortOrderAscCreatedAtAsc(Long userId);
}