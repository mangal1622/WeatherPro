package com.weather.weatherapp.model;

import java.util.List;

public class WeatherResponse {

    private String name;
    private Main main;
    private Wind wind;
    private List<Weather> weather;
    private int visibility;
    private Sys sys;
    private String formattedSunrise;
    private String formattedSunset;
    private int timezone;
    private long dt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public String getFormattedSunrise() {
        return formattedSunrise;
    }

    public void setFormattedSunrise(String formattedSunrise) {
        this.formattedSunrise = formattedSunrise;
    }

    public String getFormattedSunset() {
        return formattedSunset;
    }

    public void setFormattedSunset(String formattedSunset) {
        this.formattedSunset = formattedSunset;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }   

    public long getDt() {
        return dt;
    }   

    public void setDt(long dt) {
        this.dt = dt;
    }
}