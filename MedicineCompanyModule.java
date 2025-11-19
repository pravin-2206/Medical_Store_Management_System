package medicalstore.masterdata;

import javax.swing.*;


import javax.swing.table.DefaultTableModel;

import medicalstore.DBConnection;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MedicineCompanyModule extends JFrame {

    Connection con = DBConnection.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;

    public MedicineCompanyModule() {
        setTitle("Medicine Company Module");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // === Form Panel ===
        JPanel form = new JPanel(new GridLayout(2, 1, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Medicine Company Details"));

        JTextField txtid = new JTextField();
        form.add(labeled("Company id", txtid));
        JTextField txtName = new JTextField();
        form.add(labeled("Company Name", txtName));

        // === Table ===
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Company ID", "Company Name"}, 0
        );
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Medicine Company Records"));

        // === Buttons ===
        JPanel actions = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        JButton btnCancel = new JButton("Cancel");

        // === Save Action ===
        btnSave.addActionListener(e -> {
            String id = txtid.getText().trim();
            String name = txtName.getText().trim();

            if (!name.isEmpty()) {
                try { 
                    String sql = "INSERT INTO medicine_company (company_name) VALUES ('"+name+"')";
                    ps = con.prepareStatement(sql);  
                    
                    int rowsAffected = ps.executeUpdate();

                    if (rowsAffected > 0) {
                        model.addRow(new Object[]{id, name});
                        JOptionPane.showMessageDialog(this, "Company added successfully!");
                        clearForm(txtid, txtName); // Clear the form fields
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to add the company.");
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace(); // Print detailed exception info for debugging
                    JOptionPane.showMessageDialog(this, "SQL Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Company Name is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        // === Clear Action ===
        btnClear.addActionListener(e -> clearForm(txtid, txtName));

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
        
        refreshDataTable(model);
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void clearForm(JTextField txtid, JTextField txtName) {
        txtid.setText("");
        txtName.setText("");
    }

    private void refreshDataTable(DefaultTableModel model) {
        try {
            // Clear existing rows before refreshing the table
            //model.setRowCount(0);

            // Fetch all records from the database
            String selectQuery = "SELECT * FROM medicine_company";
            ps = con.prepareStatement(selectQuery);
            rs = ps.executeQuery();

            // Add rows to the table model
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("company_id"), rs.getString("company_name")});
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MedicineCompanyModule().setVisible(true));
    }
}
