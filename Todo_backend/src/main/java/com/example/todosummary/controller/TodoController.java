package com.example.todosummary.controller;

import com.example.todosummary.model.Todo;
import com.example.todosummary.repository.TodoRepository;
import com.example.todosummary.service.SummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos")
@CrossOrigin(origins = "http://localhost:3000")
public class TodoController {

    private final TodoRepository repo;
    private final SummaryService summaryService;

    public TodoController(TodoRepository repo, SummaryService summaryService) {
        this.repo = repo;
        this.summaryService = summaryService;
    }

    @GetMapping
    public List<Todo> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Todo add(@RequestBody Todo todo) {
        return repo.save(todo);
    }

    @PutMapping("/{id}")
    public Todo update(@PathVariable Long id, @RequestBody Todo todo) {
        Todo existing = repo.findById(id).orElseThrow();
        existing.setTitle(todo.getTitle());
        existing.setDescription(todo.getDescription());
        return repo.save(existing);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @PostMapping("/../summarize")
    public ResponseEntity<String> summarize() {
        String result = summaryService.summarizeAndSend();
        return ResponseEntity.ok("Summary sent to Slack:\n" + result);
    }
}

