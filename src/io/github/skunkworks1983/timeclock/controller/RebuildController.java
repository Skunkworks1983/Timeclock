package io.github.skunkworks1983.timeclock.controller;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.*;
import io.github.skunkworks1983.timeclock.ui.AlertMessage;

import java.util.List;

public class RebuildController
{
    private final SigninStore signinStore;
    private final MemberStore memberStore;

    @Inject
    public RebuildController(SigninStore signinStore, MemberStore memberStore)
    {
        this.signinStore = signinStore;
        this.memberStore = memberStore;
    }

    public AlertMessage handleRebuild()
    {
        // Get a list of signins from the database
        List<Signin> signins = signinStore.getSignins();

        // Get a list of members from the database
        List<Member> members = memberStore.getMembers();

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
                for(Member member : members)
                {
                    if(member.getId().equals(signin.getId()))
                    {
                        member.setLastSignIn(signin.getTime());
                    }
                }
            }
            // If signing out, calculate time delta and add to hours
            else
            {
                for(Member member : members)
                {
                    if(member.getId().equals(signin.getId()))
                    {
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
            alertMsg.append(String.format("\t%s: %f\n", member.getFirstName(), member.getHours()));
        }

        return new AlertMessage(true, alertMsg.toString());
    }
}
