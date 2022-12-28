package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import net.miginfocom.swing.MigLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.Font;
import java.awt.GridLayout;

public class MainWindow extends JFrame
{
    @Inject
    public MainWindow(MemberListPanel memberListPanel) throws Exception
    {
        super("Skunk Works Timeclock");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel instructions = new JLabel("Enter a number to sign in/out");
        
        getContentPane().setLayout(new MigLayout("fill, insets 0 0 0 0", "[800!]", "[1250!][30!]"));
        
        add(memberListPanel, "cell 0 0, grow");
        add(instructions, "cell 0 1, gap 5!, growx");
    }
    
    public void prepare()
    {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
