package io.github.skunkworks1983.timeclock.ui;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.controller.SessionController;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.MemberStore;
import io.github.skunkworks1983.timeclock.db.Role;
import io.github.skunkworks1983.timeclock.db.TimeUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MemberList extends JList<Member>
{
    private MemberStore memberStore;
    
    private Consumer<Member> selectionCallback;
    private Set<Role> roleFilter = Arrays.stream(Role.values()).collect(Collectors.toSet());
    
    @Inject
    public MemberList(MemberStore memberStore, SessionController sessionController)
    {
        super();
        
        this.memberStore = memberStore;
        
        setCellRenderer(new MemberListCellRenderer(sessionController));
        
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clearSelection();
        
        addKeyListener(new KeyListener()
        {
            String buffer = "";
            
            @Override
            public void keyTyped(KeyEvent e)
            {
                boolean bufferUpdated = false;
                if('0' <= e.getKeyChar() && e.getKeyChar() <= '9')
                {
                    buffer += e.getKeyChar();
                    bufferUpdated = true;
                }
                else if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE && !buffer.isEmpty())
                {
                    buffer = buffer.substring(0, buffer.length() - 1);
                    bufferUpdated = true;
                }
                
                if(bufferUpdated)
                {
                    System.out.println("buffer " + buffer);
                    if(!buffer.isBlank())
                    {
                        int selection = Integer.parseInt(buffer);
                        if(0 < selection && selection <= getModel().getSize())
                        {
                            selection -= 1;
                            setSelectedIndex(selection);
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
                }
                
                if(e.getKeyChar() == '\n')
                {
                    System.out.println("enter");
                    if(getSelectedIndex() != -1)
                    {
                        System.out.println("selecting " + getSelectedIndex());
                        Member selectedMember = getSelectedValue();
                        if(selectionCallback != null)
                        {
                            selectionCallback.accept(selectedMember);
                        }
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
    
    public void setSelectionCallback(Consumer<Member> selectionCallback)
    {
        this.selectionCallback = selectionCallback;
    }
    
    public void setRoleFilter(Set<Role> roleFilter)
    {
        this.roleFilter = roleFilter;
    }
    
    public void updateListModel()
    {
        setListData(memberStore.getMembers()
                               .stream()
                               .filter(member -> roleFilter.contains(member.getRole()))
                               .toArray(Member[]::new));
    }
    
    private static class MemberListCellRenderer implements ListCellRenderer<Member>
    {
        private SessionController sessionController;
        
        public MemberListCellRenderer(SessionController sessionController)
        {
            this.sessionController = sessionController;
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends Member> list, Member value, int index,
                                                      boolean isSelected, boolean cellHasFocus)
        {
            JPanel rowPanel = new JPanel();
            JLabel rowLabel = new JLabel(Integer.toString(index + 1));
            rowLabel.setFont(new Font(null, Font.BOLD, 20));
            JLabel firstName = new JLabel(value.getFirstName());
            JLabel lastName = new JLabel(value.getLastName());
            JLabel signInTime = new JLabel(value.isSignedIn()
                                                   ? TimeUtil.formatTime(value.getLastSignIn())
                                                   : "Signed out");
            JLabel requirementMet = new JLabel(
                    String.format("%3.1f/%3.1f", value.getHours(), 0.8 * sessionController.calculateScheduledHours()));
            
            rowPanel.setLayout(new MigLayout("fillx, insets 2", "[40!][150][grow][250][100]", "[24!]"));
            
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
