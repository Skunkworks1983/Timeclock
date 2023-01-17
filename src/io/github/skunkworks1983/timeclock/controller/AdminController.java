package io.github.skunkworks1983.timeclock.controller;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.*;
import io.github.skunkworks1983.timeclock.ui.AlertMessage;
import io.github.skunkworks1983.timeclock.ui.TextToSpeechHandler;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminController
{
    private static final String ADMIN_PASS_HASH = "90f745dc7b86d382ad653c96008ebff2466fc9262bcd9a3e22bad43f083da37c";
    private static final byte[] ADMIN_PASS_SALT = {-49, -73, 42, -123};
    
    private final MemberStore memberStore;
    private final SigninStore signinStore;
    private final PinStore pinStore;
    private final TextToSpeechHandler tts;
    
    @Inject
    public AdminController(MemberStore memberStore, SigninStore signinStore, PinStore pinStore, TextToSpeechHandler tts)
    {
        this.memberStore = memberStore;
        this.signinStore = signinStore;
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

    private Member getMemberById(List<Member> members, UUID id)
    {
        for(Member member : members)
        {
            if(member.getId().equals(id))
            {
                return member;
            }
        }

        // This should not happen!!
        return null;
    }

    public AlertMessage rebuildHours()
    {
        // Get a list of signins from the database
        List<Signin> signins = signinStore.getSignins();

        // Get a list of members from the database
        List<Member> members = memberStore.getMembers();

        // Save list of old members to provide an hour diff
        List<Member> membersOldHours = new ArrayList<>(members);

        // Zero out the hours, since we are rebuilding from the signins table
        for (Member member : members)
        {
            member.setHours(0);
        }

        // Iterate through signins
        for(Signin signin : signins)
        {
            // If signing in, set member.lastSignedIn
            if(signin.getIsSigningIn() == 1)
            {
                getMemberById(members, signin.getId()).setLastSignIn(signin.getTime());
            }
            // If signing out, calculate time delta and add to hours
            else
            {
                Member member = getMemberById(members, signin.getId());
                // Calculate the delta
                double delta = TimeUtil.convertSecToHour(signin.getTime() - member.getLastSignIn());

                // If it was a force signout, then max the hours to 1
                if(signin.getIsForce() == 1)
                {
                    delta = Math.max(delta, 1.0);
                }

                // Update the member's hours
                member.setHours(member.getHours() + delta);
            }
        }

        // Write new members to Members table (skip for now)
        for(Member member : members)
        {
            memberStore.writeMemberHours(member);
        }

        StringBuilder alertMsg = new StringBuilder("Rebuilt members table. Data:\n");

        // Display debug alert
        for(Member member : members)
        {
            Member oldMember = getMemberById(membersOldHours, member.getId());
            if(member.getHours() != 0)
                alertMsg.append(String.format("\t%s %s: %.2f->%.2f\n", member.getFirstName(), member.getLastName(), oldMember.getHours(), member.getHours()));
        }

        return new AlertMessage(true, alertMsg.toString());
    }
}
