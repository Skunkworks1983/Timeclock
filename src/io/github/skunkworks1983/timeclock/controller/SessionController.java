package io.github.skunkworks1983.timeclock.controller;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.MemberStore;
import io.github.skunkworks1983.timeclock.db.SessionStore;
import io.github.skunkworks1983.timeclock.db.TimeUtil;
import io.github.skunkworks1983.timeclock.ui.AlertMessage;

public class SessionController
{
    private SessionStore sessionStore;
    private MemberStore memberStore;
    
    @Inject
    public SessionController(SessionStore sessionStore, MemberStore memberStore)
    {
        this.sessionStore = sessionStore;
        this.memberStore = memberStore;
    }
    
    public double calculateScheduledHours()
    {
        return sessionStore.calculateScheduledHours();
    }
    
    public AlertMessage startSession(Member startedBy, boolean delayUntilScheduled)
    {
        long sessionStart = sessionStore.startSession(startedBy, delayUntilScheduled);
        if(sessionStart != -1)
        {
            if(delayUntilScheduled)
            {
                memberStore.signIn(startedBy, sessionStart);
            }
            return new AlertMessage(true, String.format("Session started by %s %s at %s.",
                                                        startedBy.getFirstName(), startedBy.getLastName(),
                                                        TimeUtil.formatTime(sessionStart)));
        }
        else
        {
            return new AlertMessage(false, "Failed to start session.");
        }
    }
    
    public AlertMessage endSession(Member endedBy)
    {
        int membersSignedOut = 0;
        for(Member memberToSignOut: memberStore.getMembers())
        {
            if(memberToSignOut.isSignedIn())
            {
                membersSignedOut++;
                memberStore.signOut(memberToSignOut, true);
            }
        }
    
        sessionStore.endSession(endedBy);
        return new AlertMessage(true, String.format("Session ended by %s %s at %s; %d member(s) force signed out.",
                                                    endedBy.getFirstName(), endedBy.getLastName(),
                                                    TimeUtil.formatTime(TimeUtil.getCurrentTimestamp()),
                                                    membersSignedOut));
    }
}
