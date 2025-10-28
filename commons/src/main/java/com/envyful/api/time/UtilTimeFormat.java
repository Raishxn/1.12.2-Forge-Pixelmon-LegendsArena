package com.envyful.api.time;

import com.google.common.collect.Maps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UtilTimeFormat {

    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
    private static final long SECONDS_PER_MINUTE = 60;
    private static final long MINUTES_PER_HOUR = 60;
    private static final long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    private static final long SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;

    private static final Map<String, DateFormat> DATE_FORMATS = Maps.newHashMap();

    public static String format(Date date, String format) {
        return DATE_FORMATS.computeIfAbsent(format, __ -> new SimpleDateFormat(format)).format(date);
    }

    public static String format(Date date) {
        return DATE_FORMATTER.format(date);
    }

    public static String getTimeUntil(long timeMillis) {
        long timeUntil = timeMillis - System.currentTimeMillis();
        Duration duration = Duration.ofMillis(timeUntil);

        return String.format("%d hour(s) %02d minute(s) and %02d second(s)",
                duration.get(ChronoUnit.SECONDS) / (60 * 60),
                (duration.get(ChronoUnit.SECONDS) / 60) % 60,
                duration.get(ChronoUnit.SECONDS) % 60
        );
    }

    public static String getFormattedDuration(long playTime) {
        long seconds = TimeUnit.SECONDS.convert(playTime, TimeUnit.MILLISECONDS);

        long daysPart = (seconds / SECONDS_PER_DAY);
        long hoursPart = (seconds / SECONDS_PER_HOUR) % 24;
        long minutesPart = (seconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
        long secondsPart = (seconds) % SECONDS_PER_MINUTE;

        StringBuilder builder = new StringBuilder();

        if (daysPart > 0) {
            builder.append(daysPart).append("d ");
        }

        if (hoursPart > 0) {
            builder.append(hoursPart).append("h ");
        }

        if (minutesPart > 0) {
            builder.append(minutesPart).append("m ");
        }

        if (secondsPart > 0) {
            builder.append(secondsPart).append("s");
        }

        return builder.toString();
    }
}
