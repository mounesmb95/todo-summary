package com.example.todosummary.service;

import com.example.todosummary.model.Todo;
import com.example.todosummary.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private final TodoRepository todoRepo;
    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    public SummaryService(TodoRepository todoRepo) {
        this.todoRepo = todoRepo;
        this.webClient = WebClient.create();
    }

    public String summarizeAndSend() {
        List<Todo> todos = todoRepo.findAll();
        String content = todos.stream()
                .map(todo -> "- " + todo.getTitle() + ": " + todo.getDescription())
                .collect(Collectors.joining("\n"));

        String prompt = "Summarize the following todo list:\n" + content;

        String summary = callOpenAI(prompt);
        sendToSlack(summary);
        return summary;
    }

    private String callOpenAI(String prompt) {
        String requestBody = """
            {
              "model": "gpt-3.5-turbo",
              "messages": [
                {"role": "system", "content": "You are a helpful assistant."},
                {"role": "user", "content": "%s"}
              ]
            }
        """.formatted(prompt);

        return webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openaiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    int start = json.indexOf("\"content\":\"") + 11;
                    int end = json.indexOf("\"", start);
                    return json.substring(start, end).replace("\\n", "\n");
                })
                .block();
    }

    private void sendToSlack(String message) {
        String payload = "{\"text\": \"" + message.replace("\"", "'") + "\"}";

        webClient.post()
                .uri(slackWebhookUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
