package com.example.todosummary.service;


import com.example.todosummary.exception.ResourceNotFoundException;
import com.example.todosummary.model.Todo;
import com.example.todosummary.repository.TodoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Todo getTodoById(Long id) {
        return todoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id " + id));
    }

    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public Todo updateTodo(Long id, Todo todoDetails) {
        Todo todo = getTodoById(id);
        todo.setTitle(todoDetails.getTitle());
        todo.setDescription(todoDetails.getDescription());
        todo.setCompleted(todoDetails.isCompleted());
        return todoRepository.save(todo);
    }

    public void deleteTodo(Long id) {
        Todo todo = getTodoById(id);
        todoRepository.delete(todo);
    }
    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private SlackService slackService;

    public void processTodos(List<Todo> todos) {
        // Convert List<Todo> to String
        String inputForSummary = todos.stream()
            .map(Todo::getDescription)  // or whichever method gets task text
            .collect(Collectors.joining(". "));

        // Call OpenAI to generate summary
        String summary = openAIService.generateSummary(inputForSummary);

        // Send summary to Slack
        slackService.sendToSlack(summary);
    }
}

