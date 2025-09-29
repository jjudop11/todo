package com.example.todo.repository;

import com.example.todo.domain.Todo;
import com.example.todo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    Page<Todo> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("select t from Todo t left join t.tags tg where t.user = :user and (:completed is null or t.completed = :completed) and (:priority is null or t.priority = :priority) and (:tag is null or tg.name = :tag) order by t.createdAt desc")
    Page<Todo> search(@Param("user") User user,
                      @Param("completed") Boolean completed,
                      @Param("priority") String priority,
                      @Param("tag") String tag,
                      Pageable pageable);
}


