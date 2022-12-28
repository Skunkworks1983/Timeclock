package io.github.skunkworks1983.timeclock.controller;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.MemberStore;
import io.github.skunkworks1983.timeclock.db.PinStore;
import io.github.skunkworks1983.timeclock.db.Role;
import io.github.skunkworks1983.timeclock.db.SessionStore;
import io.github.skunkworks1983.timeclock.db.TimeUtil;
import io.github.skunkworks1983.timeclock.ui.AlertMessage;

public class SignInController
{
    private PinStore pinStore;
    private MemberStore memberStore;
    private SessionStore sessionStore;
    
    @Inject
    public SignInController(PinStore pinStore, MemberStore memberStore, SessionStore sessionStore)
    {
        this.pinStore = pinStore;
        this.memberStore = memberStore;
        this.sessionStore = sessionStore;
    }
    
    public AlertMessage handleSignIn(Member member, boolean signingIn, char[] pin)
    {
        boolean isAdmin = member.getRole().equals(Role.ADMIN);
        if(pinStore.doesPinExist(member.getId()))
        {
            boolean isSessionActive = sessionStore.isSessionActive();
            if(!isAdmin)
            {
                if(pinStore.checkPin(member.getId(), pin))
                {
                    if(isSessionActive)
                    {
                        if(signingIn)
                        {
                            memberStore.signIn(member);
                        }
                        else
                        {
                            memberStore.signOut(member);
                        }
                        return new AlertMessage(true, null);
                    }
                    else
                    {
                        return new AlertMessage(false, "You can only sign in or out during meetings.");
                    }
                }
                else
                {
                    return new AlertMessage(false, String.format("Wrong PIN entered for %s %s.", member.getFirstName(),
                                                                 member.getLastName()));
                }
            }
            else
            {
                if(pinStore.checkPin(member.getId(), pin))
                {
                    if(signingIn)
                    {
                        memberStore.signIn(member);
                        if(!isSessionActive)
                        {
                            sessionStore.startSession(member, false);
                            return new AlertMessage(true, String.format("Session started by %s %s at %s.",
                                                                        member.getFirstName(), member.getLastName(),
                                                                        TimeUtil.formatTime(
                                                                                TimeUtil.getCurrentTimestamp())));
                        }
                        return new AlertMessage(true, null);
                    }
                    else
                    {
                        memberStore.signOut(member);
                        boolean isAnyAdminSignedIn = memberStore.getMembers()
                                                                .stream()
                                                                .anyMatch(m -> m.getRole()
                                                                                .equals(Role.ADMIN) && m.isSignedIn());
                        if(!isAnyAdminSignedIn)
                        {
                            memberStore.getMembers().stream().forEach(m -> memberStore.signOut(m, true));
                            sessionStore.endSession(member);
                            return new AlertMessage(true, String.format("Session ended by %s %s at %s.",
                                                                        member.getFirstName(), member.getLastName(),
                                                                        TimeUtil.formatTime(
                                                                                TimeUtil.getCurrentTimestamp())));
                        }
                        return new AlertMessage(true, null);
                    }
                }
                else
                {
                    return new AlertMessage(false, String.format("Wrong PIN entered for %s %s.", member.getFirstName(),
                                                                 member.getLastName()));
                }
            }

        }

        return new AlertMessage(false, String.format("No PIN set for %s %s. Please set your PIN before attempting to sign in.", member.getFirstName(), member.getLastName()));
    }
    
    public boolean shouldCreatePin(Member member)
    {
        return /*!member.getRole().equals(Role.ADMIN) &&*/ !pinStore.doesPinExist(member.getId());
    }
    
    public AlertMessage createPin(Member member, char[] pin)
    {
        pinStore.createPin(member.getId(), pin);
        return new AlertMessage(true, String.format("PIN set for %s %s. Please memorize your PIN and don't share it with anyone. If you forget your PIN, a coach can reset it, but it can't be retrieved for you.", member.getFirstName(), member.getLastName()));
    }
}
