package com.team.ehr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.team.ehr.exception.BadRequestException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final String endpoint;

    public GeminiClient(ObjectMapper objectMapper,
                        @Value("${gemini.apiKey}") String apiKey,
                        @Value("${gemini.model}") String model,
                        @Value("${gemini.endpoint}") String endpoint) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
    }

    public String generate(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Gemini API key is not configured");
        }
        try {
            ObjectNode body = objectMapper.createObjectNode();
            ArrayNode contents = body.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", prompt);

            String url = endpoint + "/" + model + ":generateContent?key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new BadRequestException("Gemini API error: " + response.statusCode());
            }
            JsonNode json = objectMapper.readTree(response.body());
            JsonNode textNode = json.at("/candidates/0/content/parts/0/text");
            if (textNode.isMissingNode()) {
                throw new BadRequestException("Gemini API returned no content");
            }
            return textNode.asText();
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Gemini request failed");
        }
    }
}
