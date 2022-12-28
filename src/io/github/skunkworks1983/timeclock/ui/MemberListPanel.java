package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.TimeUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Font;

public class MemberListPanel extends JPanel
{
    private MemberList memberList;
    
    @Inject
    public MemberListPanel(MemberList memberList)
    {
        this.memberList = memberList;
        JScrollPane scrollPane = new JScrollPane(this.memberList);
        JPanel headerPanel = new JPanel();
        JLabel rowLabel = new JLabel("#");
        JLabel firstName = new JLabel("First Name");
        JLabel lastName = new JLabel("Last Name");
        JLabel signInTime = new JLabel("Signed In At");
        JLabel requirementMet = new JLabel("Hours");
        
        headerPanel.setLayout(new MigLayout("fillx, insets 4", "[40!][150][grow][250][100]", "[24!]"));
        headerPanel.add(rowLabel);
        headerPanel.add(firstName);
        headerPanel.add(lastName);
        headerPanel.add(signInTime);
        headerPanel.add(requirementMet);
        
        setLayout(new MigLayout("fill", "[grow]", "[24!][grow]"));
        
        add(headerPanel, "cell 0 0, growx");
        add(scrollPane, "cell 0 1, grow");
    }
}
