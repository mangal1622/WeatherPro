package com.weather.weatherapp.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForecastItem {

    @JsonProperty("dt_txt")
    private String dtTxt;

    private String formattedTime;

    private ForecastMain main;

    private List<ForecastWeather> weather;

    private Wind wind;

    public String getDtTxt() {
        return dtTxt;
    }

    public void setDtTxt(String dtTxt) {
        this.dtTxt = dtTxt;
    }

    public ForecastMain getMain() {
        return main;
    }

    public void setMain(ForecastMain main) {
        this.main = main;
    }

    public List<ForecastWeather> getWeather() {
        return weather;
    }

    public void setWeather(List<ForecastWeather> weather) {
        this.weather = weather;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
}