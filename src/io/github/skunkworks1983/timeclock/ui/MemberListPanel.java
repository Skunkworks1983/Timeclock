package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MemberListPanel extends JPanel
{
    private MemberList memberList;
    
    @Inject
    public MemberListPanel(MemberList memberList)
    {
        this.memberList = memberList;
        JScrollPane scrollPane = new JScrollPane(this.memberList);
        
        add(scrollPane);
    }
}
