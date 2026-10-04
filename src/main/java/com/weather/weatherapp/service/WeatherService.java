    package com.weather.weatherapp.service;

    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.web.client.RestTemplate;

    import com.weather.weatherapp.model.ForecastResponse;
    import com.weather.weatherapp.model.GeoLocation;
    import com.weather.weatherapp.model.WeatherResponse;
    import com.weather.weatherapp.util.TimeUtil;
    import org.springframework.web.client.HttpClientErrorException;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.List;

    import com.weather.weatherapp.model.ForecastDay;
    import com.weather.weatherapp.model.ForecastItem;

    import org.springframework.web.client.ResourceAccessException;
    import com.weather.weatherapp.exception.WeatherServiceException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


    @Service
    public class WeatherService {

        @Value("${weather.api.key}")
        private String apiKey;

        @Value("${weather.api.url}")
        private String apiUrl;

        @Value("${weather.forecast.url}")
        private String forecastUrl;

        @Value("${weather.geocode.url}")
        private String geocodeUrl;

        private final RestTemplate restTemplate;

        public WeatherService(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        public WeatherResponse getWeather(String city) {

            String url = apiUrl + "?q=" + city + "&appid=" + apiKey + "&units=metric";

            try {

                WeatherResponse weatherResponse =
                        restTemplate.getForObject(url, WeatherResponse.class);

                if (weatherResponse != null) {

                    weatherResponse.setFormattedSunrise(
                            TimeUtil.formatUnixTime(weatherResponse.getSys().getSunrise()));

                    weatherResponse.setFormattedSunset(
                            TimeUtil.formatUnixTime(weatherResponse.getSys().getSunset()));
                }
                
                return weatherResponse;

            } catch (HttpClientErrorException e) {

                // Invalid city name, 404, etc.
                return null;

            } catch (ResourceAccessException e) {

                // Network timeout / Connection reset
                throw new WeatherServiceException(
                        "Unable to connect to the weather service.",
                        e
                );
            }
        }

        public List<GeoLocation> getCitySuggestions(String query, Double userLat, Double userLon) {

            if (query == null || query.trim().length() < 1) {
                return new ArrayList<>();
            }

            String url = geocodeUrl
                    + "?q=" + query
                    + "&limit=10"
                    + "&appid=" + apiKey;

            try {

                GeoLocation[] results = restTemplate.getForObject(url, GeoLocation[].class);

                if (results == null) {
                    return new ArrayList<>();
                }

                List<GeoLocation> list = new ArrayList<>(java.util.Arrays.asList(results));

                if (userLat != null && userLon != null) {
                    list.sort(java.util.Comparator.comparingDouble(
                            loc -> distanceKm(userLat, userLon, loc.getLat(), loc.getLon())
                    ));
                }

                if (list.size() > 5) {
                    list = list.subList(0, 5);
                }

                return list;

            } catch (HttpClientErrorException e) {

                return new ArrayList<>();

            } catch (ResourceAccessException e) {

                return new ArrayList<>();
            }
        }

        private double distanceKm(double lat1, double lon1, double lat2, double lon2) {

            double R = 6371;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(dLon / 2) * Math.sin(dLon / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return R * c;
        }

        public ForecastResponse getForecast(String city) {

            String url = forecastUrl
                    + "?q=" + city
                    + "&appid=" + apiKey
                    + "&units=metric";

            try {

                return restTemplate.getForObject(url, ForecastResponse.class);

            } catch (HttpClientErrorException e) {

                return null;

            } catch (ResourceAccessException e) {

                throw new WeatherServiceException(
                        "Unable to fetch forecast information.",
                        e
                );
            }
        }

        public List<ForecastItem> getHourlyForecast(ForecastResponse response) {

            List<ForecastItem> hourlyForecast = new ArrayList<>();

            if (response == null || response.getList() == null) {
                return hourlyForecast;
            }

            int limit = Math.min(8, response.getList().size());

            for (int i = 0; i < limit; i++) {

                ForecastItem item = response.getList().get(i);

                item.setFormattedTime(formatTime12Hour(item.getDtTxt()));

                hourlyForecast.add(item);

            }

            return hourlyForecast;
        }

        public List<ForecastDay> getFiveDayForecast(ForecastResponse response) {

            List<ForecastDay> forecastDays = new ArrayList<>();

            if (response == null || response.getList() == null) {
                return forecastDays;
            }

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Map<LocalDate, List<ForecastItem>> dailyForecasts = new LinkedHashMap<>();

            // Group forecast items by date
            for (ForecastItem item : response.getList()) {

                LocalDate date = LocalDateTime
                        .parse(item.getDtTxt(), formatter)
                        .toLocalDate();

                dailyForecasts
                        .computeIfAbsent(date, k -> new ArrayList<>())
                        .add(item);
            }

            // Skip today only if there are more than 5 dates
            boolean skipToday = dailyForecasts.size() > 5;

            // Create one ForecastDay per date
            for (Map.Entry<LocalDate, List<ForecastItem>> entry : dailyForecasts.entrySet()) {

                List<ForecastItem> items = entry.getValue();

                if (skipToday && entry.getKey().equals(LocalDate.now())) {
                    continue;
                }

                double minTemp = Double.MAX_VALUE;
                double maxTemp = Double.MIN_VALUE;

                ForecastItem representative = items.get(0);

                for (ForecastItem item : items) {

                    if (item.getMain().getTempMin() < minTemp) {
                        minTemp = item.getMain().getTempMin();
                    }

                    if (item.getMain().getTempMax() > maxTemp) {
                        maxTemp = item.getMain().getTempMax();
                    }

                    LocalDateTime dt = LocalDateTime.parse(item.getDtTxt(), formatter);

                    // Prefer the 12:00 PM forecast for icon & description
                    if (dt.getHour() == 12) {
                        representative = item;
                    }
                }

                ForecastDay day = new ForecastDay();

                day.setDay(entry.getKey()
                        .getDayOfWeek()
                        .name()
                        .substring(0, 3));

                day.setIcon(representative.getWeather().get(0).getIcon());

                day.setDescription(representative.getWeather().get(0).getDescription());

                day.setMinTemp(minTemp);

                day.setMaxTemp(maxTemp);

                forecastDays.add(day);
            }

            // Calculate week's min & max
            double weekMin = forecastDays.stream()
                    .filter(Objects::nonNull)
                    .mapToDouble(day -> day.getMinTemp())
                    .min()
                    .orElse(0);

            double weekMax = forecastDays.stream()
                    .filter(Objects::nonNull)
                    .mapToDouble(day -> day.getMaxTemp())
                    .max()
                    .orElse(0);

            double range = weekMax - weekMin;

            if (range == 0) {
                range = 1;
            }

            // Calculate bar positions
            for (ForecastDay day : forecastDays) {

                day.setBarStart(((day.getMinTemp() - weekMin) / range) * 100);

                day.setBarWidth(((day.getMaxTemp() - day.getMinTemp()) / range) * 100);
            }
            
            // Keep only the next 5 days
            if (forecastDays.size() > 5) {
                forecastDays = new ArrayList<>(forecastDays.subList(0, 5));
            }

            return forecastDays;
        }

        private String formatTime12Hour(String dateTime) {

            DateTimeFormatter input =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            DateTimeFormatter output =
                    DateTimeFormatter.ofPattern("hh:mm a");

            LocalDateTime date = LocalDateTime.parse(dateTime, input);

            return date.format(output);
        }

        public WeatherResponse getWeather(double lat, double lon) {

            String url = apiUrl
                    + "?lat=" + lat
                    + "&lon=" + lon
                    + "&appid=" + apiKey
                    + "&units=metric";

            try {

                WeatherResponse weatherResponse =
                        restTemplate.getForObject(url, WeatherResponse.class);

                if (weatherResponse != null) {

                    weatherResponse.setFormattedSunrise(
                            TimeUtil.formatUnixTime(weatherResponse.getSys().getSunrise()));

                    weatherResponse.setFormattedSunset(
                            TimeUtil.formatUnixTime(weatherResponse.getSys().getSunset()));
                }

                return weatherResponse;

            } catch (HttpClientErrorException e) {

                return null;

            } catch (ResourceAccessException e) {

                throw new WeatherServiceException(
                        "Unable to connect to the weather service.",
                        e
                );
            }
        }
    }