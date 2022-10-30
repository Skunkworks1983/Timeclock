package io.github.skunkworks1983.timeclock.ui;

import io.github.skunkworks1983.timeclock.db.MemberStore;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class MemberListPanel extends JPanel
{
    private MemberList memberList;
    
    public MemberListPanel(MemberStore memberStore, PinWindow pinWindow)
    {
        memberList = new MemberList(memberStore, pinWindow);
        JScrollPane scrollPane = new JScrollPane(memberList);
        
        add(scrollPane);
    }
}
