package io.github.skunkworks1983.timeclock.db;

import java.util.UUID;

public class PinStore
{
    public static int PIN_LENGTH = 4;
    
    public boolean checkPin(UUID memberId, String pin)
    {
        return pin.equals("1234");
    }
}
