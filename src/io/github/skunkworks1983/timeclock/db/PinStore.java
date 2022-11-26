package io.github.skunkworks1983.timeclock.db;

import io.github.skunkworks1983.timeclock.db.generated.tables.Pins;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class PinStore
{
    public static int PIN_LENGTH = 4;
    
    public boolean checkPin(UUID memberId, String pin)
    {
        try(Connection connection = DatabaseConnector.createConnection())
        {
            DSLContext query = DSL.using(connection, SQLDialect.SQLITE);
    
            Result<Record2<byte[], String>> result = query.select(Pins.PINS.SALT, Pins.PINS.HASH)
                                                          .from(Pins.PINS)
                                                          .where(Pins.PINS.MEMBERID.eq(memberId.toString()))
                                                          .fetch();
            byte[] salt = result.getValues(Pins.PINS.SALT).get(0);
            return computeHash(salt, result.getValues(Pins.PINS.HASH).get(0).toCharArray()).equals(computeHash(salt, pin.toCharArray()));
        }
        catch(SQLException e)
        {
            System.err.println("Failed to query for PIN: " + e.getMessage());
            return false;
        }
        catch(NoSuchAlgorithmException e)
        {
            System.err.println("Failed to compute hash for PIN: " + e.getMessage());
            return false;
        }
    }
    
    public char[] createPin(UUID memberId)
    {
        try
        {
            SecureRandom rand = SecureRandom.getInstanceStrong();
            char[] pin = new char[PIN_LENGTH];
            byte[] salt = new byte[4];
            for(int i = 0; i < pin.length; i++)
            {
                pin[i] = Character.forDigit(rand.nextInt(10), 10);
            }
            rand.nextBytes(salt);
            
            String hash = computeHash(salt, pin);
            
            try(Connection connection = DatabaseConnector.createConnection())
            {
                DSLContext query = DSL.using(connection, SQLDialect.SQLITE);
                
                query.insertInto(Pins.PINS, Pins.PINS.MEMBERID, Pins.PINS.SALT, Pins.PINS.HASH)
                        .values(memberId.toString(), salt, hash)
                        .execute();
            }
            catch(SQLException e)
            {
                System.err.println("Inserting PIN failed: " + e.getMessage());
                return null;
            }
    
            return pin;
        }
        catch(NoSuchAlgorithmException e)
        {
            System.err.println("Generating PIN failed: " + e.getMessage());
            return null;
        }
    }
    
    private String computeHash(byte[] salt, char[] pin) throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        CharBuffer pinBuffer = CharBuffer.wrap(pin);
        byte[] hash = digest.digest(Charset.defaultCharset().encode(pinBuffer).array());
        pinBuffer.clear();
        for(int i = 0; i < pin.length; i++)
        {
            pinBuffer.put('0');
        }
        StringBuilder hashBuilder = new StringBuilder();
        for(byte b: hash)
        {
            hashBuilder.append(String.format("%02x", b));
        }
        return hashBuilder.toString();
    }
}
