package com.weather.weatherapp.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WeatherServiceException.class)
    public String handleWeatherServiceException(
            WeatherServiceException ex,
            Model model) {

        model.addAttribute(
                "error",
                "Unable to connect to the Weather Service. Please try again in a few moments."
        );

        return "index";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(
            Exception ex,
            Model model) {

        model.addAttribute(
                "error",
                "Something went wrong. Please try again."
        );

        return "index";
    }
}