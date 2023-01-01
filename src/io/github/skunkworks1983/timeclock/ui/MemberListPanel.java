package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.KeyboardFocusManager;

public class MemberListPanel extends JPanel
{
    private final MemberList memberList;
    
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
        
        headerPanel.setLayout(new MigLayout("fillx, insets 4", "[40!][150:][grow][300:][100:]", "[30!]"));
        headerPanel.add(rowLabel);
        headerPanel.add(firstName);
        headerPanel.add(lastName);
        headerPanel.add(signInTime);
        headerPanel.add(requirementMet);
        
        setLayout(new MigLayout("fill", "[grow]", "[30!][grow]"));
        
        add(headerPanel, "cell 0 0, growx");
        add(scrollPane, "cell 0 1, grow");
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if(MemberListPanel.this.isVisible() && MemberListPanel.this.getMemberList().hasFocus())
            {
                if(e.getKeyCode() == 107) // magic number for numpad +
                {
                    int scrollValue = scrollPane.getVerticalScrollBar().getValue();
                    int delta = Math.max(
                            (scrollPane.getVerticalScrollBar().getMaximum() - scrollPane.getVerticalScrollBar()
                                                                                        .getMinimum()) / 50, 1);
                    scrollValue = Math.min(scrollValue + delta, scrollPane.getVerticalScrollBar().getMaximum());
                    scrollPane.getVerticalScrollBar().setValue(scrollValue);
                }
                else if(e.getKeyCode() == 109)
                {
                    int scrollValue = scrollPane.getVerticalScrollBar().getValue();
                    int delta = Math.max(
                            (scrollPane.getVerticalScrollBar().getMaximum() - scrollPane.getVerticalScrollBar()
                                                                                        .getMinimum()) / 50, 1);
                    scrollValue = Math.max(scrollValue - delta, scrollPane.getVerticalScrollBar().getMinimum());
                    scrollPane.getVerticalScrollBar().setValue(scrollValue);
                }
            }
            return false;
        });
    }
    
    public MemberList getMemberList()
    {
        return memberList;
    }
}
