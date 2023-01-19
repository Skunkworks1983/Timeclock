package io.github.skunkworks1983.timeclock.db;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class Signin {
    private final UUID id;
    private final long time;
    private boolean isSigningIn;
    private boolean isForce;

    @ConstructorProperties({"id", "time", "issigningin", "isforce"})
    public Signin(String id, long time, boolean isSigningIn, boolean isForce)
    {
        this.id = UUID.fromString(id);
        this.time = time;
        this.isSigningIn = isSigningIn;
        this.isForce = isForce;
    }

    public Signin(UUID id, long time, boolean isSigningIn, boolean isForce)
    {
        this.id = id;
        this.time = time;
        this.isSigningIn = isSigningIn;
        this.isForce = isForce;
    }

    public UUID getId() {
        return id;
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
}
