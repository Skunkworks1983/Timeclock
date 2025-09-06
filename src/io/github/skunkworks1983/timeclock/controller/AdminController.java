package io.github.skunkworks1983.timeclock.controller;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.BackupHandler;
import io.github.skunkworks1983.timeclock.db.DatabaseConnector;
import io.github.skunkworks1983.timeclock.db.DaySchedule;
import io.github.skunkworks1983.timeclock.db.HashUtil;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.MemberStore;
import io.github.skunkworks1983.timeclock.db.PinStore;
import io.github.skunkworks1983.timeclock.db.Role;
import io.github.skunkworks1983.timeclock.db.ScheduleStore;
import io.github.skunkworks1983.timeclock.db.SessionStore;
import io.github.skunkworks1983.timeclock.db.Signin;
import io.github.skunkworks1983.timeclock.db.SigninStore;
import io.github.skunkworks1983.timeclock.db.TimeUtil;
import io.github.skunkworks1983.timeclock.ui.AlertMessage;
import io.github.skunkworks1983.timeclock.ui.TextToSpeechHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminController
{
    private static final String ADMIN_PASS_HASH = "90f745dc7b86d382ad653c96008ebff2466fc9262bcd9a3e22bad43f083da37c";
    private static final byte[] ADMIN_PASS_SALT = {-49, -73, 42, -123};
    
    private final MemberStore memberStore;
    private final SigninStore signinStore;
    private final PinStore pinStore;
    private final SessionStore sessionStore;
    private final ScheduleStore scheduleStore;
    private final TextToSpeechHandler tts;
    private final BackupHandler backupHandler;
    
    @Inject
    public AdminController(MemberStore memberStore, SigninStore signinStore, PinStore pinStore,
                           SessionStore sessionStore,
                           ScheduleStore scheduleStore, TextToSpeechHandler tts,
                           BackupHandler backupHandler)
    {
        this.memberStore = memberStore;
        this.signinStore = signinStore;
        this.pinStore = pinStore;
        this.sessionStore = sessionStore;
        this.scheduleStore = scheduleStore;
        this.tts = tts;
        this.backupHandler = backupHandler;
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
                                                    member.getFirstName(), member.getLastName(),
                                                    member.getPenalties()));
    }
    
    public AlertMessage rebuildHours()
    {
        return backupHandler.rebuildHours();
    }
    
    public AlertMessage createGroupSignIn(List<Member> members, Member admin, OffsetDateTime start, OffsetDateTime end)
    {
        if(!admin.getRole().equals(Role.ADMIN))
        {
            return new AlertMessage(false, String.format("%s %s is not an admin.", admin.getFirstName(), admin.getLastName()));
        }
        
        List<Member> alreadySignedInMembers = new LinkedList<>();
        long startSecond = start.toEpochSecond();
        long endSecond = end.toEpochSecond();
        for(Member m: members)
        {
            if(signinStore.hasOverlappingSignIn(m, startSecond)
            || signinStore.hasOverlappingSignIn(m, endSecond))
            {
                alreadySignedInMembers.add(m);
            }
        }
    
        String currentDb = DatabaseConnector.getDatabaseFile();
        String copyDb = currentDb + "-merge";
        if(!alreadySignedInMembers.isEmpty())
        {
            try
            {
                Files.deleteIfExists(Path.of(copyDb));
                Files.copy(Path.of(currentDb), Path.of(copyDb));
            }
            catch(IOException e)
            {
                return new AlertMessage(false, String.format("%d members have conflicting sign-ins with the given time period and merging failed.", alreadySignedInMembers.size()));
            }
            DatabaseConnector.setDatabaseFile(copyDb);
        }
    
        sessionStore.createPreviousSession(admin, startSecond, endSecond);
        for(Member m: members)
        {
            memberStore.addPreviousSignIn(m, startSecond, endSecond);
        }
    
        if(!alreadySignedInMembers.isEmpty())
        {
            DatabaseConnector.setDatabaseFile(currentDb);
            backupHandler.mergeOtherDatabase(copyDb);
            try
            {
                Files.deleteIfExists(Path.of(copyDb));
            }
            catch(IOException e)
            {
                // don't care
            }
        }
        
        tts.speak("Group retroactively signed in");
        return new AlertMessage(true, String.format("%d members signed in from %s to %s by %s %s.",
                                                    members.size(),
                                                    TimeUtil.formatTime(startSecond),
                                                    TimeUtil.formatTime(endSecond),
                                                    admin.getFirstName(),
                                                    admin.getLastName()));
    }
    
    public AlertMessage fixAdminForgotSignOut()
    {
        List<Member> usersToFix = memberStore.getMembers()
                                         .stream()
                                         .filter(member -> member.isSignedIn())
                                         .collect(Collectors.toList());
        List<Member> adminsToFix = usersToFix.stream()
                                             .filter(member -> member.getRole() == Role.ADMIN)
                                             .collect(Collectors.toList());
        if(adminsToFix.size() == 0)
        {
            return new AlertMessage(false, "No admins forgot to sign out");
        }
        
        long startTime = adminsToFix.stream()
                                    .mapToLong(Member::getLastSignIn)
                                    .min()
                                    .orElse(-1);
        DaySchedule schedule;
        if(startTime != -1)
        {
            schedule = scheduleStore.getSchedule(startTime);
        }
        else
        {
            return new AlertMessage(false, "Failed to find admin's last sign in time");
        }
        long endTime = Math.min(TimeUtil.getEpochSeconds(TimeUtil.getDateTime(startTime).with(schedule.getEnd())),
                                TimeUtil.getCurrentTimestamp());
        
        for(Member member: usersToFix)
        {
            memberStore.signOut(member, member.getRole() != Role.ADMIN, endTime);
        }
        sessionStore.endSession(adminsToFix.get(0));
        
        return new AlertMessage(true, String.format("Fixed session not ended by %s %s. New session end is %s; %d members signed out.",
                                                    adminsToFix.get(0).getFirstName(),
                                                    adminsToFix.get(0).getLastName(),
                                                    TimeUtil.formatTime(endTime),
                                                    usersToFix.size()));
    }
    
    public AlertMessage forceSync()
    {
        if(!sessionStore.isSessionActive())
        {
            return backupHandler.doBackup();
        }
        else
        {
            return new AlertMessage(false, "Can't force sync while session is active");
        }
    }
}
