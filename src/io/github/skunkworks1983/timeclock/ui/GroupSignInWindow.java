package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import io.github.skunkworks1983.timeclock.controller.AdminController;
import io.github.skunkworks1983.timeclock.db.Member;
import io.github.skunkworks1983.timeclock.db.Role;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GroupSignInWindow extends JFrame
{
    private final MemberListPanel memberListPanel;
    private final AdminController adminController;
    private final AlertWindow alertWindow;
    private final MainListRefresher mainListRefresher;
    
    private DateTimePickerPanel startDatePicker;
    private DateTimePickerPanel endDatePicker;
    private JButton submitButton;
    
    private Member currentAdmin;
    
    @Inject
    public GroupSignInWindow(MemberListPanel memberListPanel, AdminController adminController, AlertWindow alertWindow,
                             MainListRefresher mainListRefresher)
    {
        super("Group Sign-In");
        
        this.memberListPanel = memberListPanel;
        this.adminController = adminController;
        this.alertWindow = alertWindow;
        this.mainListRefresher = mainListRefresher;
    
        startDatePicker = new DateTimePickerPanel("Start Time");
        endDatePicker = new DateTimePickerPanel("End Time");
        submitButton = new JButton("Submit");
        
        memberListPanel.getMemberList().enableMultiselect(true);
        
        setLayout(new MigLayout("", "[grow]", "[][][grow][]"));
        
        add(startDatePicker, "cell 0 0");
        add(endDatePicker, "cell 0 1");
        add(memberListPanel, "cell 0 2");
        add(submitButton, "cell 0 3");
    
        pack();
        setLocationRelativeTo(null);
        setVisible(false);
        
        submitButton.addActionListener(e -> {
            if(startDatePicker.isTimeValid() && endDatePicker.isTimeValid())
            {
                OffsetDateTime start = startDatePicker.getSelectedTime();
                OffsetDateTime end = endDatePicker.getSelectedTime();
                if(!start.isBefore(end))
                {
                    alertWindow.showAlert(new AlertMessage(false, "Start time must be before end time."));
                    return;
                }
                
                List<Member> members = memberListPanel.getMemberList().getSelectedValuesList();
                if(members.isEmpty())
                {
                    alertWindow.showAlert(new AlertMessage(false, "At least one member must be selected."));
                    return;
                }
                
                AlertMessage result = adminController.createGroupSignIn(members, currentAdmin, start, end);
                alertWindow.showAlert(result);
                if(result.isSuccess())
                {
                    setVisible(false);
                    mainListRefresher.refresh();
                }
            }
            else
            {
                alertWindow.showAlert(new AlertMessage(false, "Enter valid start and end times."));
            }
        });
    }
    
    public void setCurrentAdmin(Member currentAdmin)
    {
        this.currentAdmin = currentAdmin;
    }
    
    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        if(!b)
        {
            memberListPanel.getMemberList().setRoleFilter(Arrays.stream(Role.values()).collect(Collectors.toSet()));
        }
        else
        {
            memberListPanel.getMemberList().updateListModel();
            setLocationRelativeTo(null);
        }
    }
    
    private static class DateTimePickerPanel extends JPanel
    {
        private JLabel title;
        private JComboBox<String> monthDropdown;
        private JTextField dayField;
        private JTextField yearField;
        private JTextField hourField;
        private JTextField minuteField;
        private JComboBox<String> amPmDropdown;
        private JLabel timeColonLabel;
        
        public DateTimePickerPanel(String titleText)
        {
            title = new JLabel(titleText);
            monthDropdown = new JComboBox<>();
            dayField = new JTextField("1");
            yearField = new JTextField("2023");
            hourField = new JTextField("12");
            minuteField = new JTextField("00");
            amPmDropdown = new JComboBox<>();
            timeColonLabel = new JLabel(":");
    
            monthDropdown.addItem("Jan");
            monthDropdown.addItem("Feb");
            monthDropdown.addItem("Mar");
            monthDropdown.addItem("Apr");
            monthDropdown.addItem("May");
            monthDropdown.addItem("Jun");
            monthDropdown.addItem("Jul");
            monthDropdown.addItem("Aug");
            monthDropdown.addItem("Sep");
            monthDropdown.addItem("Oct");
            monthDropdown.addItem("Nov");
            monthDropdown.addItem("Dec");
            monthDropdown.setEditable(false);
            
            dayField.setColumns(2);
            
            yearField.setColumns(4);
            
            hourField.setColumns(2);
            
            minuteField.setColumns(2);
            
            amPmDropdown.addItem("AM");
            amPmDropdown.addItem("PM");
            amPmDropdown.setEditable(false);
            
            setLayout(new MigLayout("fillx, insets 4", "[][][][][][][]","[][grow]"));
            add(title, "cell 0 0, spanx");
            add(monthDropdown, "cell 0 1");
            add(dayField, "cell 1 1");
            add(yearField, "cell 2 1");
            add(hourField, "cell 3 1");
            add(timeColonLabel, "cell 4 1");
            add(minuteField, "cell 5 1");
            add(amPmDropdown, "cell 6 1");
        }
        
        public OffsetDateTime getSelectedTime()
        {
            return LocalDateTime.parse(String.format("%s %02d %s %02d:%02d %s",
                                                     monthDropdown.getSelectedItem(),
                                                     Integer.parseInt(dayField.getText()),
                                                     yearField.getText(),
                                                     Integer.parseInt(hourField.getText()),
                                                     Integer.parseInt(minuteField.getText()),
                                                     amPmDropdown.getSelectedItem()),
                                       DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a"))
                                .atOffset(OffsetDateTime.now().getOffset());
        }
        
        public boolean isTimeValid()
        {
            try
            {
                OffsetDateTime time = getSelectedTime();
                return true;
            }
            catch(DateTimeParseException e)
            {
                return false;
            }
        }
    }
}
