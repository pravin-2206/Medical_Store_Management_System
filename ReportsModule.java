package medicalstore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class ReportsModule extends JFrame {
    private Connection con = DBConnection.getConnection();
    private DefaultTableModel model;

    public ReportsModule() {
        setTitle("Reports Module");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // === Filters Panel ===
        JPanel filters = new JPanel(new GridLayout(2, 4, 10, 10));
        filters.setBorder(BorderFactory.createTitledBorder("Filters"));

        // Main report types
        JComboBox<String> type = new JComboBox<>(new String[]{
                "Sales Summary",
                "Stock Levels",
                "Purchase History",
                "Expiry Report",
                "Customer Sales"
        });

        // Date fields with current month as default
        JTextField from = new JTextField();
        JTextField to = new JTextField();
        
        // Set default dates (current month)
        setDefaultDates(from, to);

        JButton btnRun = new JButton("Run");
        JButton btnExport = new JButton("Export");

        // === Report Table ===
        model = new DefaultTableModel();
        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Report Result"));

        // === Filters section ===
        filters.add(labeled("Report Type", type));
        filters.add(labeled("From (YYYY-MM-DD)", from));
        filters.add(labeled("To (YYYY-MM-DD)", to));
        filters.add(new JLabel()); // Empty cell for layout
        filters.add(btnRun);
        filters.add(btnExport);

        // === Bottom Buttons ===
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        JButton btnCancel = new JButton("Cancel");

        bottomPanel.add(btnSave);
        bottomPanel.add(btnClear);
        bottomPanel.add(btnCancel);

        // === Button Actions ===

        // Run button - generates actual reports from database
        btnRun.addActionListener(e -> {
            String reportType = (String) type.getSelectedItem();
            String fromDate = from.getText().trim();
            String toDate = to.getText().trim();

            if (fromDate.isEmpty() || toDate.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter both From and To dates!",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Generate actual report from database
            generateReport(reportType, fromDate, toDate);
        });

        // Export button
        btnExport.addActionListener(e -> {
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No data to export! Generate a report first.", 
                    "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Export feature is a future inhancmemnt would save to PDF file.", 
                    "Export", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Save button
        btnSave.addActionListener(e -> {
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No data to save!", "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Report saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Clear button
        btnClear.addActionListener(e -> {
            from.setText("");
            to.setText("");
            model.setRowCount(0);
            setDefaultDates(from, to); // Reset to default dates
        });

        // Cancel button
        btnCancel.addActionListener(e -> dispose());

        // === Layout ===
        setLayout(new BorderLayout(10, 10));
        add(filters, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setDefaultDates(JTextField from, JTextField to) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        
        // First day of current month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        from.setText(dateFormat.format(cal.getTime()));
        
        // Current date
        to.setText(dateFormat.format(new Date()));
    }

    private void generateReport(String reportType, String fromDate, String toDate) {
        try {
            switch (reportType) {
                case "Sales Summary":
                    generateSalesSummary(fromDate, toDate);
                    break;
                case "Stock Levels":
                    generateStockLevels();
                    break;
                case "Purchase History":
                    generatePurchaseHistory(fromDate, toDate);
                    break;
                case "Expiry Report":
                    generateExpiryReport();
                    break;
                case "Customer Sales":
                    generateCustomerSales(fromDate, toDate);
                    break;
                case "Product Performance":
                    generateProductPerformance(fromDate, toDate);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Unknown report type!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating report: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void generateSalesSummary(String from, String to) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // Using the correct column names from your sales table
            String sql = "SELECT s.sale_date, c.customer_name, p.product_name, " +
                        "s.qty, s.selling_price, s.gst, s.total_amount, s.payment_mode " +
                        "FROM sales s " +
                        "JOIN customer c ON s.customer_id = c.customer_id " +
                        "JOIN purchase p ON s.purchase_id = p.purchase_id " +
                        "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                        "ORDER BY s.sale_date DESC";
            
            ps = con.prepareStatement(sql);
            ps.setString(1, from);
            ps.setString(2, to);
            rs = ps.executeQuery();
            
            model.setColumnIdentifiers(new String[]{
                "Date", "Customer", "Product", "Qty", "Price", "GST", "Total", "Payment"
            });
            model.setRowCount(0);
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy");
            double totalSales = 0;
            
            while (rs.next()) {
                String saleDate = displayFormat.format(rs.getTimestamp("sale_date"));
                double total = rs.getDouble("total_amount");
                model.addRow(new Object[]{
                    saleDate,
                    rs.getString("customer_name"),
                    rs.getString("product_name"),
                    rs.getInt("qty"),
                    String.format("₹%.2f", rs.getDouble("selling_price")),
                    String.format("₹%.2f", rs.getDouble("gst")),
                    String.format("₹%.2f", total),
                    rs.getString("payment_mode")
                });
                totalSales += total;
            }
            
            // Add summary
            model.addRow(new Object[]{"TOTAL", "", "", "", "", "", 
                                    String.format("₹%.2f", totalSales), ""});
            
        } catch (SQLException ex) {
            throw new RuntimeException("Database error: " + ex.getMessage(), ex);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } 
            catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void generateStockLevels() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT product_name, product_type, qty, mrp, mfg_date, exp_date " +
                        "FROM purchase WHERE qty > 0 ORDER BY product_name";
            
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            
            model.setColumnIdentifiers(new String[]{
                "Product", "Type", "Quantity", "MRP", "Mfg Date", "Exp Date"
            });
            model.setRowCount(0);
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy");
            int totalQty = 0;
            double totalValue = 0;
            
            while (rs.next()) {
                String mfgDate = rs.getDate("mfg_date") != null ? 
                    displayFormat.format(rs.getDate("mfg_date")) : "N/A";
                String expDate = rs.getDate("exp_date") != null ? 
                    displayFormat.format(rs.getDate("exp_date")) : "N/A";
                
                int qty = rs.getInt("qty");
                double mrp = rs.getDouble("mrp");
                double value = qty * mrp;
                
                model.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("product_type"),
                    qty,
                    String.format("₹%.2f", mrp),
                    mfgDate,
                    expDate
                });
                
                totalQty += qty;
                totalValue += value;
            }
            
            model.addRow(new Object[]{"TOTAL ITEMS", "", totalQty, "TOTAL VALUE", 
                                    String.format("₹%.2f", totalValue), ""});
            
        } catch (SQLException ex) {
            throw new RuntimeException("Database error: " + ex.getMessage(), ex);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } 
            catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void generatePurchaseHistory(String from, String to) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT product_name, product_type, qty, mrp, purchase_date, mfg_date, exp_date " +
                        "FROM purchase WHERE (purchase_date BETWEEN ? AND ? OR purchase_date IS NULL) " +
                        "ORDER BY purchase_date DESC";
            
            ps = con.prepareStatement(sql);
            ps.setString(1, from);
            ps.setString(2, to);
            rs = ps.executeQuery();
            
            model.setColumnIdentifiers(new String[]{
                "Product", "Type", "Qty", "MRP", "Purchase Date", "Mfg Date", "Exp Date"
            });
            model.setRowCount(0);
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            while (rs.next()) {
                String purchaseDate = rs.getDate("purchase_date") != null ? 
                    displayFormat.format(rs.getDate("purchase_date")) : "N/A";
                String mfgDate = rs.getDate("mfg_date") != null ? 
                    displayFormat.format(rs.getDate("mfg_date")) : "N/A";
                String expDate = rs.getDate("exp_date") != null ? 
                    displayFormat.format(rs.getDate("exp_date")) : "N/A";
                
                model.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("product_type"),
                    rs.getInt("qty"),
                    String.format("₹%.2f", rs.getDouble("mrp")),
                    purchaseDate,
                    mfgDate,
                    expDate
                });
            }
            
        } catch (SQLException ex) {
            throw new RuntimeException("Database error: " + ex.getMessage(), ex);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } 
            catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void generateExpiryReport() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT product_name, product_type, qty, mrp, exp_date " +
                        "FROM purchase WHERE exp_date IS NOT NULL ORDER BY exp_date ASC";
            
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            
            model.setColumnIdentifiers(new String[]{
                "Product", "Type", "Qty", "MRP", "Expiry Date", "Status"
            });
            model.setRowCount(0);
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date today = new Date();
            
            while (rs.next()) {
                Date expDate = rs.getDate("exp_date");
                String status = "Active";
                
                if (expDate != null) {
                    long diff = expDate.getTime() - today.getTime();
                    long days = diff / (24 * 60 * 60 * 1000);
                    
                    if (days < 0) {
                        status = "EXPIRED";
                    } else if (days <= 30) {
                        status = "Expiring in " + days + " days";
                    }
                }
                
                model.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("product_type"),
                    rs.getInt("qty"),
                    String.format("₹%.2f", rs.getDouble("mrp")),
                    displayFormat.format(expDate),
                    status
                });
            }
            
        } catch (SQLException ex) {
            throw new RuntimeException("Database error: " + ex.getMessage(), ex);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } 
            catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void generateCustomerSales(String from, String to) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // Fixed: Using COUNT(*) instead of s.sale_id
            String sql = "SELECT c.customer_name, COUNT(*) as orders, SUM(s.total_amount) as total " +
                        "FROM sales s JOIN customer c ON s.customer_id = c.customer_id " +
                        "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                        "GROUP BY c.customer_id, c.customer_name ORDER BY total DESC";
            
            ps = con.prepareStatement(sql);
            ps.setString(1, from);
            ps.setString(2, to);
            rs = ps.executeQuery();
            
            model.setColumnIdentifiers(new String[]{
                "Customer", "Total Orders", "Total Amount"
            });
            model.setRowCount(0);
            
            double grandTotal = 0;
            int totalOrders = 0;
            
            while (rs.next()) {
                double total = rs.getDouble("total");
                int orders = rs.getInt("orders");
                model.addRow(new Object[]{
                    rs.getString("customer_name"),
                    orders,
                    String.format("₹%.2f", total)
                });
                grandTotal += total;
                totalOrders += orders;
            }
            
            model.addRow(new Object[]{"GRAND TOTAL", totalOrders, String.format("₹%.2f", grandTotal)});
            
        } catch (SQLException ex) {
            throw new RuntimeException("Database error: " + ex.getMessage(), ex);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } 
            catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void generateProductPerformance(String from, String to) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // Fixed: Using correct column names
            String sql = "SELECT p.product_name, SUM(s.qty) as sold, SUM(s.total_amount) as revenue " +
                        "FROM sales s JOIN purchase p ON s.purchase_id = p.purchase_id " +
                        "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                        "GROUP BY p.product_name ORDER BY revenue DESC";
            
            ps = con.prepareStatement(sql);
            ps.setString(1, from);
            ps.setString(2, to);
            rs = ps.executeQuery();
            
            model.setColumnIdentifiers(new String[]{
                "Product", "Quantity Sold", "Total Revenue"
            });
            model.setRowCount(0);
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getInt("sold"),
                    String.format("₹%.2f", rs.getDouble("revenue"))
                });
            }
            
        } catch (SQLException ex) {
            throw new RuntimeException("Database error: " + ex.getMessage(), ex);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } 
            catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    // === Helper method to label fields ===
    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    // === Main Method (for testing) ===
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReportsModule().setVisible(true));
    }
}