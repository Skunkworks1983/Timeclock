package io.github.skunkworks1983.timeclock.db;

import io.github.skunkworks1983.timeclock.db.generated.tables.records.SigninsRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.skunkworks1983.timeclock.db.generated.tables.Members.MEMBERS;
import static io.github.skunkworks1983.timeclock.db.generated.tables.Signins.SIGNINS;

public class MemberStore
{
    public MemberStore()
    {
    }
    
    public List<Member> getMembers()
    {
        List<Member> memberList = DatabaseConnector
                .runQuery(query -> {
                    List<Member> members = query.selectFrom(MEMBERS)
                                                .orderBy(MEMBERS.LASTNAME.asc(),
                                                         MEMBERS.FIRSTNAME.asc())
                                                .fetch()
                                                .into(Member.class);
                    return members;
                });
        
        return memberList;
    }
    
    public void signIn(Member member)
    {
        signIn(member, TimeUtil.getCurrentTimestamp());
    }
    
    public void signIn(Member member, long signInTime)
    {
        if(!member.isSignedIn())
        {
            member.setLastSignIn(signInTime);
            member.setSignedIn(true);
            
            DatabaseConnector.runQuery(query -> {
                query.update(MEMBERS)
                     .set(MEMBERS.LASTSIGNEDIN, member.getLastSignIn())
                     .set(MEMBERS.ISSIGNEDIN, 1)
                     .where(MEMBERS.ID.eq(member.getId().toString()))
                     .execute();
                
                return null;
            });
            
            DatabaseConnector.runQuery(query -> {
                query.insertInto(SIGNINS)
                     .set(new SigninsRecord(member.getId().toString(), member.getLastSignIn(), 1, 0))
                     .execute();
                return null;
            });
        }
    }
    
    public void signOut(Member member)
    {
        signOut(member, false);
    }
    
    public void signOut(Member member, boolean force)
    {
        if(member.isSignedIn())
        {
            long memberTimeSec = TimeUtil.convertHourToSec(member.getHours());
            long currentTimestamp = TimeUtil.getCurrentTimestamp();
            if(force)
            {
                memberTimeSec += Math.min(currentTimestamp - member.getLastSignIn(),
                                          TimeUnit.HOURS.toSeconds(1));
            }
            else
            {
                memberTimeSec += currentTimestamp - member.getLastSignIn();
            }
            
            memberTimeSec = Math.max(memberTimeSec, 0);
            member.setHours(TimeUtil.convertSecToHour(memberTimeSec));
            member.setSignedIn(false);
            
            DatabaseConnector.runQuery(query -> {
                query.update(MEMBERS)
                     .set(MEMBERS.HOURS, (float) member.getHours())
                     .set(MEMBERS.ISSIGNEDIN, 0)
                     .where(MEMBERS.ID.eq(member.getId().toString()))
                     .execute();
                
                return null;
            });
            DatabaseConnector.runQuery(query -> {
                query.insertInto(SIGNINS)
                     .set(new SigninsRecord(member.getId().toString(), currentTimestamp, 0, force ? 1 : 0))
                     .execute();
                return null;
            });
        }
    }
    
    public void addPreviousSignIn(Member member, long signInTime, long signOutTime)
    {
        long memberTimeSec = TimeUtil.convertHourToSec(member.getHours());
        memberTimeSec += signOutTime - signInTime;
        memberTimeSec = Math.max(memberTimeSec, 0);
        member.setHours(TimeUtil.convertSecToHour(memberTimeSec));
        
        DatabaseConnector.runQuery(query -> {
            query.update(MEMBERS)
                 .set(MEMBERS.HOURS, (float) member.getHours())
                 .where(MEMBERS.ID.eq(member.getId().toString()))
                 .execute();
            
            return null;
        });
        
        DatabaseConnector.runQuery(query -> {
            query.insertInto(SIGNINS)
                 .set(new SigninsRecord(member.getId().toString(), signInTime, 1, 0))
                 .newRecord()
                 .set(new SigninsRecord(member.getId().toString(), signOutTime, 0, 0))
                 .execute();
            
            return null;
        });
    }
    
    public boolean createMember(Member member)
    {
        return DatabaseConnector.runQuery(query -> query.executeInsert(query.newRecord(MEMBERS, member))) > 0;
    }
    
    // Invoked by the RebuildController. Will overwrite the member's hours in the database.
    public void writeMemberHours(Member member)
    {
        DatabaseConnector.runQuery(query -> {
            query.update(MEMBERS)
                 .set(MEMBERS.HOURS, (float) member.getHours())
                 .where(MEMBERS.ID.eq(member.getId().toString()))
                 .execute();
            
            return null;
        });
    }
    
    public void applyPenalty(Member member)
    {
        DatabaseConnector.runQuery(query -> {
            query.update(MEMBERS)
                 .set(MEMBERS.PENALTIES, member.getPenalties() + 1)
                 .where(MEMBERS.ID.eq(member.getId().toString()))
                 .execute();
            return null;
        });
        member.setPenalties(member.getPenalties() + 1);
    }
}
