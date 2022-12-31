package io.github.skunkworks1983.timeclock.db;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Function;

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
    
    public static String getDatabaseFile()
    {
        return databaseFile;
    }
    
    public static <T> T runQuery(Function<DSLContext, T> func)
    {
        try(Connection connection = DatabaseConnector.createConnection())
        {
            DSLContext query = DSL.using(connection, SQLDialect.SQLITE);
            return func.apply(query);
        }
        catch(SQLException throwables)
        {
            throwables.printStackTrace();
            return null;
        }
    }
}
