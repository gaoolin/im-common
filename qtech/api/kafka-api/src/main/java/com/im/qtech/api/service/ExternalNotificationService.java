package com.im.qtech.api.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

@Service
public class ExternalNotificationService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void notifyExternalSystem(String message) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://external-api.com/notifications"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        // 异步发送HTTP请求
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        // 处理错误
                    }
                });
    }
}

