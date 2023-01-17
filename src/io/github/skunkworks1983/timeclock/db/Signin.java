package io.github.skunkworks1983.timeclock.db;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class Signin {
    private final UUID id;
    private final long time;
    private int isSigningIn;
    private int isForce;

    @ConstructorProperties({"id", "time", "issigningin", "isforce"})
    public Signin(String id, long time, int isSigningIn, int isForce)
    {
        this.id = UUID.fromString(id);
        this.time = time;
        this.isSigningIn = isSigningIn;
        this.isForce = isForce;
    }

    public Signin(UUID id, long time, int isSigningIn, int isForce)
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

    public int getIsSigningIn() {
        return isSigningIn;
    }

    public int getIsForce() {
        return isForce;
    }
}
