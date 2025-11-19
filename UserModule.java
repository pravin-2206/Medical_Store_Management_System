package medicalstore.masterdata;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import medicalstore.DBConnection;
import medicalstore.Dashboard;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserModule extends JFrame {

	Connection con = DBConnection.getConnection();    
    PreparedStatement ps = null;
    ResultSet rs=null;
    
    JTextField txtUsername = new JTextField();
    JPasswordField txtPassword = new JPasswordField();
    JTextField txtFirstName = new JTextField();
    JTextField txtLastName = new JTextField();
    JTextField txtAddress = new JTextField();
    JTextField txtEmail = new JTextField();
    JTextField txtPhone = new JTextField();
    String[] roles = {"Admin", "Pharmacist"};
    JComboBox<String> cmbRole = new JComboBox<>(roles);
    
    public UserModule() {
        setTitle("User Module");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // === Form Panel ===
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("User Details"));

      
        // Add labeled fields
        form.add(labeled("User Name", txtUsername));
        form.add(labeled("Password", txtPassword));
        form.add(labeled("First Name", txtFirstName));
        form.add(labeled("Last Name", txtLastName));
        form.add(labeled("Address", txtAddress));
        form.add(labeled("Role", cmbRole));
        form.add(labeled("Email ID", txtEmail));
        form.add(labeled("Phone No", txtPhone));

        // === Table ===
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"User Name", "First Name", "Last Name", "Address", "Email ID", "Phone No","Role"}, 0
        );
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("User Records"));

        // === Action Buttons ===
        JPanel actions = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        JButton btnCancel = new JButton("Cancel");

        refreshDataTable(model);
        
                
        // === Save Action ===
        btnSave.addActionListener(e -> {
            String userName = txtUsername.getText().trim();
            String userPass = new String(txtPassword.getPassword()).trim();
            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim(); 
            String address = txtAddress.getText().trim();
            String role = (String) cmbRole.getSelectedItem();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();

            if (userName.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "User Name, First Name, and Last Name are required!",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            else {
            	 
                 // Try block to check if exception/s occurs
                 try {
                	 String query="insert into users(user_name, password, first_name, last_name, email,phone_no, address, role)"
                	 		+ " values('"+userName+"','"+userPass+"','"+firstName+"','"+lastName+"','"+email+"','"+phone+"','"+address+"','"+role+"')";
                     ps =con.prepareStatement(query);                     
                     int rowsAffected = ps.executeUpdate();

                     if (rowsAffected > 0) {
                         System.out.println("User registered successfully!");
                         // You can add JOptionPane.showMessageDialog(this, "...") here for your UI
     			        model.addRow(new Object[]{userName, firstName, lastName, address, email, phone,role});
     			        //clear form data
     			        clearForm();
     		           JOptionPane.showMessageDialog(this, "User record added successfully!");

                     } else {
       		           JOptionPane.showMessageDialog(this, "Failed to register the user"); 
                     }
					
                 }catch (Exception ex) {

                     // Print the exception
                     System.out.println(ex);
                     clearForm();
                     JOptionPane.showMessageDialog(this, "This username or password is already exist!");

                 }
            }
        });

        // === Clear Action ===
      btnClear.addActionListener(e -> {
    	  clearForm();
        });

        // === Cancel Action ===
        btnCancel.addActionListener(e -> dispose());

        actions.add(btnSave);
        actions.add(btnClear);
        actions.add(btnCancel);

        // === Layout ===
        setLayout(new BorderLayout(10, 10));
        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    // === Helper method ===
    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
    private void clearForm() {
    	 txtUsername.setText("");
         txtPassword.setText("");
         txtFirstName.setText("");
         txtLastName.setText("");
         txtAddress.setText("");
         txtEmail.setText("");
         txtPhone.setText("");
         cmbRole.setSelectedIndex(0);
    }
    private void refreshDataTable(DefaultTableModel model) {
    	 try {
           	 String selectQuery="select * from users";
                ps =con.prepareStatement(selectQuery);                     
                rs = ps.executeQuery();

                // Condition check
    			  while (rs.next()) {		
    			        model.addRow(new Object[]{rs.getString("user_name"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("address"), rs.getString("email"),rs.getString("phone_no"),rs.getString("role")});
    			  }
    			 
            }catch (Exception ex) {
                // Print the exception
                System.out.println(ex);
            }
            
    }

    // === Main method for testing ===
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserModule().setVisible(true));
    }
}