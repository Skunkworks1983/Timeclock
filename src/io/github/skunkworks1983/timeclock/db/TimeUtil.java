package io.github.skunkworks1983.timeclock.db;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    
    public static LocalDateTime getDateTime(long millis)
    {
        return LocalDateTime.ofEpochSecond(TimeUnit.MILLISECONDS.toSeconds(millis),
                                           (int)TimeUnit.MILLISECONDS.toNanos(millis % 1000),
                                           OffsetDateTime.now().getOffset());
    }
}
