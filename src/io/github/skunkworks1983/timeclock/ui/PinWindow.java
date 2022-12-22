package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
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
import java.util.function.Function;

public class PinWindow extends JFrame
{
    private PinStore pinStore;
    
    private Member currentMember;
    private Function<Member, Void> successCallback;
    
    private JLabel promptText;
    private JPasswordField pinField;
    
    @Inject
    public PinWindow(PinStore pinStore) throws HeadlessException
    {
        super("Enter PIN");
        
        promptText = new JLabel("No member set");
        pinField = new JPasswordField();
        
        setAlwaysOnTop(true);
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
            @Override
            public void keyTyped(KeyEvent e)
            {
                if(e.getKeyChar() == '\n')
                {
                    char[] pinChars = pinField.getPassword();
                    String enteredPin = new String(pinChars);
                    if(enteredPin.length() == PinStore.PIN_LENGTH && pinStore.checkPin(currentMember.getId(), enteredPin))
                    {
                        successCallback.apply(currentMember);
                        setVisible(false);
                    }
                    Arrays.fill(pinChars, (char) 0);
                    pinField.setText("");
                }
                else if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE && pinField.getPassword().length == 0)
                {
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
        promptText.setText(String.format("Enter pin for %s %s (backspace to return to menu without signing in):",
                                         currentMember.getFirstName(), currentMember.getLastName()));
        pack();
    }
    
    public void setSuccessCallback(Function<Member, Void> successCallback)
    {
        this.successCallback = successCallback;
    }
}
