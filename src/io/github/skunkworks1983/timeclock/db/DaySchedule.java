package io.github.skunkworks1983.timeclock.db;

import java.time.LocalTime;
import java.util.Objects;

public class DaySchedule
{
    private final LocalTime start;
    private final LocalTime end;
    
    public DaySchedule(String startTime, String endTime)
    {
        start = LocalTime.parse(startTime);
        end = LocalTime.parse(endTime);
    }
    
    public LocalTime getStart()
    {
        return start;
    }
    
    public LocalTime getEnd()
    {
        return end;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }
        DaySchedule that = (DaySchedule) o;
        return start.equals(that.start) && end.equals(that.end);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(start, end);
    }
}
