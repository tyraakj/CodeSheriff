package com.backend.CodeSheriff.Service;

import com.backend.CodeSheriff.Model.BobAnalysis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;


@Service
public class BobService {
    @Value("${ibm.api.key}")
    private String apiKey;

    @Value("${ibm.apiURL}")
    private String apiUrl;

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper mapper = new ObjectMapper();

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


    private String callIbmBob(String prompt){
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input",prompt);

        requestBody.put("model_id", "meta-llama/llama-3-70b-instruct");
        requestBody.put("project_id", "YOUR-ACTUAL-PROJECT-ID-HERE");

        try {
            String response = webClient.post()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
    }
}
