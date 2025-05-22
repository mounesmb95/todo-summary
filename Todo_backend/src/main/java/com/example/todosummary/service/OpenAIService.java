package com.example.todosummary.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
        .baseUrl("https://api.openai.com/v1")
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .defaultHeader("Content-Type", "application/json")
        .build();
    @SuppressWarnings("unchecked")
    public String generateSummary(String input) {
        String model = "gpt-3.5-turbo";

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", "Summarize the following tasks:"),
                Map.of("role", "user", "content", input)
            )
        );

        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(
                status -> status.value() == 429,
                clientResponse -> Mono.error(new RuntimeException("Rate limit exceeded. Please try again later."))
            )
            .bodyToMono(Map.class)
            .map(response -> {
                var choices = (List<Map<String, Object>>) response.get("choices");
                var message = (Map<String, Object>) choices.get(0).get("message");
                return message.get("content").toString();
            })
            .retryWhen(
                reactor.util.retry.Retry.backoff(3, java.time.Duration.ofSeconds(2))
                    .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests)
            )
            .block();
    }
}
