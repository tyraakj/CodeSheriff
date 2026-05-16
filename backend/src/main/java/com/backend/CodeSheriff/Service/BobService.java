package com.backend.CodeSheriff.Service;

import com.backend.CodeSheriff.Exception.AiIntegrationException;
import com.backend.CodeSheriff.Model.BobAnalysis;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BobService {
    @Value("${ibm.api.key}")
    private String apiKey;

    @Value("${ibm.api.url}")
    private String apiUrl;

    @Value("${ibm.project.id:}")
    private String projectId;

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public BobService() {
        // Configure WebClient with timeouts
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofSeconds(30))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));

        this.webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    public BobAnalysis analyze (String className, String methodName, String methodBody , String classContext){

        String prompt = buildBobPrompt(className, methodName, methodBody ,classContext);
        String rawResponse = callIbmBob(prompt);
        return parseBobResponse(rawResponse, methodBody);

    }

    private String buildBobPrompt(String className, String methodName, String methodBody , String classContext){
        return
                """
                   You are IBM Bob, a senior software engineer who specializes in explaining
                   complex Java code clearly and bluntly. You never sugarcoat problems.
                
                   Analyze this Java method from class %s:
                        
                   METHOD NAME: %s
                        
                   METHOD CODE:
                   ```java
                   %s
                   ```
                        
                   CLASS CONTEXT (other methods in the same class):
                   ```java
                   %s
                   ```
                        
                   Respond ONLY with a valid JSON object. No explanation before or after.\s
                   Use this exact structure:
                   {
                   "whatItDoes": "One clear sentence describing what this method actually does",
                   "intentVsReality": "If the method is doing too many things, violating SRP, or has hidden complexity — say it bluntly. If it's fine, say so.",
                   "whereToStart": "Point to the exact line or method call that is the most important entry point for understanding this code"
                   } 
                """.formatted(className, methodName, methodBody, classContext);
    }

    private String callIbmBob(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", prompt);
        requestBody.put("model_id", "meta-llama/llama-3-70b-instruct");
        
        // Use configured project ID
        if (projectId != null && !projectId.isEmpty()) {
            requestBody.put("project_id", projectId);
        } else {
            throw new AiIntegrationException("IBM project ID not configured", null);
        }
        
        // Add parameters for better control
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_new_tokens", 2000);
        parameters.put("temperature", 0.7);
        parameters.put("top_p", 0.9);
        requestBody.put("parameters", parameters);

        try {
            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Extract just the AI's text from IBM's giant JSON response
            JsonNode root = mapper.readTree(response);
            return root.at("/results/0/generated_text").asText();

        } catch (Exception e) {
            // If IBM crashes, our GlobalExceptionHandler will catch this!
            throw new AiIntegrationException("IBM API communication failed", e);
        }
    }

    private BobAnalysis parseBobResponse(String rawText, String methodBody) {
        try {
            String cleaned = rawText.replace("```json", "").replace("```", "").trim();
            JsonNode json = mapper.readTree(cleaned);


            int lineCount = methodBody.split("\n").length;
            boolean hasTests = methodBody.toLowerCase().contains("@test")
                    || methodBody.toLowerCase().contains("assert");

            return BobAnalysis.builder()
                    .whatItDoes(json.get("whatItDoes").asText())
                    .intentVsReality(json.get("intentVsReality").asText())
                    .whereToStart(json.get("whereToStart").asText())
                    .lineCount(lineCount)
                    .hasTests(hasTests)
                    .build();

        } catch (Exception e) {
            throw new AiIntegrationException("Failed to parse IBM JSON response", e);
        }
    }
}

