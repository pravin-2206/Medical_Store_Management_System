package medicalstore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.mysql.cj.util.StringUtils;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SalesModule extends JFrame {

    private JTextField txtCustomerName, txtAddress, txtPhone, txtEmail, txtQty, txtPrice, txtGst, txtTotal;
    private JSpinner txtDate;
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbPaymentMode, cmbProductType, cmbProductName;

    Connection con = DBConnection.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;

    public SalesModule() {
        setTitle("Sales Module");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        txtCustomerName = new JTextField();
        txtAddress = new JTextField();
        txtPhone = new JTextField();
        txtEmail = new JTextField();
        txtQty = new JTextField();
        txtPrice = new JTextField();
        txtGst = new JTextField();
        txtTotal = new JTextField();

        txtGst.setEditable(false);
        txtTotal.setEditable(false);

        // --- ComboBoxes ---
        String[] productTypes = {"Tablet", "Syrup", "Injection", "Cream"};
        cmbProductType = new JComboBox<>(productTypes);

        txtDate = new JSpinner(new SpinnerDateModel());
        txtDate.setEditor(new JSpinner.DateEditor(txtDate, "dd/MM/yyyy"));

        String[] paymentModes = {"Online", "Cash", "Card"};
        cmbPaymentMode = new JComboBox<>(paymentModes);

        // === Load Product Names from purchase table ===
        List<String> productList = new ArrayList<>();
        try {
            ps = con.prepareStatement("SELECT DISTINCT product_name FROM purchase");
            rs = ps.executeQuery();
            while (rs.next()) {
                productList.add(rs.getString("product_name"));
            }
        } catch (Exception ex) {
            System.out.println("Error loading product names: " + ex);
        } finally {
            closeResources();
        }
        cmbProductName = new JComboBox<>(productList.toArray(new String[0]));

        // === Auto-fill Customer Details ===
        txtCustomerName.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                handleCustomerNameChange();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                handleCustomerNameChange();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                handleCustomerNameChange();
            }
            
            private void handleCustomerNameChange() {
                String customerName = txtCustomerName.getText().trim();
                
                // Immediately clear fields if name is empty
                if (customerName.isEmpty()) {
                    clearCustomerDetails();
                    return;
                }
                
                // Wait before searching for existing customer
                Timer timer = new Timer(500, e -> {
                    SwingUtilities.invokeLater(() -> {
                        if (!customerName.equals(txtCustomerName.getText().trim())) {
                            return; // Text has changed, ignore this request
                        }
                        searchAndFillCustomerDetails(customerName);
                    });
                });
                timer.setRepeats(false);
                timer.start();
            }
        });

        // === Auto GST Calculation ===
        txtQty.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateGST(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateGST(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateGST(); }
        });
        txtPrice.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateGST(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateGST(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateGST(); }
        });

        // === Form ===
        JPanel form = new JPanel(new GridLayout(6, 3, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("New Sale"));

        form.add(labeled("Customer Name", txtCustomerName));
        form.add(labeled("Address", txtAddress));
        form.add(labeled("Phone", txtPhone));
        form.add(labeled("Email", txtEmail));
        form.add(labeled("Product Name", cmbProductName));
        form.add(labeled("Product Type", cmbProductType));
        form.add(labeled("Quantity", txtQty));
        form.add(labeled("Price", txtPrice));
        form.add(labeled("Sale Date", txtDate));
        form.add(labeled("Payment Mode", cmbPaymentMode));
        form.add(labeled("GST (5%)", txtGst));
        form.add(labeled("Total (with GST)", txtTotal));

        // === Table ===
        model = new DefaultTableModel(
            new String[]{"Customer", "Phone", "Product", "Qty", "Price", "GST", "Total", "Date", "Payment"}, 0
        );
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Sales Records"));

        // === Buttons ===
        JPanel actions = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        JButton btnCancel = new JButton("Cancel");

        actions.add(btnSave);
        actions.add(btnClear);
        actions.add(btnCancel);

        // === Save Action ===
        btnSave.addActionListener(e -> saveSale());
        btnClear.addActionListener(e -> clearForm());
        btnCancel.addActionListener(e -> dispose());

        // === Layout ===
        setLayout(new BorderLayout(10, 10));
        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        refreshDataTable();
    }

    // Add this method to clear customer details
    private void clearCustomerDetails() {
        SwingUtilities.invokeLater(() -> {
            txtAddress.setText("");
            txtPhone.setText("");
            txtEmail.setText("");
        });
    }

    private void searchAndFillCustomerDetails(String customerName) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // Search for customer by name
            String sql = "SELECT phone_no, address, email FROM customer WHERE customer_name = ? ORDER BY created_date DESC LIMIT 1";
            ps = con.prepareStatement(sql);
            ps.setString(1, customerName);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                // Auto-fill the fields
                String phone = rs.getString("phone_no");
                String address = rs.getString("address");
                String email = rs.getString("email");
                
                // Use invokeLater to ensure thread safety
                SwingUtilities.invokeLater(() -> {
                    if (phone != null && !phone.trim().isEmpty()) {
                        txtPhone.setText(phone);
                    }
                    if (address != null && !address.trim().isEmpty()) {
                        txtAddress.setText(address);
                    }
                    if (email != null && !email.trim().isEmpty()) {
                        txtEmail.setText(email);
                    }
                });
            } else {
                // No customer found, clear the fields
                clearCustomerDetails();
            }
        } catch (SQLException ex) {
            System.out.println("Error searching customer: " + ex.getMessage());
            // Clear fields on error as well
            clearCustomerDetails();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
            }
        }
    }

    private void updateGST() {
        try {
            String qtyStr = txtQty.getText().trim();
            String priceStr = txtPrice.getText().trim();
            if (StringUtils.isNullOrEmpty(qtyStr) || StringUtils.isNullOrEmpty(priceStr)) return;
            int qty = Integer.parseInt(qtyStr);
            double price = Double.parseDouble(priceStr);
            calculateGstAmount(qty, price);
        } catch (Exception e) {
            txtGst.setText("");
            txtTotal.setText("");
        }
    }

    private void saveSale() {
        // Input validation
        if (!validateForm()) {
            return;
        }

        Connection conn = null;
        PreparedStatement custPs = null;
        PreparedStatement pp = null;
        PreparedStatement salesPs = null;
        ResultSet keys = null;
        ResultSet pr = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            String customer = txtCustomerName.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            String address = txtAddress.getText().trim();
            String product = (String) cmbProductName.getSelectedItem();
            String productType = (String) cmbProductType.getSelectedItem();
            String payment = (String) cmbPaymentMode.getSelectedItem();

            int qty = Integer.parseInt(txtQty.getText().trim());
            double price = Double.parseDouble(txtPrice.getText().trim());
            double gst = Double.parseDouble(txtGst.getText().trim());
            double total = Double.parseDouble(txtTotal.getText().trim());
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // === Check if customer already exists ===
            int customerId = getCustomerId(phone, conn);
            
            if (customerId == 0) {
                // === Insert Customer ===
                String custSql = "INSERT INTO customer(customer_name, address, phone_no, created_date, email) VALUES (?, ?, ?, ?, ?)";
                custPs = conn.prepareStatement(custSql, Statement.RETURN_GENERATED_KEYS);
                custPs.setString(1, customer);
                custPs.setString(2, address);
                custPs.setString(3, phone);
                custPs.setString(4, currentDate);
                custPs.setString(5, email);
                custPs.executeUpdate();

                keys = custPs.getGeneratedKeys();
                if (keys.next()) customerId = keys.getInt(1);
            }

            // === Get purchase_id and check stock ===
            int purchaseId = 0;
            pp = conn.prepareStatement("SELECT purchase_id, qty FROM purchase WHERE product_name=?");
            pp.setString(1, product);
            pr = pp.executeQuery();
            if (pr.next()) {
                purchaseId = pr.getInt("purchase_id");
                int availableQty = pr.getInt("qty");
                
                // Check stock availability
                if (availableQty < qty) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + availableQty, "Stock Error", JOptionPane.ERROR_MESSAGE);
                    conn.rollback();
                    return;
                }
                
                // Update stock in purchase table
                PreparedStatement updateStock = conn.prepareStatement("UPDATE purchase SET qty = qty - ? WHERE purchase_id = ?");
                updateStock.setInt(1, qty);
                updateStock.setInt(2, purchaseId);
                updateStock.executeUpdate();
                updateStock.close();
            } else {
                JOptionPane.showMessageDialog(this, "Product not found in inventory!", "Error", JOptionPane.ERROR_MESSAGE);
                conn.rollback();
                return;
            }

            int userId = 1; // (If you have a login system, replace this)

            // === Insert into sales ===
            String sql = "INSERT INTO sales(sale_date, qty, selling_price, payment_mode, purchase_id, customer_id, user_id, gst, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            salesPs = conn.prepareStatement(sql);
            salesPs.setString(1, currentDate);
            salesPs.setInt(2, qty);
            salesPs.setDouble(3, price);
            salesPs.setString(4, payment);
            salesPs.setInt(5, purchaseId);
            salesPs.setInt(6, customerId);
            salesPs.setInt(7, userId);
            salesPs.setDouble(8, gst);
            salesPs.setDouble(9, total);

            int rows = salesPs.executeUpdate();
            if (rows > 0) {
                conn.commit();
                JOptionPane.showMessageDialog(this, "Sale added successfully!");
                clearForm();
                refreshDataTable();
            }
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error saving sale: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Close all resources
            closeResources();
            try {
                if (keys != null) keys.close();
                if (pr != null) pr.close();
                if (custPs != null) custPs.close();
                if (pp != null) pp.close();
                if (salesPs != null) salesPs.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean validateForm() {
        String customer = txtCustomerName.getText().trim();
        String phone = txtPhone.getText().trim();
        String qtyStr = txtQty.getText().trim();
        String priceStr = txtPrice.getText().trim();

        if (customer.isEmpty() || phone.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            double price = Double.parseDouble(priceStr);
            
            if (qty <= 0 || price <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity and Price must be greater than zero!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for quantity and price!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private int getCustomerId(String phone, Connection conn) {
        PreparedStatement checkStmt = null;
        ResultSet rs = null;
        try {
            String checkSql = "SELECT customer_id FROM customer WHERE phone_no = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, phone);
            rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("customer_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void clearForm() {
        txtCustomerName.setText("");
        txtAddress.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtQty.setText("");
        txtPrice.setText("");
        txtGst.setText("");
        txtTotal.setText("");
        if (cmbProductName.getItemCount() > 0) cmbProductName.setSelectedIndex(0);
        if (cmbPaymentMode.getItemCount() > 0) cmbPaymentMode.setSelectedIndex(0);
        if (cmbProductType.getItemCount() > 0) cmbProductType.setSelectedIndex(0);
    }

    private void refreshDataTable() {
        try {
            model.setRowCount(0);
            ps = con.prepareStatement(
                "SELECT s.sale_date, s.qty, s.selling_price, s.payment_mode, s.gst, s.total_amount, " +
                "c.customer_name, c.phone_no, p.product_name " +
                "FROM sales s " +
                "JOIN customer c ON s.customer_id = c.customer_id " +
                "JOIN purchase p ON s.purchase_id = p.purchase_id " +
                "ORDER BY s.sale_date DESC"
            );
            rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("customer_name"),
                    rs.getString("phone_no"),
                    rs.getString("product_name"),
                    rs.getInt("qty"),
                    rs.getDouble("selling_price"),
                    rs.getDouble("gst"),
                    rs.getDouble("total_amount"),
                    rs.getString("sale_date"),
                    rs.getString("payment_mode")
                });
            }
        } catch (Exception e) {
            System.out.println("Error loading data: " + e);
        } finally {
            closeResources();
        }
    }

    private void calculateGstAmount(int qty, double price) {
        double total = qty * price;
        double gst = total * 0.05;
        txtGst.setText(String.format("%.2f", gst));
        txtTotal.setText(String.format("%.2f", total + gst));
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
        } catch (SQLException ex) {
            System.out.println("Error closing resources: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SalesModule().setVisible(true));
    }
}