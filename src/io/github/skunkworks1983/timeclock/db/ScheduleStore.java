package io.github.skunkworks1983.timeclock.db;

import com.google.inject.Inject;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ScheduleStore
{
    private final Map<DayOfWeek, DaySchedule> scheduleMap;
    
    @Inject
    public ScheduleStore()
    {
        scheduleMap = new HashMap<>();
        
        // baseline meeting schedule
        scheduleMap.put(DayOfWeek.MONDAY, new DaySchedule("18:00", "20:30"));
        scheduleMap.put(DayOfWeek.TUESDAY, new DaySchedule("18:00", "20:30"));
        scheduleMap.put(DayOfWeek.WEDNESDAY, new DaySchedule("18:00", "20:30"));
        scheduleMap.put(DayOfWeek.THURSDAY, new DaySchedule("18:00", "20:30"));
        scheduleMap.put(DayOfWeek.FRIDAY, new DaySchedule("00:00", "00:00"));
        scheduleMap.put(DayOfWeek.SATURDAY, new DaySchedule("11:00", "16:00"));
        scheduleMap.put(DayOfWeek.SUNDAY, new DaySchedule("00:00", "00:00"));
    }
    
    double getScheduleOverlap(LocalDateTime start, LocalDateTime end)
    {
        DaySchedule daySchedule = scheduleMap.get(start.getDayOfWeek());
        
        // if the given interval doesn't overlap with the schedule, skip the math and return 0
        // if session goes past midnight, end's time will be before start's, so check the date too
        if(!(start.toLocalTime().isBefore(daySchedule.getEnd())
                && (end.toLocalDate().isAfter(start.toLocalDate()) || end.toLocalTime()
                                                                         .isAfter(daySchedule.getStart()))))
        {
            return 0;
        }
        
        LocalDateTime intervalStart = LocalDateTime.from(start);
        LocalDateTime intervalEnd = LocalDateTime.from(end);
        if(start.toLocalTime().isBefore(daySchedule.getStart()))
        {
            intervalStart = intervalStart.withHour(daySchedule.getStart().getHour())
                                         .withMinute(daySchedule.getStart().getMinute())
                                         .withSecond(0);
        }
        
        if(end.toLocalDate().isAfter(start.toLocalDate()) || end.toLocalTime().isAfter(daySchedule.getEnd()))
        {
            intervalEnd = intervalEnd.withHour(daySchedule.getEnd().getHour())
                                     .withMinute(daySchedule.getEnd().getMinute())
                                     .withSecond(0);
        }
        
        return Duration.between(intervalStart, intervalEnd).getSeconds() / 3600.;
    }
    
    public DaySchedule getSchedule()
    {
        return getSchedule(LocalDateTime.now());
    }
    
    public DaySchedule getSchedule(LocalDateTime date)
    {
        return scheduleMap.get(date.getDayOfWeek());
    }
}
