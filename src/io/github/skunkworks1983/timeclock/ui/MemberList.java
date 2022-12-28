package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.controller.SessionController;
import io.github.skunkworks1983.timeclock.controller.SignInController;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.MemberStore;
import io.github.skunkworks1983.timeclock.db.TimeUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MemberList extends JList<Member>
{
    @Inject
    public MemberList(MemberStore memberStore, SignInController signInController, SessionController sessionController,
                      PinWindow pinWindow, PinCreationWindow pinCreationWindow, AlertWindow alertWindow)
    {
        super();
        
        setListData(memberStore.getMembers().toArray(new Member[0]));
        setCellRenderer(new MemberListCellRenderer(sessionController));
        
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
                            Member selectedMember = getSelectedValue();
                            if(signInController.shouldCreatePin(selectedMember))
                            {
                                alertWindow.showAlert(new AlertMessage(false,
                                                                       "You haven't set a PIN yet. Please set a four-digit PIN to sign in and out with on the next screen. Your PIN may not contain the same number more than two times in a row.",
                                                                       () ->
                                    {
                                        pinCreationWindow.setCurrentMember(selectedMember);
                                        pinCreationWindow.setSuccessCallback(() -> setListData(memberStore.getMembers().toArray(new Member[0])));
                                        pinCreationWindow.setVisible(true);
                                    }));
                            }
                            else
                            {
                                pinWindow.setCurrentMember(selectedMember);
                                pinWindow.setSuccessCallback(() -> setListData(memberStore.getMembers().toArray(new Member[0])));
                                pinWindow.setVisible(true);
                            }
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
            JLabel requirementMet = new JLabel(String.format("%3.1f/%3.1f", value.getHours(), 0.8 * sessionController.calculateScheduledHours()));
            
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
