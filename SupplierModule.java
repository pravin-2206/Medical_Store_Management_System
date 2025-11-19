package medicalstore.masterdata;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import medicalstore.DBConnection;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SupplierModule extends JFrame {
	
	Connection con = DBConnection.getConnection();    
    PreparedStatement ps = null;
    ResultSet rs=null;
    
    JTextField txtSupplierName = new JTextField(); 
    JTextField txtAddress = new JTextField();
    JTextField txtPhone = new JTextField();
    JTextField txtEmail = new JTextField();
	
    public SupplierModule() {
        setTitle("Supplier Module");
        setSize(800, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // === Form Panel ===
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Supplier Details"));


        form.add(labeled("First Name", txtSupplierName)); 
        form.add(labeled("Address", txtAddress));
        form.add(labeled("Phone", txtPhone));
        form.add(labeled("Email", txtEmail));

        // === Table ===
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Supplier Name", "Address", "Phone", "Email"}, 0
        );
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Supplier Records"));

        // === Action Buttons ===
        JPanel actions = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        JButton btnCancel = new JButton("Cancel");
        
        refreshDataTable(model);

        // === Save Action ===
        btnSave.addActionListener(e -> {
            String supplierName = txtSupplierName.getText().trim(); 
            String address = txtAddress.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();

            if (supplierName.isEmpty() && address.isEmpty() && phone.isEmpty() && email.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Supplier name, address,and phone no,email are required!",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            } else {
           	 
                // Try block to check if exception/s occurs
                try {
                	String sql="insert into supplier(supplier_name,address,phone_no,email) "
                			+ "values('"+supplierName+"','"+address+"','"+phone+"','"+email+"')";
                    ps =con.prepareStatement(sql);                     
                    int rowsAffected = ps.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Supplier added successfully!");
    			        model.addRow(new Object[]{supplierName, address, phone, email});
    			        //clear form data
    			        clearForm();
                    } else {
                        System.out.println("Failed to add Supplier ");
                    }
					
                }catch (Exception ex) {

                    // Print the exception
                    System.out.println(ex);
                }
            }
            JOptionPane.showMessageDialog(this, "Supplier record added successfully!");
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

    // Helper to make labeled fields neatly
    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
    
    private void clearForm() {
        txtSupplierName.setText(""); 
        txtAddress.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
    }
    
    private void refreshDataTable(DefaultTableModel model) {
   	 try {
          	 String selectQuery="select * from supplier";
               ps =con.prepareStatement(selectQuery);                     
               rs = ps.executeQuery();

               // Condition check
   			  while (rs.next()) {		
   			        model.addRow(new Object[]{rs.getString("supplier_name"), rs.getString("address"), rs.getString("email"),rs.getString("phone_no")});
   			  }
   			 
           }catch (Exception ex) {
               // Print the exception
               System.out.println(ex);
           }
           
   }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SupplierModule().setVisible(true));
    }
}
