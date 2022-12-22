package io.github.skunkworks1983.timeclock.db;

import java.time.LocalTime;

public class DaySchedule
{
    private LocalTime start;
    private LocalTime end;
    
    public DaySchedule(String startTime, String endTime)
    {
        start = LocalTime.parse(startTime);
        end = LocalTime.parse(endTime);
    }
}
