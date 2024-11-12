package io.github.skunkworks1983.timeclock.controller;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.MemberStore;
import io.github.skunkworks1983.timeclock.db.PinStore;
import io.github.skunkworks1983.timeclock.db.Role;
import io.github.skunkworks1983.timeclock.db.SessionStore;
import io.github.skunkworks1983.timeclock.db.TimeUtil;
import io.github.skunkworks1983.timeclock.ui.AlertMessage;
import io.github.skunkworks1983.timeclock.ui.TextToSpeechHandler;

public class SignInController
{
    private final PinStore pinStore;
    private final MemberStore memberStore;
    private final SessionStore sessionStore;
    private final SessionController sessionController;
    private final TextToSpeechHandler tts;
    
    @Inject
    public SignInController(PinStore pinStore, MemberStore memberStore, SessionStore sessionStore,
                            SessionController sessionController, TextToSpeechHandler tts)
    {
        this.pinStore = pinStore;
        this.memberStore = memberStore;
        this.sessionStore = sessionStore;
        this.sessionController = sessionController;
        this.tts = tts;
    }
    
    public AlertMessage handleSignIn(Member member, boolean signingIn, char[] pin)
    {
        if(pinStore.doesPinExist(member.getId()))
        {
            boolean isSessionActive = sessionStore.isSessionActive();
            long queuedSessionStart = sessionStore.getQueuedSessionStart();
            if(member.getRole().equals(Role.STUDENT))
            {
                if(pinStore.checkPin(member.getId(), pin))
                {
                    if(isSessionActive)
                    {
                        if(signingIn)
                        {
                            memberStore.signIn(member);
                            speakSignIn(member, true);
                        }
                        else
                        {
                            memberStore.signOut(member);
                            speakSignIn(member, false);
                        }
                        return new AlertMessage(true, null);
                    }
                    else if(signingIn && queuedSessionStart > 0)
                    {
                        memberStore.signIn(member, queuedSessionStart);
                        speakSignIn(member, true);
                        return new AlertMessage(true, String.format("%s %s signed in for meeting starting at %s.",
                                                                    member.getFirstName(), member.getLastName(),
                                                                    TimeUtil.formatTime(queuedSessionStart)));
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
            else if(member.getRole().equals(Role.MENTOR))
            {
                if(pinStore.checkPin(member.getId(), pin))
                {
                    if(signingIn)
                    {
                        memberStore.signIn(member);
                        speakSignIn(member, true);
                    }
                    else
                    {
                        memberStore.signOut(member);
                        speakSignIn(member, false);
                    }
                    return new AlertMessage(true, null);
                }
                else
                {
                    return new AlertMessage(false, String.format("Wrong PIN entered for %s %s.", member.getFirstName(),
                                                                 member.getLastName()));
                }
            }
            else if(member.getRole().equals(Role.ADMIN))
            {
                if(pinStore.checkPin(member.getId(), pin))
                {
                    if(signingIn)
                    {
                        memberStore.signIn(member);
                        if(!(isSessionActive || queuedSessionStart > 0))
                        {
                            return sessionController.startSession(member, false);
                        }
                        speakSignIn(member, true);
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
                            return sessionController.endSession(member);
                        }
                        speakSignIn(member, false);
                        return new AlertMessage(true, null);
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
                return new AlertMessage(false, "Unknown role: " + member.getRole().name());
            }
            
        }
        
        return new AlertMessage(false,
                                String.format("No PIN set for %s %s. Please set your PIN before attempting to sign in.",
                                              member.getFirstName(), member.getLastName()));
    }
    
    public boolean shouldCreatePin(Member member)
    {
        return !member.getRole().equals(Role.ADMIN) && !pinStore.doesPinExist(member.getId());
    }
    
    public AlertMessage createPin(Member member, char[] pin)
    {
        pinStore.createPin(member.getId(), pin);
        tts.speak(String.format("PIN set for %s %s", member.getFirstName(), member.getLastName()));
        return new AlertMessage(true, String.format(
                "PIN set for %s %s. Please memorize your PIN and don't share it with anyone. If you forget your PIN, a coach can reset it, but it can't be retrieved for you.",
                member.getFirstName(), member.getLastName()));
    }
    
    private void speakSignIn(Member member, boolean signedIn)
    {
        tts.speak(String.format("%s %s signed %s", member.getFirstName(), member.getLastName(), signedIn ? "in" : "out"));
    }
}
