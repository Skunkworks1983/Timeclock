package io.github.skunkworks1983.timeclock;

import io.github.skunkworks1983.timeclock.db.DatabaseConnector;
import io.github.skunkworks1983.timeclock.ui.MainWindow;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        if(args.length > 0)
        {
            if(Files.exists(Path.of(args[0])))
            {
                DatabaseConnector.setDatabaseFile(args[0]);
            }
        }
        new MainWindow();
    }
}
