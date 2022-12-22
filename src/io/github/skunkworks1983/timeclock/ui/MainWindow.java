package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;

import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.GridLayout;

public class MainWindow extends JFrame
{
    @Inject
    public MainWindow(MemberListPanel memberListPanel) throws Exception
    {
        super("Skunk Works Timeclock");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout());
        
        add(memberListPanel);
    }
    
    public void prepare()
    {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
