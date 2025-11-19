package medicalstore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;

public class PurchaseModule extends JFrame {
    
    Connection con = DBConnection.getConnection();    
    PreparedStatement ps = null;
    ResultSet rs=null;
    
    JTextField txtPO = new JTextField();  
    JTextField txtProductName = new JTextField(); 
    JTextField txtQty = new JTextField();
    JTextField txtMRP = new JTextField();

    JComboBox<String> cmbSupplier;
    
    String[] productTypes = {"Syrup", "Tablet"};
    JComboBox<String> cmbProductType = new JComboBox<>(productTypes);
    
    JSpinner txtMFG;
    JSpinner txtExpiry;
    
    // Declare model and table as instance variables
    private DefaultTableModel model;
    private JTable table;
    
    public PurchaseModule() {
        setTitle("Purchase Module");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ===== Form Panel =====
        JPanel form = new JPanel(new GridLayout(4, 3, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Purchase Order"));

        // Fix date spinners initialization
        txtMFG = new JSpinner(new SpinnerDateModel());
        txtMFG.setEditor(new JSpinner.DateEditor(txtMFG, "dd/MM/yyyy"));

        txtExpiry = new JSpinner(new SpinnerDateModel());
        txtExpiry.setEditor(new JSpinner.DateEditor(txtExpiry, "MM/yyyy"));

        // Load suppliers
        loadSuppliers();
        
        form.add(labeled("PO No", txtPO));
        form.add(labeled("Supplier", cmbSupplier)); 
        form.add(labeled("Product Name", txtProductName));
        form.add(labeled("Product type", cmbProductType));
        form.add(labeled("Qty", txtQty));
        form.add(labeled("MRP", txtMRP));
        form.add(labeled("MFG", txtMFG));
        form.add(labeled("Expiry", txtExpiry));

        // ===== Table =====
        model = new DefaultTableModel(
                new String[]{"Product", "Qty", "MRP", "Expiry"}, 0);
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Purchased Items"));

        // ===== Action Buttons =====
        JPanel actions = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        JButton btnCancel = new JButton("Cancel");

        actions.add(btnSave);
        actions.add(btnClear);
        actions.add(btnCancel);

        // Load existing purchase items when the module opens
        refreshDataTable();

        // ===== Save Action =====
        btnSave.addActionListener(e -> {
            String POText = txtPO.getText().trim();
            String product = txtProductName.getText().trim();
            String qtyText = txtQty.getText().trim();
            String mrpText = txtMRP.getText().trim();
            String productType = cmbProductType.getSelectedItem().toString();
            String supplier = cmbSupplier.getSelectedItem().toString();

            if (POText.isEmpty() || product.isEmpty() || qtyText.isEmpty() || mrpText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int qty = Integer.parseInt(qtyText);
                double mrp = Double.parseDouble(mrpText);
                
                // Format dates properly for database
                SimpleDateFormat mfgFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyy-MM-dd");
                
                String mfg = mfgFormat.format(txtMFG.getValue());
                String expiry = expiryFormat.format(txtExpiry.getValue());
                
                // Extract supplier ID
                int supplierId = extractSupplierId(supplier);
                if (supplierId == -1) {
                    JOptionPane.showMessageDialog(this, "Invalid supplier selected!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Use PreparedStatement with parameters to prevent SQL injection
                String insertPurchaseQuery = "INSERT INTO purchase(po_no, purchase_date, qty, mrp, mfg_date, exp_date, product_name, product_type, supplier_id, company_id, user_id) " +
                                           "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                ps = con.prepareStatement(insertPurchaseQuery);
                ps.setString(1, POText);
                ps.setTimestamp(2, new java.sql.Timestamp(new Date().getTime()));
                ps.setInt(3, qty);
                ps.setDouble(4, mrp);
                ps.setString(5, mfg);
                ps.setString(6, expiry);
                ps.setString(7, product);
                ps.setString(8, productType);
                ps.setInt(9, supplierId);
                ps.setInt(10, 1); // company_id
                ps.setInt(11, 1); // user_id

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    model.addRow(new Object[]{product, qty, mrp, expiry});
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                    clearForm();
                    refreshDataTable(); // Refresh table to show all items including new one
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity or price format!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving purchase: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                closeResources();
            }
        });

        // ===== Clear Action =====
        btnClear.addActionListener(e -> {
            clearForm();
            model.setRowCount(0); // Clear table rows
            refreshDataTable(); // Reload data from database after clear
        });

        // ===== Cancel Action =====
        btnCancel.addActionListener(e -> dispose());

        // ===== Layout =====
        setLayout(new BorderLayout(10, 10));
        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void loadSuppliers() {
        try {
            String selectQuery = "SELECT supplier_id, supplier_name FROM supplier";
            ps = con.prepareStatement(selectQuery);                     
            rs = ps.executeQuery();

            List<String> suppliersData = new ArrayList<String>();
            
            while (rs.next()) {	
                suppliersData.add(rs.getString("supplier_name") + " (ID: " + rs.getInt("supplier_id") + ")");
            } 
            
            String suppliers[] = new String[suppliersData.size()];
            for(int i = 0; i < suppliersData.size(); i++) {
                suppliers[i] = suppliersData.get(i);
            }
            
            cmbSupplier = new JComboBox<>(suppliers);
              
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println(ex);
        } finally {
            closeResources();
        }
    }

    private void refreshDataTable() {
        try {
            model.setRowCount(0); // Clear existing rows
            
            String selectQuery = "SELECT product_name, qty, mrp, exp_date FROM purchase ORDER BY purchase_date DESC";
            ps = con.prepareStatement(selectQuery);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                String product = rs.getString("product_name");
                int qty = rs.getInt("qty");
                double mrp = rs.getDouble("mrp");
                String expiry = rs.getString("exp_date");
                
                model.addRow(new Object[]{product, qty, mrp, expiry});
            }
        } catch (Exception ex) {
            System.out.println("Error loading purchase data: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading purchase data!", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            closeResources();
        }
    }

    private int extractSupplierId(String supplierText) {
        try {
            // Extract supplier ID from text like "Supplier Name (ID: 123)"
            int startIndex = supplierText.lastIndexOf("(ID: ") + 5;
            int endIndex = supplierText.lastIndexOf(")");
            String idStr = supplierText.substring(startIndex, endIndex);
            return Integer.parseInt(idStr.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private void clearForm() {
        txtPO.setText(""); 
        txtProductName.setText("");
        txtQty.setText("");
        txtMRP.setText("");
        txtMFG.setValue(new Date());
        txtExpiry.setValue(new Date());
        if (cmbSupplier != null && cmbSupplier.getItemCount() > 0) {
            cmbSupplier.setSelectedIndex(0);
        }
        if (cmbProductType != null && cmbProductType.getItemCount() > 0) {
            cmbProductType.setSelectedIndex(0);
        }
    }

    private void closeResources() {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
        } catch (Exception ex) {
            System.out.println("Error closing resources: " + ex.getMessage());
        }
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PurchaseModule().setVisible(true));
    }
}