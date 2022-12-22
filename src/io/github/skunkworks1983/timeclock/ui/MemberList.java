package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.MemberStore;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class MemberList extends JList<Member>
{
    private MemberStore memberStore;
    
    @Inject
    public MemberList(MemberStore memberStore, PinWindow pinWindow)
    {
        super();
        this.memberStore = memberStore;
        
        setListData(memberStore.getMembers().toArray(new Member[0]));
        setCellRenderer(new MemberListCellRenderer());
        
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clearSelection();
        
        addKeyListener(new KeyListener()
        {
            String buffer = "";
            
            @Override
            public void keyTyped(KeyEvent e)
            {
                if('0' <= e.getKeyChar() && e.getKeyChar() <= '9')
                {
                    buffer += e.getKeyChar();
                    System.out.println("buffer " + buffer);
                }
                else if(e.getKeyChar() == '\n')
                {
                    System.out.println("enter");
                    clearSelection();
                    if(!buffer.isBlank())
                    {
                        int selection = Integer.parseInt(buffer);
                        if(0 < selection && selection <= getModel().getSize())
                        {
                            selection -= 1;
                            setSelectedIndex(selection);
                            System.out.println("selecting " + selection);
                            pinWindow.setCurrentMember(getSelectedValue());
                            pinWindow.setSuccessCallback((member ->
                                {
                                    memberStore.toggleSignIn(member.getId());
                                    return null;
                                }));
                            pinWindow.setVisible(true);
                        }
                        else
                        {
                            clearSelection();
                        }
                    }
                    else
                    {
                        clearSelection();
                    }
                    buffer = "";
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e)
            {
            
            }
            
            @Override
            public void keyReleased(KeyEvent e)
            {
            
            }
        });
    }
    
    private static class MemberListCellRenderer implements ListCellRenderer<Member>
    {
        
        @Override
        public Component getListCellRendererComponent(JList<? extends Member> list, Member value, int index,
                                                      boolean isSelected, boolean cellHasFocus)
        {
            JPanel rowPanel = new JPanel();
            JLabel rowLabel = new JLabel(Integer.toString(index + 1));
            JLabel firstName = new JLabel(value.getFirstName());
            JLabel lastName = new JLabel(value.getLastName());
            JLabel signInTime = new JLabel(value.isSignedIn()
                                                   ? DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
                    LocalDateTime.ofEpochSecond(value.getLastSignIn(),
                                                0,
                                                ZoneOffset.ofHours(-8)))
                                                   : "Signed out");
            JLabel requirementMet = new JLabel(value.getHours() > 0 ? "yes" : "no");
            
            rowPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            
            rowPanel.add(rowLabel);
            rowPanel.add(firstName);
            rowPanel.add(lastName);
            rowPanel.add(signInTime);
            rowPanel.add(requirementMet);
            
            if(isSelected)
            {
                rowPanel.setBackground(Color.LIGHT_GRAY);
            }
            else
            {
                rowPanel.setBackground(Color.WHITE);
            }
            
            return rowPanel;
        }
    }
}
