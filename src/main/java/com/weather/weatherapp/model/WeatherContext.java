package com.weather.weatherapp.model;

import java.util.List;

public class WeatherContext {

    private String city;
    private String country;
    private double temperature;
    private double feelsLike;
    private String condition;
    private int humidity;
    private double windSpeed;
    private int pressure;
    private int visibility;
    private String sunrise;
    private String sunset;
    private List<HourlyForecastItem> hourlyForecast;
    private List<DailyForecastItem> dailyForecast;

    public WeatherContext() {}

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public int getPressure() { return pressure; }
    public void setPressure(int pressure) { this.pressure = pressure; }

    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }

    public String getSunrise() { return sunrise; }
    public void setSunrise(String sunrise) { this.sunrise = sunrise; }

    public String getSunset() { return sunset; }
    public void setSunset(String sunset) { this.sunset = sunset; }

    public List<HourlyForecastItem> getHourlyForecast() { return hourlyForecast; }
    public void setHourlyForecast(List<HourlyForecastItem> hourlyForecast) { this.hourlyForecast = hourlyForecast; }

    public List<DailyForecastItem> getDailyForecast() { return dailyForecast; }
    public void setDailyForecast(List<DailyForecastItem> dailyForecast) { this.dailyForecast = dailyForecast; }

    public static class HourlyForecastItem {
        private String time;
        private double temperature;
        private String condition;
        private double precipitationProbability;

        public HourlyForecastItem() {}

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public double getPrecipitationProbability() { return precipitationProbability; }
        public void setPrecipitationProbability(double precipitationProbability) { this.precipitationProbability = precipitationProbability; }
    }

    public static class DailyForecastItem {
        private String day;
        private double minTemp;
        private double maxTemp;
        private String condition;
        private String description;

        public DailyForecastItem() {}

        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }

        public double getMinTemp() { return minTemp; }
        public void setMinTemp(double minTemp) { this.minTemp = minTemp; }

        public double getMaxTemp() { return maxTemp; }
        public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}