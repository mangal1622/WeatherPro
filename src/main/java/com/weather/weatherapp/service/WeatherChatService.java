package com.weather.weatherapp.service;

import com.weather.weatherapp.model.WeatherChatRequest;
import com.weather.weatherapp.model.WeatherChatResponse;
import com.weather.weatherapp.model.WeatherContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;



import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WeatherChatService {

    private final WebClient webClient;
    private final String systemPrompt;
    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final double temperature;
    private final int maxTokens;
    private final boolean aiEnabled;

    public WeatherChatService(WebClient.Builder webClientBuilder,
                               @Value("${weather.ai.api.url}") String apiUrl,
                               @Value("${weather.ai.api.key}") String apiKey,
                               @Value("${weather.ai.model}") String model,
                               @Value("${weather.ai.temperature:0.3}") double temperature,
                               @Value("${weather.ai.max-tokens:500}") int maxTokens) {
        this.webClient = webClientBuilder.build();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.aiEnabled = apiKey != null && !apiKey.trim().isEmpty();
        
        // Log configuration status without exposing the actual API key
        if (this.aiEnabled) {
            System.out.println("AI API key configured: true");
            System.out.println("AI API URL configured: true");
            System.out.println("AI model: " + this.model);
        } else {
            System.out.println("AI API key configured: false");
        }
        
        this.systemPrompt = "You are WeatherPro AI, a weather-focused assistant.\n\n" +
            "Answer questions using the supplied weather and forecast data.\n" +
            "Never invent current weather information.\n" +
            "If the supplied data does not contain information needed to answer a question, clearly say that the information is unavailable.\n" +
            "For forecasts, use phrases such as 'According to the current forecast' rather than presenting predictions as guarantees.\n" +
            "Keep answers concise, useful, and easy to understand.\n" +
            "You can explain weather concepts when asked.\n" +
            "You must remain focused on weather-related questions.\n\n" +
            "If the user asks unrelated questions such as programming, general knowledge, or non-weather topics, politely respond:\n" +
            "'I'm WeatherPro AI, so I can help with weather, forecasts, and weather-related questions.'";
    }

    public WeatherChatResponse getChatResponse(WeatherChatRequest request) {
        if (!aiEnabled) {
            return WeatherChatResponse.error("Weather AI is not configured. Please set WEATHER_AI_API_KEY environment variable.");
        }
        
        if (request.getWeatherContext() == null) {
            return WeatherChatResponse.error("Weather data is not available.");
        }

        try {
            String contextPrompt = buildContextPrompt(request.getWeatherContext());
            List<Map<String, String>> messages = buildMessages(contextPrompt, request);

            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "temperature", temperature,
                "max_tokens", maxTokens
            );

            String response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            return parseResponse(response);
            
        } catch (WebClientResponseException e) {
            System.err.println("========== GROQ API ERROR ==========");
            System.err.println("Status: " + e.getStatusCode());
            System.err.println("Response: " + e.getResponseBodyAsString());
            System.err.println("====================================");

            return WeatherChatResponse.error(
                "AI service error: " + e.getStatusCode() +
                " - " + e.getResponseBodyAsString()
            );

        } catch (Exception e) {
            System.err.println("========== WEATHER AI ERROR ==========");
            e.printStackTrace();
            System.err.println("======================================");

            return WeatherChatResponse.error(
                "Unable to connect to Weather AI: " + e.getMessage()
            );
        }
    }

    private String buildContextPrompt(WeatherContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Weather Data:\n");
        sb.append("Location: ").append(context.getCity()).append(", ").append(context.getCountry()).append("\n");
        sb.append("Temperature: ").append(String.format("%.1f", context.getTemperature())).append("°C\n");
        sb.append("Feels Like: ").append(String.format("%.1f", context.getFeelsLike())).append("°C\n");
        sb.append("Condition: ").append(context.getCondition()).append("\n");
        sb.append("Humidity: ").append(context.getHumidity()).append("%\n");
        sb.append("Wind: ").append(String.format("%.1f", context.getWindSpeed())).append(" m/s\n");
        sb.append("Pressure: ").append(context.getPressure()).append(" hPa\n");
        sb.append("Visibility: ").append(context.getVisibility()).append(" km\n");
        sb.append("Sunrise: ").append(context.getSunrise()).append("\n");
        sb.append("Sunset: ").append(context.getSunset()).append("\n");

        if (context.getHourlyForecast() != null && !context.getHourlyForecast().isEmpty()) {
            sb.append("\nHourly Forecast (next 24 hours):\n");
            for (WeatherContext.HourlyForecastItem item : context.getHourlyForecast()) {
                sb.append(String.format("  %s: %.1f°C, %s, precip: %.0f%%\n",
                    item.getTime(), item.getTemperature(), item.getCondition(), item.getPrecipitationProbability() * 100));
            }
        }

        if (context.getDailyForecast() != null && !context.getDailyForecast().isEmpty()) {
            sb.append("\n5-Day Forecast:\n");
            for (WeatherContext.DailyForecastItem item : context.getDailyForecast()) {
                sb.append(String.format("  %s: %.1f-%.1f°C, %s (%s)\n",
                    item.getDay(), item.getMinTemp(), item.getMaxTemp(), item.getCondition(), item.getDescription()));
            }
        }

        return sb.toString();
    }

    private List<Map<String, String>> buildMessages(String contextPrompt, WeatherChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        messages.add(Map.of("role", "system", "content", systemPrompt + "\n\n" + contextPrompt));
        
        if (request.getConversationHistory() != null) {
            for (WeatherChatRequest.ChatMessage msg : request.getConversationHistory()) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }
        
        messages.add(Map.of("role", "user", "content", request.getMessage()));
        
        return messages;
    }

    private WeatherChatResponse parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            return WeatherChatResponse.error("Empty response from AI service");
        }
        
        try {
            // Simple JSON parsing for OpenAI-compatible response
            // Expected format: {"choices":[{"message":{"content":"..."}}]}
            int contentStart = response.indexOf("\"content\":\"");
            if (contentStart == -1) {
                contentStart = response.indexOf("\"content\": \"");
            }
            if (contentStart == -1) {
                return WeatherChatResponse.error("Invalid response format from AI");
            }
            
            contentStart += "\"content\":\"".length();
            int contentEnd = response.indexOf("\"", contentStart);
            while (contentEnd > 0 && response.charAt(contentEnd - 1) == '\\') {
                contentEnd = response.indexOf("\"", contentEnd + 1);
            }
            
            if (contentEnd == -1) {
                return WeatherChatResponse.error("Invalid response format from AI");
            }
            
            String content = response.substring(contentStart, contentEnd)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
            
            return WeatherChatResponse.success(content.trim());
            
        } catch (Exception e) {
            return WeatherChatResponse.error("Failed to parse AI response: " + e.getMessage());
        }
    }
}