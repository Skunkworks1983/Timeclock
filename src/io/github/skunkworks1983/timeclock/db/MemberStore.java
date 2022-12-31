package io.github.skunkworks1983.timeclock.db;

import io.github.skunkworks1983.timeclock.db.generated.tables.Members;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MemberStore
{
    public MemberStore()
    {
    }
    
    public List<Member> getMembers()
    {
        List<Member> memberList = DatabaseConnector
                .runQuery(query -> {
                    List<Member> members = query.selectFrom(Members.MEMBERS)
                                                .orderBy(Members.MEMBERS.LASTNAME.asc(),
                                                         Members.MEMBERS.FIRSTNAME.asc())
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
                query.update(Members.MEMBERS)
                     .set(Members.MEMBERS.LASTSIGNEDIN, member.getLastSignIn())
                     .set(Members.MEMBERS.ISSIGNEDIN, 1)
                     .where(Members.MEMBERS.ID.eq(member.getId().toString()))
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
            if(force)
            {
                memberTimeSec += Math.min(TimeUtil.getCurrentTimestamp() - member.getLastSignIn(),
                                          TimeUnit.HOURS.toSeconds(1));
            }
            else
            {
                memberTimeSec += TimeUtil.getCurrentTimestamp() - member.getLastSignIn();
            }
            
            memberTimeSec = Math.max(memberTimeSec, 0);
            member.setHours(TimeUtil.convertSecToHour(memberTimeSec));
            member.setSignedIn(false);
            
            DatabaseConnector.runQuery(query -> {
                query.update(Members.MEMBERS)
                     .set(Members.MEMBERS.HOURS, (float) member.getHours())
                     .set(Members.MEMBERS.ISSIGNEDIN, 0)
                     .where(Members.MEMBERS.ID.eq(member.getId().toString()))
                     .execute();
                
                return null;
            });
        }
    }
    
    public boolean createMember(Member member)
    {
        return DatabaseConnector.runQuery(query -> query.executeInsert(query.newRecord(Members.MEMBERS, member))) > 0;
    }
}
