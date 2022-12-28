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

public class PinCreationWindow extends JFrame
{
    private Member currentMember;
    private JLabel promptText;
    private JPasswordField pinField;
    
    @Inject
    public PinCreationWindow(SignInController signInController, AlertWindow alertWindow) throws HeadlessException
    {
        super("Enter PIN");
        
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
            private char[] pinChars = null;
            private char[] pinToSet = null;
            
            @Override
            public void keyTyped(KeyEvent e)
            {
                if(e.getKeyChar() == '\n')
                {
                    pinChars = pinField.getPassword();
                    if(pinChars.length == PinStore.PIN_LENGTH)
                    {
                        if(pinToSet != null)
                        {
                            boolean pinsMatch = true;
                            for(int i = 0; i < PinStore.PIN_LENGTH; i++)
                            {
                                pinsMatch = pinsMatch && (pinToSet[i] == pinChars[i]);
                            }
                            if(pinsMatch)
                            {
                                alertWindow.showAlert(signInController.createPin(currentMember, pinToSet));
                                Arrays.fill(pinToSet, (char) 0);
                                pinToSet = null;
                                setVisible(false);
                            }
                            else
                            {
                                Arrays.fill(pinToSet, (char) 0);
                                pinToSet = null;
                                alertWindow.showAlert(new AlertMessage(false, "Entered PINs do not match, please try again.", null));
                                setCurrentMember(currentMember);
                            }
                        }
                        else
                        {
                            int repeats = 0;
                            char last = 0;
                            boolean allNumbers = true;
                            for(int i = 0; i < PinStore.PIN_LENGTH; i++)
                            {
                                if(pinChars[i] == last)
                                {
                                    repeats++;
                                }
                                else
                                {
                                    repeats = 0;
                                }
                                last = pinChars[i];
                                allNumbers = allNumbers && '0' <= pinChars[i] && pinChars[i] <= '9';
                            }
                            if(repeats < 2 && allNumbers)
                            {
                                pinToSet = pinField.getPassword();
                                promptText.setText("Enter the same PIN again to confirm:");
                            }
                            else if(allNumbers)
                            {
                                alertWindow.showAlert(new AlertMessage(false, "PIN cannot have the same number more than twice in a row, please try again.", null));
                            }
                            else
                            {
                                alertWindow.showAlert(new AlertMessage(false, "PIN may only contain numbers, please try again.", null));
                            }
                        }
                    }
                    else
                    {
                        alertWindow.showAlert(new AlertMessage(false, "PIN must be four digits, please try again.", null));
                        setCurrentMember(currentMember);
                        if(pinToSet != null)
                        {
                            Arrays.fill(pinToSet, (char) 0);
                        }
                        pinToSet = null;
                    }
                    Arrays.fill(pinChars, (char) 0);
                    pinField.setText("");
                }
                else if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE && pinField.getPassword().length == 0)
                {
                    if(pinToSet != null)
                    {
                        Arrays.fill(pinToSet, (char) 0);
                    }
                    pinToSet = null;
                    setVisible(false);
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
        promptText.setText(String.format("Enter PIN for %s %s (backspace to return to menu without creating PIN):",
                                         currentMember.getFirstName(), currentMember.getLastName()));
        pack();
    }
}
