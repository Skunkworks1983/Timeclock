package io.github.skunkworks1983.timeclock.db;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class Member
{
    private final UUID id;
    private final Role role;
    private final String firstName;
    private final String lastName;
    private double hours;
    private long lastSignIn;
    private boolean isSignedIn;
    
    @ConstructorProperties({"id", "role", "firstname", "lastname", "hours", "lastsignedin", "issignedin"})
    public Member(String id, Role role, String firstName, String lastName, double hours, long lastSignIn,
                  boolean isSignedIn)
    {
        this.id = UUID.fromString(id);
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hours = hours;
        this.lastSignIn = lastSignIn;
        this.isSignedIn = isSignedIn;
    }
    
    public Member(UUID id, Role role, String firstName, String lastName, double hours, long lastSignIn,
                  boolean isSignedIn)
    {
        this.id = id;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hours = hours;
        this.lastSignIn = lastSignIn;
        this.isSignedIn = isSignedIn;
    }
    
    public UUID getId()
    {
        return id;
    }
    
    public Role getRole()
    {
        return role;
    }
    
    public String getFirstName()
    {
        return firstName;
    }
    
    public String getLastName()
    {
        return lastName;
    }
    
    public double getHours()
    {
        return hours;
    }
    
    public void setHours(double hours)
    {
        this.hours = hours;
    }
    
    public long getLastSignIn()
    {
        return lastSignIn;
    }
    
    public void setLastSignIn(long lastSignIn)
    {
        this.lastSignIn = lastSignIn;
    }
    
    public boolean isSignedIn()
    {
        return isSignedIn;
    }
    
    public void setSignedIn(boolean signedIn)
    {
        isSignedIn = signedIn;
    }
}
