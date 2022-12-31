package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.Role;

import javax.swing.JFrame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MemberPickerWindow extends JFrame
{
    private final MemberListPanel memberListPanel;
    
    @Inject
    public MemberPickerWindow(MemberListPanel memberListPanel) throws HeadlessException
    {
        super("Choose a user");
        this.memberListPanel = memberListPanel;
    
        setIconImage(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("skunkicon.png")));
        
        add(memberListPanel);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    public MemberListPanel getMemberListPanel()
    {
        return memberListPanel;
    }
    
    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        if(!b)
        {
            memberListPanel.getMemberList().setRoleFilter(Arrays.stream(Role.values()).collect(Collectors.toSet()));
        }
        else
        {
            memberListPanel.getMemberList().updateListModel();
            memberListPanel.getMemberList().grabFocus();
            setLocationRelativeTo(null);
        }
    }
}
