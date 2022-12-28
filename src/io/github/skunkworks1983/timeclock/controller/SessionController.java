package io.github.skunkworks1983.timeclock.controller;

import io.github.skunkworks1983.timeclock.db.DatabaseConnector;
import io.github.skunkworks1983.timeclock.db.generated.tables.Sessions;

public class SessionController
{
    public double calculateScheduledHours()
    {
        return DatabaseConnector.runQuery(query ->
                                              {
                                                  double total = 0;
                                                  for(float hours: query.selectFrom(Sessions.SESSIONS).fetch(Sessions.SESSIONS.SCHEDULEDHOURS))
                                                  {
                                                      total += hours;
                                                  }
                                                  return total;
                                              });
    }
}
