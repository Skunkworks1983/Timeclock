package io.github.skunkworks1983.timeclock.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector
{
    private static String databaseFile = "";
    
    public static Connection createConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
    }
    
    public static void setDatabaseFile(String databaseFile)
    {
        DatabaseConnector.databaseFile = databaseFile;
    }
}
