package io.github.skunkworks1983.timeclock.db;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class Signin {
    private final UUID memberId;
    private final long time;
    private boolean isSigningIn;
    private boolean isForce;
    private final UUID sessionId;

    @ConstructorProperties({"memberId", "time", "issigningin", "isforce", "sessionId"})
    public Signin(String memberId, long time, boolean isSigningIn, boolean isForce, UUID sessionId)
    {
        this.memberId = UUID.fromString(memberId);
        this.time = time;
        this.isSigningIn = isSigningIn;
        this.isForce = isForce;
        this.sessionId = sessionId;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public long getTime() {
        return time;
    }

    public boolean getIsSigningIn() {
        return isSigningIn;
    }

    public boolean getIsForce() {
        return isForce;
    }
    
    public UUID getSessionId()
    {
        return sessionId;
    }
}
