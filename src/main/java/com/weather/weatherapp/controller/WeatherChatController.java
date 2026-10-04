package com.weather.weatherapp.controller;

import com.weather.weatherapp.model.WeatherChatRequest;
import com.weather.weatherapp.model.WeatherChatResponse;
import com.weather.weatherapp.service.WeatherChatService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherChatController {

    private final WeatherChatService weatherChatService;

    public WeatherChatController(WeatherChatService weatherChatService) {
        this.weatherChatService = weatherChatService;
    }

    @PostMapping("/chat")
    public WeatherChatResponse chat(@RequestBody WeatherChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return WeatherChatResponse.error("Message cannot be empty");
        }
        
        // Validate message length
        if (request.getMessage().length() > 1000) {
            return WeatherChatResponse.error("Message too long");
        }

        return weatherChatService.getChatResponse(request);
    }
}