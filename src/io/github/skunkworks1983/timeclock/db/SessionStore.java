package io.github.skunkworks1983.timeclock.db;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.SessionsRecord;
import org.jooq.Result;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static io.github.skunkworks1983.timeclock.db.generated.tables.Sessions.SESSIONS;

public class SessionStore
{
    private final ScheduleStore scheduleStore;
    
    private final Object scheduledHoursFreshLock = new Object();
    private boolean scheduledHoursFresh = false;
    private double scheduledHours = 0;
    
    @Inject
    public SessionStore(ScheduleStore scheduleStore)
    {
        this.scheduleStore = scheduleStore;
    }
    
    public long startSession(Member startedBy, boolean delayUntilScheduled)
    {
        if(startedBy.getRole().equals(Role.ADMIN) && !(isSessionActive() || getQueuedSessionStart() > 0))
        {
            synchronized(scheduledHoursFreshLock)
            {
                scheduledHoursFresh = false;
                return DatabaseConnector.runQuery(query -> {
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
                         .values(UUID.randomUUID().toString(), 0, startedBy.getId().toString(), null, startTime, 0)
                         .execute();
                    return startTime;
                });
            }
        }
        return -1;
    }
    
    public void endSession(Member endedBy)
    {
        endSession(endedBy, TimeUtil.getCurrentTimestamp());
    }
    
    public void endSession(Member endedBy, long time)
    {
        if(endedBy.getRole().equals(Role.ADMIN) && (isSessionActive() || getQueuedSessionStart() > 0))
        {
            synchronized(scheduledHoursFreshLock)
            {
                DatabaseConnector.runQuery(query -> {
                    long sessionStart = query.select(SESSIONS.START)
                                             .from(SESSIONS)
                                             .where(SESSIONS.END.eq(0L))
                                             .fetchAny()
                                             .get(SESSIONS.START);
                    double scheduledHours = scheduleStore.getScheduleOverlap(
                            TimeUtil.getDateTime(sessionStart),
                            TimeUtil.getDateTime(time));
        
                    query.update(SESSIONS)
                         .set(SESSIONS.END, time)
                         .set(SESSIONS.SCHEDULEDHOURS, (float) scheduledHours)
                         .set(SESSIONS.ENDEDBY, endedBy.getId().toString())
                         .where(SESSIONS.END.eq(0L))
                         .execute();
                    return null;
                });
                scheduledHoursFresh = false;
            }
        }
    }
    
    public boolean isSessionActive()
    {
        Boolean result = DatabaseConnector.<Boolean>runQuery((query) -> {
            long now = TimeUtil.getCurrentTimestamp();
            long openSessions = query.select()
                                     .from(SESSIONS)
                                     .where(SESSIONS.START.le(now)
                                                          .and(SESSIONS.END.eq(0L)
                                                                           .or(SESSIONS.END.ge(now))
                                                              )
                                           )
                                     .fetch()
                                     .stream()
                                     .count();
            return openSessions > 0;
        });
        return result != null && result;
    }
    
    public UUID getOpenSessionId()
    {
        if(isSessionActive())
        {
            return DatabaseConnector.runQuery((query) -> {
                long now = TimeUtil.getCurrentTimestamp();
                String sessionId = query.select()
                                        .from(SESSIONS)
                                        .where(SESSIONS.START.le(now)
                                                             .and(SESSIONS.END.eq(
                                                                                  0L)
                                                                              .or(SESSIONS.END.ge(
                                                                                      now))
                                                                 )
                                              )
                                        .fetchOne(SESSIONS.ID);
                return UUID.fromString(sessionId);
            });
        }
        return null;
    }
    
    public UUID getSessionId(long time)
    {
        String sessionId = DatabaseConnector.runQuery((query) -> {
            long now = TimeUtil.getCurrentTimestamp();
            return query.select()
                        .from(SESSIONS)
                        .where(SESSIONS.START.le(time)
                                             .and(SESSIONS.END.eq(
                                                                  0L)
                                                              .or(SESSIONS.END.ge(
                                                                      time))
                                                 )
                              )
                        .orderBy(SESSIONS.END.desc())
                        .fetchAny(SESSIONS.ID);
        });
        
        return sessionId == null ? null : UUID.fromString(sessionId);
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
        synchronized(scheduledHoursFreshLock)
        {
            if(!scheduledHoursFresh)
            {
                scheduledHours = DatabaseConnector.runQuery(query -> {
                    double total = 0;
                    Map<LocalDate, Result<SessionsRecord>> sessionsByDate = query.selectFrom(SESSIONS)
                                                                                 .orderBy(SESSIONS.START.asc(),
                                                                                          SESSIONS.END.asc())
                                                                                 .fetch()
                                                                                 .intoGroups(
                                                                                         session -> TimeUtil.getDateTime(
                                                                                                                    session.getStart())
                                                                                                            .toLocalDate());
                    ;
                    for(Map.Entry<LocalDate, Result<SessionsRecord>> entry : sessionsByDate.entrySet())
                    {
                        if(entry.getValue().size() == 1)
                        {
                            total += entry.getValue().get(0).getScheduledhours();
                        }
                        else if(entry.getValue().size() > 1)
                        {
                            LocalDateTime intervalStart = TimeUtil.getDateTime(
                                    entry.getValue().getValue(0, SESSIONS.START));
                            LocalDateTime intervalEnd = TimeUtil.getDateTime(
                                    entry.getValue().getValue(0, SESSIONS.END));
                            for(SessionsRecord session : entry.getValue())
                            {
                                LocalDateTime sessionStart = TimeUtil.getDateTime(session.getStart());
                                LocalDateTime sessionEnd = TimeUtil.getDateTime(session.getEnd());
                                if(sessionStart.isBefore(intervalEnd))
                                {
                                    if(sessionEnd.isAfter(intervalEnd))
                                    {
                                        intervalEnd = TimeUtil.getDateTime(session.getEnd());
                                    }
                                }
                                else
                                {
                                    total += scheduleStore.getScheduleOverlap(intervalStart, intervalEnd);
                                    intervalStart = sessionStart;
                                    intervalEnd = sessionEnd;
                                }
                            }
                            total += scheduleStore.getScheduleOverlap(intervalStart, intervalEnd);
                        }
                    }
                    return total;
                });
                scheduledHoursFresh = true;
            }
        }
        return scheduledHours;
    }
    
    public void createPreviousSession(Member createdBy, long start, long end)
    {
        if(createdBy.getRole().equals(Role.ADMIN))
        {
            DatabaseConnector.runQuery(query -> {
                String sessionId = UUID.randomUUID().toString();
                sessionId = "0000" + sessionId.substring(4);
                query.insertInto(SESSIONS)
                     .values(sessionId,
                             scheduleStore.getScheduleOverlap(TimeUtil.getDateTime(start), TimeUtil.getDateTime(end)),
                             createdBy.getId().toString(),
                             createdBy.getId().toString(),
                             start,
                             end)
                     .execute();
                
                return null;
            });
            scheduledHoursFresh = false;
        }
    }
}
