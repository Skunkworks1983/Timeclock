package io.github.skunkworks1983.timeclock.db;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.generated.tables.Members;
import io.github.skunkworks1983.timeclock.db.generated.tables.Membertransactions;
import io.github.skunkworks1983.timeclock.db.generated.tables.Pins;
import io.github.skunkworks1983.timeclock.db.generated.tables.Sessions;
import io.github.skunkworks1983.timeclock.db.generated.tables.Signins;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.MembersRecord;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.MembertransactionsRecord;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.PinsRecord;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.SessionsRecord;
import io.github.skunkworks1983.timeclock.db.generated.tables.records.SigninsRecord;
import io.github.skunkworks1983.timeclock.ui.AlertMessage;
import org.jooq.Field;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BackupHandler
{
    private static final String BUCKET = "skunklogindbbackupbucket";
    
    private final AWSCredentials awsCredentials;
    private final SigninStore signinStore;
    private final MemberStore memberStore;
    
    @Inject
    public BackupHandler(SigninStore signinStore,
                         MemberStore memberStore,
                         @Nullable AWSCredentials awsCredentials)
    {
        this.signinStore = signinStore;
        this.memberStore = memberStore;
        this.awsCredentials = awsCredentials;
    }
    
    public AlertMessage doBackup()
    {
        String currentDbFile = DatabaseConnector.getDatabaseFile();
        
        if(awsCredentials != null)
        {
            AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                                                     .withCredentials(credentialsProvider)
                                                     .withRegion("us-west-2")
                                                     .build();
            
            ListObjectsRequest listRequest = new ListObjectsRequest();
            listRequest.setBucketName(BUCKET);
            listRequest.setPrefix(currentDbFile);
            ObjectListing objectListing = s3Client.listObjects(listRequest);
            List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
            while(objectListing.isTruncated())
            {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
                summaries.addAll(objectListing.getObjectSummaries());
            }
            
            TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
            
            boolean mergeDone = false;
            if(!summaries.isEmpty())
            {
                S3ObjectSummary mostRecentSummary = summaries.get(0);
                for(S3ObjectSummary summary : summaries)
                {
                    if(summary.getLastModified().after(mostRecentSummary.getLastModified()))
                    {
                        mostRecentSummary = summary;
                    }
                }
                
                String toMergeFileName = String.format("%s-merge", mostRecentSummary.getKey());
                Download toMergeDownload = transferManager.download(BUCKET, mostRecentSummary.getKey(),
                                                                    new File(toMergeFileName));
                try
                {
                    toMergeDownload.waitForCompletion();
                    mergeOtherDatabase(toMergeFileName);
                    mergeDone = true;
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                    return new AlertMessage(false, "Download/merge failed: " + e.getMessage());
                }
            }
            
            Path backupDbFile = writeLocalBackup(currentDbFile);
            
            if(backupDbFile != null)
            {
                Upload upload = transferManager.upload(BUCKET, backupDbFile.getFileName().toString(), backupDbFile.toFile());
                try
                {
                    upload.waitForUploadResult();
                    return new AlertMessage(true, "Cloud backup complete, merge " + (mergeDone ? "complete" : "incomplete"));
                }
                catch(Exception e)
                {
                    return new AlertMessage(false, String.format("Cloud backup upload failed: \"%s\" merge %s", e.getMessage(), (mergeDone ? "complete" : "incomplete")));
                }
            }
            else
            {
                return new AlertMessage(false, "Writing local backup failed, merge " + (mergeDone ? "complete" : "incomplete"));
            }
        }
        else
        {
            Path backupDbFile = writeLocalBackup(currentDbFile);
            if(backupDbFile != null)
            {
                return new AlertMessage(true, "Writing local backup complete, AWS creds missing");
            }
            else
            {
                return new AlertMessage(false, "Writing local backup failed, AWS creds missing");
            }
        }
    }
    
    public void mergeOtherDatabase(String toMergeFileName)
    {
        // member/PIN updates
        List<MembertransactionsRecord> unknownTransactions =
                DatabaseConnector.runAttachedQuery(context ->
                                                   {
                                                       String otherTableName = String.format("%s.%s",
                                                                                             DatabaseConnector.ATTACHED_DB_NAME,
                                                                                             Membertransactions.MEMBERTRANSACTIONS.getName());
                                                       Membertransactions currTable = Membertransactions.MEMBERTRANSACTIONS.as(
                                                               "curr");
                                                       Field<Long> otherTableTime = DSL.field(
                                                               DSL.unquotedName(DatabaseConnector.ATTACHED_DB_NAME,
                                                                                Membertransactions.MEMBERTRANSACTIONS.getName(),
                                                                                Membertransactions.MEMBERTRANSACTIONS.TIME.getName()),
                                                               Long.class);
                                                       Field<String> otherTableTablename = DSL.field(
                                                               DSL.unquotedName(DatabaseConnector.ATTACHED_DB_NAME,
                                                                                Membertransactions.MEMBERTRANSACTIONS.getName(),
                                                                                Membertransactions.MEMBERTRANSACTIONS.TABLENAME.getName()),
                                                               String.class);
                                                       Field<String> otherTableId = DSL.field(
                                                               DSL.unquotedName(DatabaseConnector.ATTACHED_DB_NAME,
                                                                                Membertransactions.MEMBERTRANSACTIONS.getName(),
                                                                                Membertransactions.MEMBERTRANSACTIONS.ID.getName()),
                                                               String.class);
                                                       return context.select(otherTableTime, otherTableTablename,
                                                                             otherTableId)
                                                                     .from(currTable.fullOuterJoin(otherTableName)
                                                                                    .on(currTable.TIME.eq(otherTableTime)
                                                                                                      .and(currTable.TABLENAME.eq(otherTableTablename))
                                                                                                      .and(currTable.ID.eq(otherTableId)))
                                                                                    .asTable())
                                                                     .where(currTable.ID.isNull())
                                                                     .fetchInto(MembertransactionsRecord.class);
                                                   },
                                                   DatabaseConnector.getDatabaseFile(),
                                                   toMergeFileName
                                                  );
        
        for(MembertransactionsRecord transaction : unknownTransactions)
        {
            Long mostRecentTime = DatabaseConnector.runQuery(
                    context -> context.select(Membertransactions.MEMBERTRANSACTIONS.TIME)
                                      .from(Membertransactions.MEMBERTRANSACTIONS)
                                      .where(Membertransactions.MEMBERTRANSACTIONS.ID.eq(transaction.getId())
                                                                                     .and(Membertransactions.MEMBERTRANSACTIONS.TABLENAME.eq(
                                                                                             transaction.getTablename())))
                                      .orderBy(Membertransactions.MEMBERTRANSACTIONS.TIME.desc())
                                      .fetchAny(Membertransactions.MEMBERTRANSACTIONS.TIME));
            if(mostRecentTime == null || mostRecentTime < transaction.getTime())
            {
                // take the current value from the other database (or delete if not present)
                if(transaction.getTablename().equals(Members.MEMBERS.getName()))
                {
                    MembersRecord updatedRecord = DatabaseConnector.runQuery(
                            context -> context.selectFrom(Members.MEMBERS)
                                              .where(Members.MEMBERS.ID.eq(transaction.getId()))
                                              .fetchAny(),
                            toMergeFileName);
                    if(updatedRecord != null)
                    {
                        updatedRecord.changed(true);
                        boolean memberExists = DatabaseConnector.runQuery(context -> context.selectCount().from(Members.MEMBERS).where(Members.MEMBERS.ID.eq(updatedRecord.getId())).fetchAny()).value1() > 0;
                        if(memberExists)
                        {
                            DatabaseConnector.runQuery(context -> context.update(Members.MEMBERS)
                                                                         .set(updatedRecord)
                                                                         .where(Members.MEMBERS.ID.eq(
                                                                                 transaction.getId()))
                                                                         .execute());
                        }
                        else
                        {
                            DatabaseConnector.runQuery(context -> context.insertInto(Members.MEMBERS)
                                                                         .set(updatedRecord)
                                                                         .execute());
                        }
                    }
                    else
                    {
                        DatabaseConnector.runQuery(context -> context.deleteFrom(Members.MEMBERS)
                                                                     .where(Members.MEMBERS.ID.eq(transaction.getId()))
                                                                     .execute());
                    }
                }
                else if(transaction.getTablename().equals(Pins.PINS.getName()))
                {
                    PinsRecord updatedRecord = DatabaseConnector.runQuery(context -> context.selectFrom(Pins.PINS)
                                                                                            .where(Pins.PINS.MEMBERID.eq(
                                                                                                    transaction.getId()))
                                                                                            .fetchAny(),
                                                                          toMergeFileName);
                    if(updatedRecord != null)
                    {
                        updatedRecord.changed(true);
                        boolean pinExists = DatabaseConnector.runQuery(context -> context.selectCount().from(Pins.PINS).where(Pins.PINS.MEMBERID.eq(updatedRecord.getMemberid())).fetchAny()).value1() > 0;
                        if(pinExists)
                        {
                            DatabaseConnector.runQuery(context -> context.update(Pins.PINS)
                                                                         .set(updatedRecord)
                                                                         .where(Pins.PINS.MEMBERID.eq(
                                                                                 transaction.getId()))
                                                                         .execute());
                        }
                        else
                        {
                            DatabaseConnector.runQuery(context -> context.insertInto(Pins.PINS)
                                                                         .set(updatedRecord)
                                                                         .execute());
                        }
                    }
                    else
                    {
                        DatabaseConnector.runQuery(context -> context.deleteFrom(Pins.PINS)
                                                                     .where(Pins.PINS.MEMBERID.eq(transaction.getId()))
                                                                     .execute());
                    }
                }
            }
            transaction.changed(true);
        }
        
        DatabaseConnector.runQuery(context -> context.batchInsert(unknownTransactions).execute());
        
        // sign-in/session updates
        Set<SessionsRecord> currentSessions = DatabaseConnector.runQuery((context ->
                new HashSet<>(context.selectFrom(Sessions.SESSIONS)
                                     .where(Sessions.SESSIONS.END.ge(
                                             Long.valueOf(0)))
                                     .fetch())));
        Set<SessionsRecord> otherSessions = DatabaseConnector.runQuery((context ->
                                                                               new HashSet<>(context.selectFrom(Sessions.SESSIONS)
                                                                                                    .where(Sessions.SESSIONS.END.ge(
                                                                                                            Long.valueOf(0)))
                                                                                                    .fetch())),
                                                                       toMergeFileName);
        
        Set<SessionsRecord> sessionsToMerge = new HashSet<>();
        for(SessionsRecord session : otherSessions)
        {
            if(!currentSessions.contains(session))
            {
                sessionsToMerge.add(session);
            }
        }
        
        for(SessionsRecord session : sessionsToMerge)
        {
            List<SigninsRecord> sessionSigninRecords = DatabaseConnector.runQuery(
                    context -> context.selectFrom(Signins.SIGNINS)
                                      .where(Signins.SIGNINS.SESSIONID.eq(session.getId()))
                                      .orderBy(Signins.SIGNINS.TIME.asc())
                                      .fetch(),
                    toMergeFileName);
            
            Map<String, SessionsRecord> intersectingSessions = DatabaseConnector.runQuery((context ->
                    context.selectFrom(Sessions.SESSIONS)
                           .where(Sessions.SESSIONS.START.le(session.getEnd()).and(Sessions.SESSIONS.START.ge(session.getStart())) // start is inside session
                                                         .or(Sessions.SESSIONS.END.le(session.getEnd()).and(Sessions.SESSIONS.END.ge(session.getStart()))) // end is inside session
                                                         .or(Sessions.SESSIONS.START.le(session.getStart()).and(Sessions.SESSIONS.END.ge(session.getEnd())))) // session is inside start and end
                           .fetch().intoMap(Sessions.SESSIONS.ID)));
            List<SigninsRecord> intersectingSigninRecords = DatabaseConnector.runQuery(
                    context -> context.selectFrom(Signins.SIGNINS)
                                      .where(Signins.SIGNINS.SESSIONID.in(intersectingSessions.keySet()))
                                      .orderBy(Signins.SIGNINS.TIME.asc())
                                      .fetch());
            
            Map<String, List<SigninsRecord>> memberSignins = new HashMap<>();
            List<SigninsRecord> revisedRecords = new ArrayList<>(
                    sessionSigninRecords.size() + intersectingSigninRecords.size());
            int curr = 0;
            int other = 0;
            while(curr < sessionSigninRecords.size() || other < intersectingSigninRecords.size())
            {
                // pick the next chronological sign-in record
                SigninsRecord nextRecord;
                if((other >= intersectingSigninRecords.size()) || (curr < sessionSigninRecords.size())
                        && sessionSigninRecords.get(curr).getTime() < intersectingSigninRecords.get(other).getTime())
                {
                    nextRecord = sessionSigninRecords.get(curr);
                    curr++;
                }
                else
                {
                    nextRecord = intersectingSigninRecords.get(other);
                    other++;
                }
                
                nextRecord.changed(true);
                
                if(!memberSignins.containsKey(nextRecord.getMemberid()))
                {
                    memberSignins.put(nextRecord.getMemberid(), new ArrayList<>());
                    memberSignins.get(nextRecord.getMemberid()).add(nextRecord);
                }
                else
                {
                    List<SigninsRecord> memberSigninList = memberSignins.get(nextRecord.getMemberid());
                    if(memberSigninList.size() == 1 && memberSigninList.get(0)
                                                                       .getSessionid()
                                                                       .equals(nextRecord.getSessionid()))
                    {
                        // we expect two consecutive records for the same session, so nothing to fix here
                        revisedRecords.add(memberSigninList.get(0));
                        revisedRecords.add(nextRecord);
                        memberSignins.remove(nextRecord.getMemberid());
                    }
                    else
                    {
                        memberSigninList.add(nextRecord);
                        if(memberSigninList.size() == 4) // don't worry about 3+ way merges
                        {
                            // determine if one session's sign-in period fully contains the other
                            if(memberSigninList.get(0).getSessionid().equals(memberSigninList.get(2).getSessionid()))
                            {
                                // AIn BIn AOut BOut; move AOut to before BIn
                                memberSigninList.get(2).setTime(memberSigninList.get(1).getTime() - 1);
                                memberSigninList.get(2).setIsforce(0);
                            }
                            else if(memberSigninList.get(0)
                                                    .getSessionid()
                                                    .equals(memberSigninList.get(3).getSessionid()))
                            {
                                // AIn BIn BOut AOut
                                if(memberSigninList.get(3).getIsforce() == 1)
                                {
                                    // AOut was forced, so move it to BIn and unforce since we know the user was there
                                    memberSigninList.get(3).setTime(memberSigninList.get(1).getTime() - 1);
                                    memberSigninList.get(3).setIsforce(0);
                                }
                                else
                                {
                                    // AOut was not forced and A contains B, so we only need to keep A
                                    memberSigninList.remove(1);
                                    memberSigninList.remove(1);
                                }
                            }
                            revisedRecords.addAll(memberSigninList);
                            memberSignins.remove(nextRecord.getMemberid());
                        }
                    }
                }
            }
            
            session.changed(true);
            
            DatabaseConnector.runQuery(context -> context.executeInsert(session));
            DatabaseConnector.runQuery(context -> context.deleteFrom(Signins.SIGNINS)
                                                         .where(Signins.SIGNINS.SESSIONID.in(
                                                                 intersectingSessions.keySet()))
                                                         .execute());
            DatabaseConnector.runQuery(context -> context.batchInsert(revisedRecords).execute());
        }
        
        rebuildHours();
    }
    
    public AlertMessage rebuildHours()
    {
        // Get a list of signins from the database
        List<Signin> signins = signinStore.getSignins();
        
        // Get a list of members from the database
        List<Member> members = memberStore.getMembers();
        
        Map<UUID, Double> uuidToOldHours = new HashMap<UUID, Double>();
        // Zero out the hours, since we are rebuilding from the signins table
        for(Member member : members)
        {
            uuidToOldHours.put(member.getId(), member.getHours());
            member.setHours(0);
        }
        
        // Iterate through signins
        for(Signin signin : signins)
        {
            // If signing in, set member.lastSignedIn
            if(signin.getIsSigningIn())
            {
                members.stream()
                       .filter(m -> m.getId().equals(signin.getMemberId()))
                       .findFirst()
                       .get()
                       .setLastSignIn(signin.getTime());
            }
            // If signing out, calculate time delta and add to hours
            else
            {
                Member member = members.stream()
                                       .filter(m -> m.getId().equals(signin.getMemberId()))
                                       .findFirst()
                                       .get();
                
                // Calculate the delta
                double delta = TimeUtil.convertSecToHour(signin.getTime() - member.getLastSignIn());
                
                // If it was a force signout, then cap the hours to 1
                if(signin.getIsForce())
                {
                    delta = Math.min(delta, 1.0);
                }
                
                // Update the member's hours
                member.setHours(member.getHours() + delta);
            }
        }
        
        // Write new members to Members table
        for(Member member : members)
        {
            memberStore.writeMemberHours(member);
        }
        
        StringBuilder alertMsg = new StringBuilder("Rebuilt members table. Data:\n");
        
        // Display debug alert
        for(Member member : members)
        {
            if(member.getHours() != 0 && !(uuidToOldHours.get(member.getId()) - 0.5 < member.getHours()
                    && member.getHours() < uuidToOldHours.get(member.getId()) + 0.5))
            {
                alertMsg.append(String.format("\t%s %s: %.2f->%.2f\n", member.getFirstName(), member.getLastName(),
                                              uuidToOldHours.get(member.getId()), member.getHours()));
            }
        }
        
        return new AlertMessage(true, alertMsg.toString());
    }
    
    private Path writeLocalBackup(String currentDbFile)
    {
        String backupDbFile = String.format("%s-%d", currentDbFile, TimeUtil.getCurrentTimestamp());
        try
        {
            return Files.copy(Path.of(currentDbFile), Path.of(backupDbFile));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
