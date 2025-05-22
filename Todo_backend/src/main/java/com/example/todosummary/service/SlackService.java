package com.example.todosummary.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SlackService {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final WebClient webClient = WebClient.create();

    public boolean sendToSlack(String message) {
        Map<String, String> payload = Map.of("text", message);
        try {
            webClient.post()
                .uri(slackWebhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
