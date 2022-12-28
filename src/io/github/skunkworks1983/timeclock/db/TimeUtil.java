package io.github.skunkworks1983.timeclock.db;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class TimeUtil
{
    public static long getCurrentTimestamp()
    {
        return OffsetDateTime.now().toEpochSecond();
    }
    
    public static String formatTime(long millis)
    {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME
                .format(LocalDateTime.ofEpochSecond(millis, 0, OffsetDateTime.now().getOffset()));
    }
    
    public static LocalDateTime getDateTime(long seconds)
    {
        return LocalDateTime.ofEpochSecond(seconds,
                                           0,
                                           OffsetDateTime.now().getOffset());
    }
    
    public static double convertSecToHour(long seconds)
    {
        return seconds/(60. * 60);
    }
    
    public static long convertHourToSec(double hours)
    {
        return (long)(hours * 60 * 60);
    }
}
