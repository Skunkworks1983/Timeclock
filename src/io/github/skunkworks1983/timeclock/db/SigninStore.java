package io.github.skunkworks1983.timeclock.db;

import org.jooq.Record1;

import java.util.List;

import static io.github.skunkworks1983.timeclock.db.generated.tables.Signins.SIGNINS;

public class SigninStore
{
    public SigninStore() {}

    public List<Signin> getSignins()
    {
        List<Signin> signinList = DatabaseConnector
                .runQuery(query -> {
                    List<Signin> signins = query.selectFrom(SIGNINS)
                            .orderBy(SIGNINS.TIME.asc())
                            .fetch()
                            .into(Signin.class);
                    return signins;
                });

        return signinList;
    }
    
    public boolean hasOverlappingSignIn(Member member, long time)
    {
        long lastSignInBefore = DatabaseConnector.runQuery(query -> {
            Record1<Long> result = query.select(SIGNINS.TIME)
                                        .from(SIGNINS)
                                        .where(SIGNINS.TIME.lessThan(time), SIGNINS.ID.eq(member.getId().toString()), SIGNINS.ISSIGNINGIN.eq(1))
                                        .orderBy(SIGNINS.TIME.desc())
                                        .fetchAny();
            
            return (result == null || result.value1() == null) ? -1 : result.value1();
        });
    
        if(lastSignInBefore == -1)
        {
            return false;
        }
        
        long firstSignOutAfter = DatabaseConnector.runQuery(query -> {
            Record1<Long> result = query.select(SIGNINS.TIME)
                               .from(SIGNINS)
                               .where(SIGNINS.TIME.greaterThan(lastSignInBefore), SIGNINS.ID.eq(member.getId().toString()), SIGNINS.ISSIGNINGIN.eq(0))
                               .orderBy(SIGNINS.TIME.asc())
                               .fetchAny();
    
            return (result == null || result.value1() == null) ? -1 : result.value1();
        });
        
        if(firstSignOutAfter == -1)
        {
            return false;
        }
        
        return lastSignInBefore < time && time < firstSignOutAfter;
    }
}
