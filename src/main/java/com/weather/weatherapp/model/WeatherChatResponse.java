package com.weather.weatherapp.model;

public class WeatherChatResponse {

    private String reply;
    private boolean error;
    private String errorMessage;

    public WeatherChatResponse() {}

    public static WeatherChatResponse success(String reply) {
        WeatherChatResponse response = new WeatherChatResponse();
        response.setReply(reply);
        response.setError(false);
        return response;
    }

    public static WeatherChatResponse error(String errorMessage) {
        WeatherChatResponse response = new WeatherChatResponse();
        response.setError(true);
        response.setErrorMessage(errorMessage);
        response.setReply("I'm unable to connect to Weather AI right now. Please try again.");
        return response;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public boolean isError() { return error; }
    public void setError(boolean error) { this.error = error; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}