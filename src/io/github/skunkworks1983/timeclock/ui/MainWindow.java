package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.skunkworks1983.timeclock.controller.SignInController;
import net.miginfocom.swing.MigLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.KeyboardFocusManager;

@Singleton
public class MainWindow extends JFrame implements MainListRefresher
{
    
    private final MemberListPanel memberListPanel;
    
    @Inject
    public MainWindow(MemberListPanel memberListPanel, SignInController signInController, PinWindow pinWindow,
                      PinCreationWindow pinCreationWindow, AdminWindow adminWindow, AlertWindow alertWindow) throws
                                                                                                             Exception
    {
        super("Skunk Works Timeclock");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel instructions = new JLabel(
                "Type a number and press Enter to sign in/out; + and - to scroll; * to open admin controls");
        
        getContentPane().setLayout(new MigLayout("fill, insets 0 0 0 0", "[800!]", "[1250!][30!]"));
        
        add(memberListPanel, "cell 0 0, grow");
        add(instructions, "cell 0 1, gap 5!, growx");
        
        this.memberListPanel = memberListPanel;
        this.memberListPanel.getMemberList().setSelectionCallback(selectedMember -> {
            if(signInController.shouldCreatePin(selectedMember))
            {
                alertWindow.showAlert(new AlertMessage(false,
                                                       "You haven't set a PIN yet. Please set a four-digit PIN to sign in and out with on the next screen. Your PIN may not contain the same number more than two times in a row.",
                                                       () ->
                                                       {
                                                           pinCreationWindow.setCurrentMember(selectedMember);
                                                           pinCreationWindow.setLocationRelativeTo(null);
                                                           pinCreationWindow.setVisible(true);
                                                       })
                                     );
            }
            else
            {
                pinWindow.setCurrentMember(selectedMember);
                pinWindow.setLocationRelativeTo(null);
                pinWindow.setVisible(true);
            }
        });
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if(MainWindow.this.isFocused() && !adminWindow.isVisible() && e.getKeyChar() == '*')
            {
                adminWindow.setVisible(true);
            }
            return false;
        });
        
        memberListPanel.getMemberList().updateListModel();
    }
    
    public void prepare()
    {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    @Override
    public void refresh()
    {
        memberListPanel.getMemberList().updateListModel();
    }
}
