package io.github.skunkworks1983.timeclock.controller;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.HashUtil;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.MemberStore;
import io.github.skunkworks1983.timeclock.db.PinStore;
import io.github.skunkworks1983.timeclock.db.Role;
import io.github.skunkworks1983.timeclock.ui.AlertMessage;
import io.github.skunkworks1983.timeclock.ui.TextToSpeechHandler;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class AdminController
{
    private static final String ADMIN_PASS_HASH = "90f745dc7b86d382ad653c96008ebff2466fc9262bcd9a3e22bad43f083da37c";
    private static final byte[] ADMIN_PASS_SALT = {-49, -73, 42, -123};
    
    private final MemberStore memberStore;
    private final PinStore pinStore;
    private final TextToSpeechHandler tts;
    
    @Inject
    public AdminController(MemberStore memberStore, PinStore pinStore, TextToSpeechHandler tts)
    {
        this.memberStore = memberStore;
        this.pinStore = pinStore;
        this.tts = tts;
    }
    
    public AlertMessage authenticateAdminWindow(char[] enteredPass)
    {
        try
        {
            if(HashUtil.computeHash(ADMIN_PASS_SALT, enteredPass).equals(ADMIN_PASS_HASH))
            {
                tts.speak("Logged into admin panel");
                return new AlertMessage(true, null, null);
            }
            else
            {
                return new AlertMessage(false, "Wrong admin password.", null);
            }
        }
        catch(NoSuchAlgorithmException e)
        {
            return new AlertMessage(false, "Hash computation failed.", null);
        }
    }
    
    public AlertMessage createMember(Role role, String firstName, String lastName)
    {
        if(memberStore.createMember(new Member(UUID.randomUUID(), role, firstName, lastName, 0, 0, false, 0)))
        {
            return new AlertMessage(true, String.format("%s %s %s created.", role, firstName, lastName));
        }
        else
        {
            return new AlertMessage(false, String.format("Failed to create %s %s %s.", role, firstName, lastName));
        }
    }
    
    public AlertMessage forceSignOut(Member member)
    {
        memberStore.signOut(member, true);
        String message = String.format("Force signed out %s %s.", member.getFirstName(), member.getLastName());
        tts.speak(message);
        return new AlertMessage(true, message);
    }
    
    public AlertMessage resetPin(Member member)
    {
        pinStore.deletePin(member.getId());
        String message = String.format("Cleared PIN for %s %s.", member.getFirstName(), member.getLastName());
        tts.speak(message);
        return new AlertMessage(true, message);
    }
    
    public AlertMessage applyPenalty(Member member)
    {
        memberStore.applyPenalty(member);
        tts.speak(String.format("Applied penalty to %s %s", member.getFirstName(), member.getLastName()));
        return new AlertMessage(true, String.format("Applied penalty to %s %s. New penalty count: %d.",
                                                    member.getFirstName(), member.getLastName(), member.getPenalties()));
    }
}
