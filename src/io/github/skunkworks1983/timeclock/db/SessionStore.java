package io.github.skunkworks1983.timeclock.db;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.generated.tables.Sessions;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

public class SessionStore
{
    private ScheduleStore scheduleStore;
    
    @Inject
    public SessionStore(ScheduleStore scheduleStore)
    {
        this.scheduleStore = scheduleStore;
    }
    
    public void startSession(Member startedBy, boolean delayUntilScheduled)
    {
        if(startedBy.getRole().equals(Role.ADMIN) && !isSessionActive())
        {
            try(Connection connection = DatabaseConnector.createConnection())
            {
                DSLContext query = DSL.using(connection, SQLDialect.SQLITE);
                query.insertInto(Sessions.SESSIONS)
                     .values(0, startedBy.getId().toString(), null, TimeUtil.getCurrentTimestamp(), 0)
                     .execute();
            }
            catch(SQLException throwables)
            {
                throwables.printStackTrace();
            }
        }
    }
    
    public void endSession(Member endedBy)
    {
        if(endedBy.getRole().equals(Role.ADMIN) && isSessionActive())
        {
            try(Connection connection = DatabaseConnector.createConnection())
            {
                DSLContext query = DSL.using(connection, SQLDialect.SQLITE);
                long sessionEnd = TimeUtil.getCurrentTimestamp();
                long sessionStart = query.select(Sessions.SESSIONS.START)
                                         .from(Sessions.SESSIONS)
                                         .where(Sessions.SESSIONS.END.eq(0L))
                                         .fetchAny()
                                         .get(Sessions.SESSIONS.START);
                double scheduledHours = scheduleStore.getScheduleOverlap(TimeUtil.getDateTime(sessionStart), TimeUtil.getDateTime(sessionEnd));
                
                query.update(Sessions.SESSIONS)
                     .set(Sessions.SESSIONS.END, sessionEnd)
                     .set(Sessions.SESSIONS.SCHEDULEDHOURS, (float)scheduledHours)
                     .set(Sessions.SESSIONS.ENDEDBY, endedBy.getId().toString())
                     .where(Sessions.SESSIONS.END.eq(0L))
                     .execute();
            }
            catch(SQLException throwables)
            {
                throwables.printStackTrace();
            }
        }
    }
    
    public boolean isSessionActive()
    {
        Boolean result = DatabaseConnector.<Boolean>runQuery((query) ->
                                                             {
                                                                 long openSessions = query.select()
                                                                                          .from(Sessions.SESSIONS)
                                                                                          .where(Sessions.SESSIONS.END.eq(0L))
                                                                                          .fetch()
                                                                                          .stream()
                                                                                          .count();
                                                                 return openSessions > 0;
                                                             });
        return result != null && result;
    }
}
