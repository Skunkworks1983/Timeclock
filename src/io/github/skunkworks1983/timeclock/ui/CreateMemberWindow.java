package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.controller.AdminController;
import io.github.skunkworks1983.timeclock.db.Role;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateMemberWindow extends JFrame
{
    private JLabel roleLabel;
    private JComboBox<Role> roleDropdown;
    private JLabel firstNameLabel;
    private JTextField firstNameField;
    private JLabel lastNameLabel;
    private JTextField lastNameField;
    private JButton okButton;
    
    @Inject
    public CreateMemberWindow(AdminController adminController, AlertWindow alertWindow, MainListRefresher refresher)
    {
        super("Create member");
        
        roleLabel = new JLabel("Role");
        roleDropdown = new JComboBox<>();
        roleDropdown.setModel(new DefaultComboBoxModel<>(Role.values()));
        
        firstNameLabel = new JLabel("First Name");
        firstNameField = new JTextField();
        
        lastNameLabel = new JLabel("Last Name");
        lastNameField = new JTextField();
        
        okButton = new JButton("OK");
        
        setLayout(new MigLayout("", "[grow]", "[][][][][][][]"));
        
        add(roleLabel, "cell 0 0, grow");
        add(roleDropdown, "cell 0 1, grow");
        add(firstNameLabel, "cell 0 2, grow");
        add(firstNameField, "cell 0 3, grow");
        add(lastNameLabel, "cell 0 4, grow");
        add(lastNameField, "cell 0 5, grow");
        add(okButton, "cell 0 6, grow");
    
        pack();
        setLocationRelativeTo(null);
        
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
    
                Role role = (Role) roleDropdown.getSelectedItem();
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                if(role != null && firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank())
                {
                    AlertMessage alertMessage = adminController.createMember(role, firstName, lastName);
                    alertWindow.showAlert(new AlertMessage(alertMessage.isSuccess(), alertMessage.getMessage(), refresher::refresh));
                    setVisible(false);
                }
                else
                {
                    alertWindow.showAlert(new AlertMessage(false, "Please fill in all fields."));
                }
            }
        });
    }
    
    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        roleDropdown.setSelectedItem(Role.STUDENT);
        firstNameField.setText("");
        lastNameField.setText("");
    }
}
