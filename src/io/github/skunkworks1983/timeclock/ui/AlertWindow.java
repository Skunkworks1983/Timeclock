package io.github.skunkworks1983.timeclock.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class AlertWindow extends JFrame
{
    private final JLabel messageLabel;
    private final JButton okButton;
    private Runnable okButtonCallback;
    
    public AlertWindow() throws HeadlessException
    {
        messageLabel = new JLabel();
        okButton = new JButton("OK");
    
        setIconImage(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("skunkicon.png")));
        
        setLayout(new MigLayout("", "[grow]", "[][]"));
        
        add(messageLabel, "cell 0 0, grow");
        add(okButton, "cell 0 1, center");
        
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                if(okButtonCallback != null)
                {
                    okButtonCallback.run();
                }
            }
        });
        
        addWindowFocusListener(new WindowFocusListener()
        {
            @Override
            public void windowGainedFocus(WindowEvent e)
            {
                okButton.grabFocus();
            }
            
            @Override
            public void windowLostFocus(WindowEvent e)
            {
            
            }
        });
        
        setAlwaysOnTop(true);
        setAutoRequestFocus(true);
    }
    
    public void showAlert(AlertMessage message)
    {
        if(message.getMessage() != null)
        {
            setTitle(message.isSuccess() ? "Message" : "Error");
            messageLabel.setText(message.getMessage());
            okButtonCallback = message.getOkButtonCallback();
            
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            
            requestFocus();
            okButton.grabFocus();
        }
    }
}
