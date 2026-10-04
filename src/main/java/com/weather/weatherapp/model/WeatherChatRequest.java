package com.weather.weatherapp.model;

import java.util.List;

public class WeatherChatRequest {

    private String message;
    private WeatherContext weatherContext;
    private List<ChatMessage> conversationHistory;

    public WeatherChatRequest() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public WeatherContext getWeatherContext() { return weatherContext; }
    public void setWeatherContext(WeatherContext weatherContext) { this.weatherContext = weatherContext; }

    public List<ChatMessage> getConversationHistory() { return conversationHistory; }
    public void setConversationHistory(List<ChatMessage> conversationHistory) { this.conversationHistory = conversationHistory; }

    public static class ChatMessage {
        private String role;
        private String content;

        public ChatMessage() {}

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}