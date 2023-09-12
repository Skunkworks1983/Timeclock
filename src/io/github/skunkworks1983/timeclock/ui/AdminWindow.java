package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.controller.AdminController;
import io.github.skunkworks1983.timeclock.controller.SessionController;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.Role;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class AdminWindow extends JFrame
{
    private final AdminController adminController;
    private final SessionController sessionController;
    private final AlertWindow alertWindow;
    private final CreateMemberWindow createMemberWindow;
    private final MemberPickerWindow memberPickerWindow;
    private final PinCreationWindow pinCreationWindow;
    private final GroupSignInWindow groupSignInWindow;
    private final MainListRefresher mainListRefresher;
    
    private final JLabel instructions;
    private final JPasswordField passwordField;
    private final JButton createMemberButton;
    private final JButton startScheduledSessionButton;
    private final JButton endSessionButton;
    private final JButton signMemberOutButton;
    private final JButton resetPinButton;
    private final JButton createAdminPinButton;
    private final JButton applyPenaltyButton;
    private final JButton rebuildHoursButton;
    private final JButton groupSignInButton;
    
    private boolean authenticated = false;
    private Member currentAdmin = null;
    
    @Inject
    public AdminWindow(AdminController adminController,
                       SessionController sessionController,
                       AlertWindow alertWindow,
                       CreateMemberWindow createMemberWindow,
                       MemberPickerWindow memberPickerWindow,
                       PinCreationWindow pinCreationWindow,
                       GroupSignInWindow groupSignInWindow,
                       MainListRefresher mainListRefresher)
            throws HeadlessException
    {
        super("Admin");
        
        this.adminController = adminController;
        this.sessionController = sessionController;
        this.alertWindow = alertWindow;
        this.createMemberWindow = createMemberWindow;
        this.memberPickerWindow = memberPickerWindow;
        this.pinCreationWindow = pinCreationWindow;
        this.groupSignInWindow = groupSignInWindow;
        this.mainListRefresher = mainListRefresher;
        
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        instructions = new JLabel("Enter admin password to continue");
        
        passwordField = new JPasswordField();
        createMemberButton = new JButton("1: Create member");
        startScheduledSessionButton = new JButton("2: Queue today's scheduled meeting start");
        endSessionButton = new JButton("3: End meeting and force sign-outs");
        signMemberOutButton = new JButton("4: Force a member to sign out");
        resetPinButton = new JButton("5: Reset member's PIN");
        createAdminPinButton = new JButton("6: Set PIN for admin member");
        applyPenaltyButton = new JButton("7: Apply time penalty to member");
        rebuildHoursButton = new JButton("8: Rebuild member hours from signins");
        groupSignInButton = new JButton("9: Group sign-in/out");
    
        setIconImage(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("skunkicon.png")));
        
        setLayout(new MigLayout("", "[grow]", "[][][][][][][][][][]"));
        
        add(instructions, "cell 0 0, grow");
        add(passwordField, "cell 0 1, grow");
        add(createMemberButton, "cell 0 2, grow");
        add(startScheduledSessionButton, "cell 0 3, grow");
        add(endSessionButton, "cell 0 4, grow");
        add(signMemberOutButton, "cell 0 5, grow");
        add(resetPinButton, "cell 0 6, grow");
        add(createAdminPinButton, "cell 0 7, grow");
        add(applyPenaltyButton, "cell 0 8, grow");
        add(rebuildHoursButton, "cell 0 9, grow");
        add(groupSignInButton, "cell 0 10, grow");
        
        pack();
        setLocationRelativeTo(null);
        setVisible(false);
        
        passwordField.addKeyListener(new KeyAdapter()
        {
            private boolean readyToClose = true;
            
            @Override
            public void keyTyped(KeyEvent e)
            {
                if(e.getKeyChar() == '\n')
                {
                    char[] enteredPass = passwordField.getPassword();
                    AlertMessage authMessage = adminController.authenticateAdminWindow(enteredPass);
                    alertWindow.showAlert(authMessage);
                    setAuthenticated(authMessage.isSuccess());
                    passwordField.setText("");
                    readyToClose = true;
                }
                else if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE && passwordField.getPassword().length == 0)
                {
                    if(readyToClose)
                    {
                        setVisible(false);
                    }
                    else
                    {
                        readyToClose = true;
                    }
                }
                else
                {
                    readyToClose = false;
                }
            }
        });
        
        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE)
                {
                    setVisible(false);
                }
                else if(isAuthenticated())
                {
                    switch(e.getKeyChar())
                    {
                        case KeyEvent.VK_1:
                            showCreateMemberWindow();
                            break;
                        case KeyEvent.VK_2:
                            startScheduledSession();
                            break;
                        case KeyEvent.VK_3:
                            endSession();
                            break;
                        case KeyEvent.VK_4:
                            forceSignOut();
                            break;
                        case KeyEvent.VK_5:
                            resetPin();
                            break;
                        case KeyEvent.VK_6:
                            createAdminPin();
                            break;
                        case KeyEvent.VK_7:
                            applyPenalty();
                            break;
                        case KeyEvent.VK_8:
                            rebuildHours();
                            break;
                        case KeyEvent.VK_9:
                            runGroupSignIn();
                            break;
                    }
                }
            }
        });
        
        createMemberButton.addActionListener(e -> showCreateMemberWindow());
        startScheduledSessionButton.addActionListener(e -> startScheduledSession());
        endSessionButton.addActionListener(e -> endSession());
        signMemberOutButton.addActionListener(e -> forceSignOut());
        resetPinButton.addActionListener(e -> resetPin());
        createAdminPinButton.addActionListener(e -> createAdminPin());
        applyPenaltyButton.addActionListener(e -> applyPenalty());
        rebuildHoursButton.addActionListener(e -> rebuildHours());
        groupSignInButton.addActionListener(e-> runGroupSignIn());
    }
    
    private void showCreateMemberWindow()
    {
        createMemberWindow.setVisible(true);
    }
    
    private void startScheduledSession()
    {
        alertWindow.showAlert(new AlertMessage(true, "Choose an admin to start the session.", () ->
        {
            memberPickerWindow.getMemberListPanel().getMemberList().setRoleFilter(Collections.singleton(Role.ADMIN));
            memberPickerWindow.getMemberListPanel().getMemberList().setSelectionCallback(member -> {
                alertWindow.showAlert(sessionController.startSession(member, true));
                memberPickerWindow.setVisible(false);
                mainListRefresher.refresh();
            });
            memberPickerWindow.setVisible(true);
        }));
    }
    
    private void endSession()
    {
        alertWindow.showAlert(new AlertMessage(true, "Choose an admin to end the session.", () ->
        {
            memberPickerWindow.getMemberListPanel().getMemberList().setRoleFilter(Collections.singleton(Role.ADMIN));
            memberPickerWindow.getMemberListPanel().getMemberList().setSelectionCallback(member -> {
                alertWindow.showAlert(sessionController.endSession(member));
                memberPickerWindow.setVisible(false);
                mainListRefresher.refresh();
            });
            memberPickerWindow.setVisible(true);
        }));
    }
    
    private void forceSignOut()
    {
        memberPickerWindow.getMemberListPanel()
                          .getMemberList()
                          .setRoleFilter(Arrays.stream(Role.values()).collect(Collectors.toSet()));
        memberPickerWindow.getMemberListPanel().getMemberList().setSelectionCallback(member -> {
            alertWindow.showAlert(adminController.forceSignOut(member));
            memberPickerWindow.setVisible(false);
            mainListRefresher.refresh();
        });
        memberPickerWindow.setVisible(true);
    }
    
    private void resetPin()
    {
        memberPickerWindow.getMemberListPanel().getMemberList().setRoleFilter(
                Arrays.stream(Role.values()).collect(Collectors.toSet()));
        memberPickerWindow.getMemberListPanel().getMemberList().setSelectionCallback(member -> {
            alertWindow.showAlert(adminController.resetPin(member));
            memberPickerWindow.setVisible(false);
        });
        memberPickerWindow.setVisible(true);
    }
    
    private void createAdminPin()
    {
        memberPickerWindow.getMemberListPanel().getMemberList().setRoleFilter(Collections.singleton(Role.ADMIN));
        memberPickerWindow.getMemberListPanel().getMemberList().setSelectionCallback(member -> {
            pinCreationWindow.setCurrentMember(member);
            pinCreationWindow.setLocationRelativeTo(null);
            pinCreationWindow.setVisible(true);
            memberPickerWindow.setVisible(false);
        });
        memberPickerWindow.setVisible(true);
    }
    
    private void applyPenalty()
    {
        alertWindow.showAlert(new AlertMessage(true, "Choose a student to apply the penalty to. Each penalty reduces accumulated hours by 10%. The true hour count is preserved, so they can be reversed at any time.", () ->
        {
            memberPickerWindow.getMemberListPanel().getMemberList().setRoleFilter(Collections.singleton(Role.STUDENT));
            memberPickerWindow.getMemberListPanel().getMemberList().setSelectionCallback(member -> {
                alertWindow.showAlert(adminController.applyPenalty(member));
                memberPickerWindow.setVisible(false);
                mainListRefresher.refresh();
            });
            memberPickerWindow.setVisible(true);
        }));
    }

    private void rebuildHours()
    {
        alertWindow.showAlert(adminController.rebuildHours());
    }

    private void runGroupSignIn()
    {
        alertWindow.showAlert(new AlertMessage(true, "Choose an admin to start/end the sessions.", () -> {
            memberPickerWindow.getMemberListPanel().getMemberList().setRoleFilter(Collections.singleton(Role.ADMIN));
            memberPickerWindow.getMemberListPanel().getMemberList().setSelectionCallback(member -> {
               groupSignInWindow.setCurrentAdmin(member);
               memberPickerWindow.setVisible(false);
               groupSignInWindow.setVisible(true);
            });
            memberPickerWindow.setVisible(true);
        }));
    }
    
    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        passwordField.setText("");
        setAuthenticated(false); // always require reauth when window is opened or closed
    }
    
    public boolean isAuthenticated()
    {
        return authenticated;
    }
    
    public void setAuthenticated(boolean authenticated)
    {
        this.authenticated = authenticated;
        
        instructions.setText(authenticated
                                     ? "Enter a number or click a button to select an action"
                                     : "Enter admin password to continue");
        
        passwordField.setEnabled(!authenticated);
        passwordField.setVisible(!authenticated);
        
        createMemberButton.setEnabled(authenticated);
        createMemberButton.setVisible(authenticated);
        startScheduledSessionButton.setEnabled(authenticated);
        startScheduledSessionButton.setVisible(authenticated);
        endSessionButton.setEnabled(authenticated);
        endSessionButton.setVisible(authenticated);
        signMemberOutButton.setEnabled(authenticated);
        signMemberOutButton.setVisible(authenticated);
        resetPinButton.setEnabled(authenticated);
        resetPinButton.setVisible(authenticated);
        createAdminPinButton.setEnabled(authenticated);
        createAdminPinButton.setVisible(authenticated);
        applyPenaltyButton.setEnabled(authenticated);
        applyPenaltyButton.setVisible(authenticated);
        rebuildHoursButton.setEnabled(authenticated);
        rebuildHoursButton.setVisible(authenticated);
        groupSignInButton.setEnabled(authenticated);
        groupSignInButton.setVisible(authenticated);
        
        pack();
        setLocationRelativeTo(null);
        
        if(authenticated)
        {
            requestFocus();
        }
        else
        {
            passwordField.grabFocus();
        }
    }
}
