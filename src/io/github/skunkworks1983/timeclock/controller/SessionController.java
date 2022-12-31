package io.github.skunkworks1983.timeclock.controller;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.DatabaseConnector;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.MemberStore;
import io.github.skunkworks1983.timeclock.db.SessionStore;
import io.github.skunkworks1983.timeclock.db.TimeUtil;
import io.github.skunkworks1983.timeclock.ui.AlertMessage;

import javax.annotation.Nullable;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SessionController
{
    private SessionStore sessionStore;
    private MemberStore memberStore;
    private AWSCredentials awsCredentials;
    
    @Inject
    public SessionController(SessionStore sessionStore, MemberStore memberStore,
                             @Nullable AWSCredentials awsCredentials)
    {
        this.sessionStore = sessionStore;
        this.memberStore = memberStore;
        this.awsCredentials = awsCredentials;
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
        for(Member memberToSignOut : memberStore.getMembers())
        {
            if(memberToSignOut.isSignedIn())
            {
                membersSignedOut++;
                memberStore.signOut(memberToSignOut, true);
            }
        }
        
        sessionStore.endSession(endedBy);
        
        // back up database
        SwingUtilities.invokeLater(() -> {
            String currentDbFile = DatabaseConnector.getDatabaseFile();
            String backupDbFile = String.format("%s-%d", currentDbFile, TimeUtil.getCurrentTimestamp());
            try
            {
                Files.copy(Path.of(currentDbFile), Path.of(backupDbFile));
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return;
            }
            
            if(awsCredentials != null)
            {
                AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                                                         .withCredentials(credentialsProvider)
                                                         .withRegion("us-west-2")
                                                         .build();
                
                TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
                
                transferManager.upload("skunklogindbbackupbucket", backupDbFile, new File(backupDbFile));
            }
        });
        
        
        return new AlertMessage(true, String.format("Session ended by %s %s at %s; %d member(s) force signed out.",
                                                    endedBy.getFirstName(), endedBy.getLastName(),
                                                    TimeUtil.formatTime(TimeUtil.getCurrentTimestamp()),
                                                    membersSignedOut));
    }
}
