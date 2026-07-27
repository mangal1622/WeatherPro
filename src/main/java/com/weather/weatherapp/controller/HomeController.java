package com.weather.weatherapp.controller;

import com.weather.weatherapp.model.ForecastDay;
import com.weather.weatherapp.model.ForecastItem;
import com.weather.weatherapp.model.ForecastResponse;
import com.weather.weatherapp.model.WeatherResponse;
import com.weather.weatherapp.service.WeatherService;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weather.weatherapp.model.GeoLocation;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    private final WeatherService weatherService;

    public HomeController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("backgroundVideo", "clear_day.mp4");

        return "index";
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam String city, Model model) {
        
        WeatherResponse weather = weatherService.getWeather(city);

        if (weather == null) {
            model.addAttribute("error", "City not found. Please enter a valid city name.");
            return "index";
        }

        ForecastResponse forecastResponse =
                weatherService.getForecast(city);

        List<ForecastItem> hourlyForecast =
                weatherService.getHourlyForecast(forecastResponse);

        List<ForecastDay> forecastDays =
                weatherService.getFiveDayForecast(forecastResponse);

        String backgroundVideo = getBackgroundVideo(weather);

        model.addAttribute("weather", weather);
        model.addAttribute("forecastDays", forecastDays);
        model.addAttribute("hourlyForecast", hourlyForecast);
        model.addAttribute("backgroundVideo", backgroundVideo);
        model.addAttribute("weatherTip", getWeatherTip(weather));
        model.addAttribute("weatherTipIcon", getWeatherTipIcon(weather));

        return "index";
    }

    @GetMapping("/weather/location")
    public String getWeatherByLocation(
            @RequestParam double lat,
            @RequestParam double lon,
            Model model) {

        WeatherResponse weather = weatherService.getWeather(lat, lon);

        ForecastResponse forecastResponse =
                weatherService.getForecast(weather.getName());

        List<ForecastItem> hourlyForecast =
                weatherService.getHourlyForecast(forecastResponse);

        List<ForecastDay> forecastDays =
                weatherService.getFiveDayForecast(forecastResponse);

        String backgroundVideo = getBackgroundVideo(weather);

        model.addAttribute("weather", weather);
        model.addAttribute("forecastDays", forecastDays);
        model.addAttribute("hourlyForecast", hourlyForecast);   
        model.addAttribute("backgroundVideo", backgroundVideo);

        return "index";
    }

    private String getBackgroundVideo(WeatherResponse weather) {

        if (weather == null ||
            weather.getWeather() == null ||
            weather.getWeather().isEmpty()) {

            return "clear_day.mp4";
        }

        // Check if it is currently day or night
        long currentTime = weather.getDt();
        long sunrise = weather.getSys().getSunrise();
        long sunset = weather.getSys().getSunset();

        boolean isDay = currentTime >= sunrise && currentTime < sunset;

        String condition = weather.getWeather().get(0).getMain();

        switch (condition) {

            case "Clear":
                return isDay ? "clear_day.mp4" : "clear_night.mp4";

            case "Clouds":
                return isDay ? "clouds_day.mp4" : "clouds_night.mp4";

            case "Rain":
                return isDay ? "rain_day.mp4" : "rain_night.mp4";

            case "Drizzle":
                return isDay ? "drizzle_day.mp4" : "drizzle_night.mp4";

            case "Thunderstorm":
                return isDay ? "thunderstorm_day.mp4" : "thunderstorm_night.mp4";

            case "Snow":
                return isDay ? "snow_day.mp4" : "snow_night.mp4";

            case "Mist":
            case "Fog":
            case "Haze":
            case "Smoke":
                return isDay ? "mist_day.mp4" : "mist_night.mp4";

            case "Dust":
            case "Sand":
            case "Ash":
                return isDay ? "dust_day.mp4" : "dust_night.mp4";

            case "Squall":
                return isDay ? "windy_day.mp4" : "windy_night.mp4";

            case "Tornado":
                return "tornado.mp4";

            default:
                return isDay ? "clear_day.mp4" : "clear_night.mp4";
        }
    }

    private String getWeatherTip(WeatherResponse weather) {

        if (weather == null ||
            weather.getWeather() == null ||
            weather.getWeather().isEmpty()) {

            return "Stay prepared for today's weather.";
        }

        String condition = weather.getWeather().get(0).getMain();

        switch(condition){

            case "Clear":
                return "Wear sunglasses and stay hydrated.";

            case "Clouds":
                return "Pleasant weather for outdoor activities.";

            case "Rain":
                return "Carry an umbrella before heading out.";

            case "Drizzle":
                return "A light umbrella is recommended.";

            case "Thunderstorm":
                return "Stay indoors during lightning.";

            case "Snow":
                return "Wear warm clothing and walk carefully.";

            case "Mist":
            case "Fog":
                return "Drive slowly and use low-beam headlights.";

            case "Smoke":
            case "Haze":
                return "Limit outdoor activities if possible.";

            case "Dust":
            case "Sand":
                return "Protect your eyes and wear a mask.";

            case "Squall":
                return "Secure loose objects outdoors.";

            case "Tornado":
                return "Seek shelter immediately.";

            default:
                return "Have a wonderful day!";
        }
    }

    private String getWeatherTipIcon(WeatherResponse weather){

        if(weather == null){
            return "🌤️";
        }

        switch(weather.getWeather().get(0).getMain()){

            case "Clear":
                return "☀️";

            case "Clouds":
                return "☁️";

            case "Rain":
                return "☔";

            case "Drizzle":
                return "🌦️";

            case "Thunderstorm":
                return "⛈️";

            case "Snow":
                return "❄️";

            case "Mist":
            case "Fog":
                return "🌫️";

            case "Smoke":
            case "Haze":
                return "😷";

            case "Dust":
            case "Sand":
                return "🌪️";

            case "Squall":
                return "💨";

            case "Tornado":
                return "🌪️";

            default:
                return "🌤️";
        }
    }

    @GetMapping("/weather/suggestions")
    @ResponseBody
    public List<GeoLocation> getCitySuggestions(
            @RequestParam String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {

        return weatherService.getCitySuggestions(query, lat, lon);
    }
}