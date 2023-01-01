package io.github.skunkworks1983.timeclock;

import com.google.inject.Guice;
import io.github.skunkworks1983.timeclock.controller.ControllerModule;
import io.github.skunkworks1983.timeclock.db.DatabaseConnector;
import io.github.skunkworks1983.timeclock.ui.MainWindow;
import io.github.skunkworks1983.timeclock.ui.UiModule;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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
        UIManager.put("Label.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("Button.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("TextField.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("TextArea.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("ComboBox.font", new FontUIResource(null, Font.PLAIN, 26));
        UIManager.put("PasswordField.font", new FontUIResource(null, Font.PLAIN, 26));
        
        Guice.createInjector(new UiModule(), new ControllerModule(awsCredPath)).getInstance(MainWindow.class).prepare();
    }
}
