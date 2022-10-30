package io.github.skunkworks1983.timeclock.db;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MemberStore
{
    private Map<UUID, Member> memberMap;
    
    public MemberStore()
    {
        memberMap = new HashMap<>();
        
        for(int i = 0; i < 40; i++)
        {
            Member test = new Member(UUID.randomUUID(), Role.ADMIN, "first" + i, "last" + (i % 5), 10 * i, System.currentTimeMillis()/1000, i % 2 == 0);
            memberMap.put(test.getId(), test);
        }
    }
    
    public List<Member> getMembers()
    {
        return memberMap.values().stream().sorted(Comparator.comparing(Member::getFirstName)).sorted(Comparator.comparing(Member::getLastName)).collect(Collectors.toList());
    }
    
    public void toggleSignIn(UUID memberId)
    {
        Member member = memberMap.get(memberId);
        if(member.isSignedIn())
        {
            member.setHours(member.getHours() + (System.currentTimeMillis() / 1000 - member.getLastSignIn()));
        }
        else
        {
            member.setLastSignIn(System.currentTimeMillis()/1000);
        }
        member.setSignedIn(!member.isSignedIn());
    }
}
