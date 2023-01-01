package io.github.skunkworks1983.timeclock;

import com.google.inject.Guice;
import io.github.skunkworks1983.timeclock.controller.ControllerModule;
import io.github.skunkworks1983.timeclock.db.DatabaseConnector;
import io.github.skunkworks1983.timeclock.ui.MainWindow;
import io.github.skunkworks1983.timeclock.ui.UiModule;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        if(args.length > 0)
        {
            setUpDatabase(args[0]);
        }
        else
        {
            setUpDatabase("logintable");
        }
        
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Label.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("Button.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("TextField.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("TextArea.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("ComboBox.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("PasswordField.font", new FontUIResource(null, Font.PLAIN, 26));
        
        Guice.createInjector(new UiModule(), new ControllerModule()).getInstance(MainWindow.class).prepare();
    }
    
    private static void setUpDatabase(String databaseFile) throws IOException
    {
        if(!Files.exists(Path.of(databaseFile)))
        {
            System.out.println(databaseFile + " not found, generating new database");
            Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream("blanktable"), Path.of(databaseFile));
        }
        DatabaseConnector.setDatabaseFile(databaseFile);
    }
}
