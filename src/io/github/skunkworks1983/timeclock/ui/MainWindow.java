package io.github.skunkworks1983.timeclock.ui;

import io.github.skunkworks1983.timeclock.db.MemberStore;
import io.github.skunkworks1983.timeclock.db.PinStore;

import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.GridLayout;

public class MainWindow extends JFrame
{
    public MainWindow() throws Exception
    {
        super("Skunk Works Timeclock");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout());
        
        add(new MemberListPanel(new MemberStore(), new PinWindow(new PinStore())));
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
