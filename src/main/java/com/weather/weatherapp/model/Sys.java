package com.weather.weatherapp.model;

public class Sys {

    private long sunrise;
    private long sunset;

    private String country;

    public String getCountry() {
        return country;
    }   

    public void setCountry(String country) {
        this.country = country;
    }   

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }
}