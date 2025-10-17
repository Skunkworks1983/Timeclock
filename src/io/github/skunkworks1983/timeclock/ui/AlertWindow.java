package io.github.skunkworks1983.timeclock.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class AlertWindow extends JFrame
{
    private final JTextArea messageArea;
    private final JButton okButton;
    private Runnable okButtonCallback;
    
    public AlertWindow() throws HeadlessException
    {
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messageArea.setBackground((Color)UIManager.getDefaults().get("Label.background"));
        okButton = new JButton("OK");
    
        setIconImage(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("skunkicon.png")));
        
        setLayout(new MigLayout("", "[grow]", "[grow][]"));
        
        add(messageArea, "cell 0 0, grow, wmin 500");
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
        
        okButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e)
            {
                if(e.getKeyChar() == '\n')
                {
                    setVisible(false);
                    if(okButtonCallback != null)
                    {
                        okButtonCallback.run();
                    }
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
            messageArea.setText(message.getMessage());
            okButtonCallback = message.getOkButtonCallback();
            
            System.out.println(message.getMessage());
            
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            
            requestFocus();
            okButton.grabFocus();
        }
    }
}
