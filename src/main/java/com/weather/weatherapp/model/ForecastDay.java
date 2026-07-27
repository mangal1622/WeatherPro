package com.weather.weatherapp.model;

public class ForecastDay {

    private String day;
    private String icon;
    private double minTemp;
    private double maxTemp;
    private String description;
    private double barStart;
    private double barWidth;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(double minTemp) {
        this.minTemp = minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBarStart() {
        return barStart;
    }

    public double getBarWidth() {
        return barWidth;
    }

    public void setBarWidth(double barWidth) {
        this.barWidth = barWidth;
    }

    public void setBarStart(double barStart) {
        this.barStart = barStart;
    }   
}