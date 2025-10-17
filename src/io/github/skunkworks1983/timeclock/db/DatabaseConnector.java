package io.github.skunkworks1983.timeclock.db;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Function;

public class DatabaseConnector
{
    public static final String ATTACHED_DB_NAME = "attached";
    
    private static String databaseFile = "";
    
    public static Connection createConnection() throws SQLException
    {
        return createConnection(getDatabaseFile());
    }
    
    public static Connection createConnection(String fileName) throws SQLException
    {
        return DriverManager.getConnection("jdbc:sqlite:" + fileName);
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
        return runQuery(func, getDatabaseFile());
    }
    
    public static <T> T runQuery(Function<DSLContext, T> func, String dbFileName)
    {
        try(Connection connection = DatabaseConnector.createConnection(dbFileName))
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
    
    public static <T> T runAttachedQuery(Function<DSLContext, T> func, String baseFileName, String attachFileName)
    {
        try(Connection connection = DatabaseConnector.createConnection(baseFileName))
        {
            connection.prepareStatement(String.format("ATTACH DATABASE \"%s\" AS %s", attachFileName, ATTACHED_DB_NAME)).execute();
            DSLContext query = DSL.using(connection, SQLDialect.SQLITE);
            return func.apply(query);
        }
        catch(SQLException throwables)
        {
            throwables.printStackTrace();
            return null;
        }
    }
    
    public static void setUpDatabase(String databaseFile) throws IOException
    {
        System.out.println("setting up database " + databaseFile);
        if(!Files.exists(Path.of(databaseFile)))
        {
            System.out.println(databaseFile + " not found, generating new database");
            Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream("blanktable"), Path.of(databaseFile));
        }
        setDatabaseFile(databaseFile);
    }
}
