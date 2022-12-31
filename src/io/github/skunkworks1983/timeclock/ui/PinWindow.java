package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.controller.SignInController;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.PinStore;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Arrays;

public class PinWindow extends JFrame
{
    private Member currentMember;
    private JLabel promptText;
    private JPasswordField pinField;
    
    @Inject
    public PinWindow(SignInController signInController, AlertWindow alertWindow, MainListRefresher refresher) throws HeadlessException
    {
        super("Enter PIN");
    
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        promptText = new JLabel("No member set");
        pinField = new JPasswordField();
        
        setAutoRequestFocus(true);
        
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        add(promptText);
        add(pinField);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(false);
        
        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e)
            {
                pinField.grabFocus();
            }
    
            @Override
            public void windowLostFocus(WindowEvent e)
            {
        
            }
        });
        
        pinField.addKeyListener(new KeyListener() {
            private boolean readyToClose = true;
            
            @Override
            public void keyTyped(KeyEvent e)
            {
                if(e.getKeyChar() == '\n')
                {
                    char[] pinChars = pinField.getPassword();
                    if(pinChars.length == PinStore.PIN_LENGTH)
                    {
                        AlertMessage alert = signInController.handleSignIn(currentMember, !currentMember.isSignedIn(), pinChars);
                        alertWindow.showAlert(alert);
                        if(alert.isSuccess())
                        {
                            setVisible(false);
                            refresher.refresh();
                        }
                    }
                    Arrays.fill(pinChars, (char) 0);
                    pinField.setText("");
                    readyToClose = true;
                }
                else if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE && pinField.getPassword().length == 0)
                {
                    // add an extra step here because otherwise the window will hide when backspacing the last character
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
    
    public void setCurrentMember(Member currentMember)
    {
        this.currentMember = currentMember;
        promptText.setText(String.format("Enter pin for %s %s (backspace to return to menu without signing in):",
                                         currentMember.getFirstName(), currentMember.getLastName()));
        pack();
    }
}
