package medicalstore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReturnModule extends JFrame {
    private Connection con = DBConnection.getConnection();
    private DefaultTableModel model;

    public ReturnModule() {
        setTitle("Product Return Module");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // === Form Panel ===
        JPanel form = new JPanel(new GridLayout(2, 4, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Return Product"));

        JTextField txtCustomerName = new JTextField();
        JTextField txtProductName = new JTextField();
        JTextField txtQuantity = new JTextField();
        JTextField txtReason = new JTextField();
        JComboBox<String> cmbReturnType = new JComboBox<>(new String[]{"Defective", "Expired", "Wrong Product", "Customer Change Mind"});
        JSpinner txtReturnDate = new JSpinner(new SpinnerDateModel());
        txtReturnDate.setEditor(new JSpinner.DateEditor(txtReturnDate, "dd/MM/yyyy"));

        form.add(labeled("Customer Name *", txtCustomerName));
        form.add(labeled("Product Name *", txtProductName));
        form.add(labeled("Quantity *", txtQuantity));
        form.add(labeled("Return Type", cmbReturnType));
        form.add(labeled("Reason", txtReason));
        form.add(labeled("Return Date", txtReturnDate));

        // === Table ===
        model = new DefaultTableModel(
                new String[]{"Customer Name", "Product Name", "Quantity", "Return Type", "Reason", "Return Date"}, 0
        );
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Return History"));

        // === Action Buttons ===
        JPanel actions = new JPanel();
        JButton btnProcessReturn = new JButton("Process Return");
        JButton btnClear = new JButton("Clear Form");
        JButton btnCancel = new JButton("Cancel");

        // Process Return Action
        btnProcessReturn.addActionListener(e -> {
            try {
                String customerName = txtCustomerName.getText().trim();
                String productName = txtProductName.getText().trim();
                int quantity = Integer.parseInt(txtQuantity.getText().trim());
                String returnType = cmbReturnType.getSelectedItem().toString();
                String reason = txtReason.getText().trim();
                String returnDate = new SimpleDateFormat("yyyy-MM-dd").format(txtReturnDate.getValue());

                // Validation
                if (customerName.isEmpty() || productName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Customer Name and Product Name are required!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than zero!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Process return in database
                if (processReturn(customerName, productName, quantity, returnType, reason, returnDate)) {
                    // Add to table
                    String displayDate = new SimpleDateFormat("dd/MM/yyyy").format(txtReturnDate.getValue());
                    model.addRow(new Object[]{customerName, productName, quantity, returnType, reason, displayDate});
                    
                    JOptionPane.showMessageDialog(this, "Return processed successfully!");
                    clearForm(txtCustomerName, txtProductName, txtQuantity, txtReason, cmbReturnType, txtReturnDate);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid quantity!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error processing return: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Clear form
        btnClear.addActionListener(e -> {
            clearForm(txtCustomerName, txtProductName, txtQuantity, txtReason, cmbReturnType, txtReturnDate);
        });

        btnCancel.addActionListener(e -> dispose());

        actions.add(btnProcessReturn);
        actions.add(btnClear);
        actions.add(btnCancel);

        // === Layout ===
        setLayout(new BorderLayout(10, 10));
        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        // Load return history
        loadReturnHistory();
    }

    private boolean processReturn(String customerName, String productName, int quantity, 
                                 String returnType, String reason, String returnDate) {
        PreparedStatement ps = null;
        try {
            // Insert into returns table (you might need to create this table)
            String sql = "INSERT INTO returns (customer_name, product_name, quantity, return_type, reason, return_date) VALUES (?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, customerName);
            ps.setString(2, productName);
            ps.setInt(3, quantity);
            ps.setString(4, returnType);
            ps.setString(5, reason);
            ps.setString(6, returnDate);
            
            int rows = ps.executeUpdate();
            return rows > 0;
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadReturnHistory() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            model.setRowCount(0);
            
            // Assuming you have a 'returns' table
            String sql = "SELECT customer_name, product_name, quantity, return_type, reason, return_date FROM returns ORDER BY return_date DESC";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            while (rs.next()) {
                String returnDate = displayFormat.format(rs.getDate("return_date"));
                model.addRow(new Object[]{
                    rs.getString("customer_name"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getString("return_type"),
                    rs.getString("reason"),
                    returnDate
                });
            }
            
        } catch (SQLException ex) {
            // If returns table doesn't exist, just continue without history
            System.out.println("Returns table might not exist: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void clearForm(JTextField txtCustomerName, JTextField txtProductName, JTextField txtQuantity,
                          JTextField txtReason, JComboBox<String> cmbReturnType, JSpinner txtReturnDate) {
        txtCustomerName.setText("");
        txtProductName.setText("");
        txtQuantity.setText("");
        txtReason.setText("");
        cmbReturnType.setSelectedIndex(0);
        txtReturnDate.setValue(new Date());
        txtCustomerName.requestFocus();
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReturnModule().setVisible(true));
    }
}