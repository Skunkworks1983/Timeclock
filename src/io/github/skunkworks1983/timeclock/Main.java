package io.github.skunkworks1983.timeclock;

import com.google.inject.Guice;
import io.github.skunkworks1983.timeclock.controller.ControllerModule;
import io.github.skunkworks1983.timeclock.db.DatabaseConnector;
import io.github.skunkworks1983.timeclock.db.DatabaseModule;
import io.github.skunkworks1983.timeclock.ui.MainWindow;
import io.github.skunkworks1983.timeclock.ui.UiModule;

import javax.swing.UIManager;
import java.awt.Font;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        String awsCredPath = null;
        if(args.length > 0)
        {
            if(Files.exists(Path.of(args[0])))
            {
                DatabaseConnector.setDatabaseFile(args[0]);
            }
            
            if(args.length > 1)
            {
                if(Files.exists(Path.of(args[1])))
                {
                    awsCredPath = args[1];
                }
            }
        }
    
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Label.font", new Font(null, Font.PLAIN, 20));
        UIManager.put("Button.font", new Font(null, Font.PLAIN, 20));
        UIManager.put("TextField.font", new Font(null, Font.PLAIN, 20));
        UIManager.put("ComboBox.font", new Font(null, Font.PLAIN, 20));
        UIManager.put("PasswordField.font", new Font(null, Font.PLAIN, 20));
        
        Guice.createInjector(new UiModule(), new ControllerModule(awsCredPath)).getInstance(MainWindow.class).prepare();
    }
}
