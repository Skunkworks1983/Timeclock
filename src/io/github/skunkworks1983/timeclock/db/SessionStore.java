package io.github.skunkworks1983.timeclock.db;

import io.github.skunkworks1983.timeclock.db.generated.tables.Sessions;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

public class SessionStore
{
    public void startSession(Member member, boolean delayUntilScheduled)
    {
        if(member.getRole().equals(Role.ADMIN) && !isSessionActive())
        {
            try(Connection connection = DatabaseConnector.createConnection())
            {
                DSLContext query = DSL.using(connection, SQLDialect.SQLITE);
                query.insertInto(Sessions.SESSIONS)
                     .values(TimeUtil.getCurrentTimestamp(), 0, 0, member.getId().toString(), null)
                     .execute();
            }
            catch(SQLException throwables)
            {
                throwables.printStackTrace();
            }
        }
    }
    
    public void endSession(Member member)
    {
        if(member.getRole().equals(Role.ADMIN) && isSessionActive())
        {
            try(Connection connection = DatabaseConnector.createConnection())
            {
                DSLContext query = DSL.using(connection, SQLDialect.SQLITE);
                query.update(Sessions.SESSIONS)
                     .set(Sessions.SESSIONS.END, TimeUtil.getCurrentTimestamp())
                     .set(Sessions.SESSIONS.SCHEDULEDHOURS, 0.f)
                     .set(Sessions.SESSIONS.ENDEDBY, member.getId().toString())
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
