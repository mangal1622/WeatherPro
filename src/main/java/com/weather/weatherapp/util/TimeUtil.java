package com.weather.weatherapp.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a");

    public static String formatUnixTime(long unixTime) {

        return Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.systemDefault())
                .format(FORMATTER);

    }

}