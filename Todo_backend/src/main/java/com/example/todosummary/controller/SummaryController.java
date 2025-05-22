package com.example.todosummary.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todosummary.model.Todo;
import com.example.todosummary.repository.TodoRepository;
import com.example.todosummary.service.OpenAIService;
import com.example.todosummary.service.SlackService;

@RestController
@CrossOrigin(origins = "*")
public class SummaryController {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private SlackService slackService;

    @PostMapping("/summarize")
    public ResponseEntity<String> summarizeAndSend() {
        List<Todo> todos = todoRepository.findAll();

        if (todos.isEmpty()) {
            return ResponseEntity.badRequest().body("No todos to summarize.");
        }

        // Convert the list of Todo items into a formatted string
        String tasksText = todos.stream()
            .map(todo -> "Title: " + todo.getTitle() + ", Description: " + todo.getDescription())
            .reduce("", (acc, task) -> acc.isEmpty() ? task : acc + "\n" + task);

        // Generate summary using OpenAI service
        String summary = openAIService.generateSummary(tasksText);

        // Send the summary to Slack and check if successful
        boolean sent = slackService.sendToSlack(summary);

        if (sent) {
            return ResponseEntity.ok("Summary sent to Slack.");
        } else {
            return ResponseEntity.status(500).body("Failed to send summary.");
        }
    }
}
