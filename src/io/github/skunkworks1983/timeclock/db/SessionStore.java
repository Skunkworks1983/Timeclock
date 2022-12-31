package io.github.skunkworks1983.timeclock.db;

import com.google.inject.Inject;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import static io.github.skunkworks1983.timeclock.db.generated.tables.Sessions.SESSIONS;

public class SessionStore
{
    private final ScheduleStore scheduleStore;
    
    @Inject
    public SessionStore(ScheduleStore scheduleStore)
    {
        this.scheduleStore = scheduleStore;
    }
    
    public long startSession(Member startedBy, boolean delayUntilScheduled)
    {
        if(startedBy.getRole().equals(Role.ADMIN) && !(isSessionActive() || getQueuedSessionStart() > 0))
        {
            return DatabaseConnector.runQuery(query ->
                                              {
                                                  long startTime = TimeUtil.getCurrentTimestamp();
                                                  LocalTime start = scheduleStore.getSchedule().getStart();
                                                  if(delayUntilScheduled && start.isAfter(
                                                          LocalTime.parse("00:00")) && start.isAfter(LocalTime.now()))
                                                  {
                                                      startTime = OffsetDateTime.now()
                                                                                .withHour(start.getHour())
                                                                                .withMinute(start.getMinute())
                                                                                .withSecond(0)
                                                                                .toEpochSecond();
                                                  }
                                                  query.insertInto(SESSIONS)
                                                       .values(0, startedBy.getId().toString(), null, startTime, 0)
                                                       .execute();
                                                  return startTime;
                                              });
        }
        return -1;
    }
    
    public void endSession(Member endedBy)
    {
        if(endedBy.getRole().equals(Role.ADMIN) && (isSessionActive() || getQueuedSessionStart() > 0))
        {
            DatabaseConnector.runQuery(query ->
                                       {
                                           long sessionEnd = TimeUtil.getCurrentTimestamp();
                                           long sessionStart = query.select(SESSIONS.START)
                                                                    .from(SESSIONS)
                                                                    .where(SESSIONS.END.eq(0L))
                                                                    .fetchAny()
                                                                    .get(SESSIONS.START);
                                           double scheduledHours = scheduleStore.getScheduleOverlap(
                                                   TimeUtil.getDateTime(sessionStart),
                                                   TimeUtil.getDateTime(sessionEnd));
                
                                           query.update(SESSIONS)
                                                .set(SESSIONS.END, sessionEnd)
                                                .set(SESSIONS.SCHEDULEDHOURS, (float) scheduledHours)
                                                .set(SESSIONS.ENDEDBY, endedBy.getId().toString())
                                                .where(SESSIONS.END.eq(0L))
                                                .execute();
                                           return null;
                                       });
        }
    }
    
    public boolean isSessionActive()
    {
        Boolean result = DatabaseConnector.<Boolean>runQuery((query) ->
                                                             {
                                                                 long now = TimeUtil.getCurrentTimestamp();
                                                                 long openSessions = query.select()
                                                                                          .from(SESSIONS)
                                                                                          .where(SESSIONS.START.le(now)
                                                                                                               .and(SESSIONS.END.eq(
                                                                                                                                    0L)
                                                                                                                                .or(SESSIONS.END.ge(
                                                                                                                                        now))
                                                                                                                   )
                                                                                                )
                                                                                          .fetch()
                                                                                          .stream()
                                                                                          .count();
                                                                 return openSessions > 0;
                                                             });
        return result != null && result;
    }
    
    public long getQueuedSessionStart()
    {
        return DatabaseConnector.runQuery(query -> {
            Long result = query.selectFrom(SESSIONS)
                               .where(SESSIONS.START.ge(TimeUtil.getCurrentTimestamp()))
                               .fetchAny(SESSIONS.START);
            if(result != null)
            {
                return result;
            }
            return Long.valueOf(0);
        });
    }
    
    public double calculateScheduledHours()
    {
        return DatabaseConnector.runQuery(query ->
                                          {
                                              double total = 0;
                                              for(float hours : query.selectFrom(SESSIONS)
                                                                     .fetch(SESSIONS.SCHEDULEDHOURS))
                                              {
                                                  total += hours;
                                              }
                                              return total;
                                          });
    }
}
