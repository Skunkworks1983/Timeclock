package io.github.skunkworks1983.timeclock.db;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil
{
    public static String computeHash(byte[] salt, char[] pin) throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        CharBuffer pinBuffer = CharBuffer.wrap(pin);
        byte[] hash = digest.digest(Charset.defaultCharset().encode(pinBuffer).array());
        StringBuilder hashBuilder = new StringBuilder();
        for(byte b : hash)
        {
            hashBuilder.append(String.format("%02x", b));
        }
        return hashBuilder.toString();
    }
}
