package medicalstore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StockModule extends JFrame {
    private Connection con = DBConnection.getConnection();
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    public StockModule() {
        setTitle("Stock Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // === Search Bar ===
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtSearch = new JTextField(25);
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");
        search.add(new JLabel("Search:"));
        search.add(txtSearch);
        search.add(btnSearch);
        search.add(btnRefresh);

        // === Form Panel ===
        JPanel form = new JPanel(new GridLayout(3, 3, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Add/Update Stock"));

        JTextField txtProductName = new JTextField();
        JTextField txtQty = new JTextField();
        JTextField txtMRP = new JTextField();

        // Product Type
        String[] productTypes = {"Tablet", "Syrup", "Injection", "Capsule", "Cream"};
        JComboBox<String> cmbProductType = new JComboBox<>(productTypes);

        // === Date Pickers ===
        JSpinner txtPurchaseDate = new JSpinner(new SpinnerDateModel());
        txtPurchaseDate.setEditor(new JSpinner.DateEditor(txtPurchaseDate, "dd/MM/yyyy"));

        JSpinner txtMfgDate = new JSpinner(new SpinnerDateModel());
        txtMfgDate.setEditor(new JSpinner.DateEditor(txtMfgDate, "dd/MM/yyyy"));

        JSpinner txtExpDate = new JSpinner(new SpinnerDateModel());
        txtExpDate.setEditor(new JSpinner.DateEditor(txtExpDate, "dd/MM/yyyy"));

        // === Add components to form ===
        form.add(labeled("Product Name *", txtProductName));
        form.add(labeled("Product Type", cmbProductType));
        form.add(labeled("Quantity *", txtQty));
        form.add(labeled("MRP *", txtMRP));
        form.add(labeled("Purchase Date", txtPurchaseDate));
        form.add(labeled("Mfg Date", txtMfgDate));
        form.add(labeled("Exp Date", txtExpDate));

        // === Table ===
        model = new DefaultTableModel(
                new String[]{"ID", "Product Name", "Type", "Qty", "MRP", "Purchase Date", "Mfg Date", "Exp Date"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        // Hide ID column
        table.removeColumn(table.getColumnModel().getColumn(0));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Current Stock"));

        // Row sorter for search & filter
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // === Action Buttons ===
        JPanel actions = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        JButton btnCancel = new JButton("Cancel");
        JButton btnDelete = new JButton("Delete");

        // === Save Action ===
        btnSave.addActionListener(e -> {
            try {
                String product = txtProductName.getText().trim();
                String type = cmbProductType.getSelectedItem().toString();
                int qty = Integer.parseInt(txtQty.getText().trim());
                double mrp = Double.parseDouble(txtMRP.getText().trim());
                
                // Format dates for database
                SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                
                Date purchaseDateValue = (Date) txtPurchaseDate.getValue();
                Date mfgDateValue = (Date) txtMfgDate.getValue();
                Date expDateValue = (Date) txtExpDate.getValue();
                
                String purchaseDate = purchaseDateValue != null ? dbDateFormat.format(purchaseDateValue) : null;
                String mfgDate = mfgDateValue != null ? dbDateFormat.format(mfgDateValue) : null;
                String expDate = expDateValue != null ? dbDateFormat.format(expDateValue) : null;

                // Validation
                if (product.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Product name is required!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (qty <= 0 || mrp <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity and MRP must be greater than zero!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Save to database
                if (saveToDatabase(product, type, qty, mrp, purchaseDate, mfgDate, expDate)) {
                    refreshStockTable();
                    JOptionPane.showMessageDialog(this, "Stock added successfully!");
                    clearForm(txtProductName, txtQty, txtMRP, cmbProductType, txtPurchaseDate, txtMfgDate, txtExpDate);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // === Delete Action ===
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    int purchaseId = (int) model.getValueAt(modelRow, 0);
                    
                    if (deleteFromDatabase(purchaseId)) {
                        refreshStockTable();
                        JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting product: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        // === Clear Action ===
        btnClear.addActionListener(e -> {
            clearForm(txtProductName, txtQty, txtMRP, cmbProductType, txtPurchaseDate, txtMfgDate, txtExpDate);
        });

        // === Cancel Action ===
        btnCancel.addActionListener(e -> dispose());

        // === Search Action ===
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                try {
                    String safeKeyword = java.util.regex.Pattern.quote(keyword);
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + safeKeyword));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error while searching: " + ex.getMessage());
                }
            }
        });

        // === Refresh Action ===
        btnRefresh.addActionListener(e -> {
            sorter.setRowFilter(null);
            txtSearch.setText("");
            refreshStockTable();
        });

        actions.add(btnSave);
        actions.add(btnClear);
        actions.add(btnDelete);
        actions.add(btnCancel);

        // === Layout ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(search, BorderLayout.NORTH);
        topPanel.add(form, BorderLayout.CENTER);

        setLayout(new BorderLayout(10, 10));
        add(topPanel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        // Load initial data
        refreshStockTable();
    }

    private boolean saveToDatabase(String productName, String type, int qty, double mrp, 
                                  String purchaseDate, String mfgDate, String expDate) {
        PreparedStatement ps = null;
        try {
            // Check if product already exists (by name and type)
            String checkSql = "SELECT purchase_id, qty FROM purchase WHERE product_name = ? AND product_type = ?";
            ps = con.prepareStatement(checkSql);
            ps.setString(1, productName);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Product exists, update quantity and MRP
                int existingId = rs.getInt("purchase_id");
                int existingQty = rs.getInt("qty");
                
                String updateSql = "UPDATE purchase SET qty = qty + ?, mrp = ?, purchase_date = ?, mfg_date = ?, exp_date = ? WHERE purchase_id = ?";
                ps = con.prepareStatement(updateSql);
                ps.setInt(1, qty);
                ps.setDouble(2, mrp);
                // Handle dates - set to NULL if empty
                if (purchaseDate != null && !purchaseDate.isEmpty()) {
                    ps.setString(3, purchaseDate);
                } else {
                    ps.setNull(3, Types.DATE);
                }
                if (mfgDate != null && !mfgDate.isEmpty()) {
                    ps.setString(4, mfgDate);
                } else {
                    ps.setNull(4, Types.DATE);
                }
                if (expDate != null && !expDate.isEmpty()) {
                    ps.setString(5, expDate);
                } else {
                    ps.setNull(5, Types.DATE);
                }
                ps.setInt(6, existingId);
                
                int rows = ps.executeUpdate();
                return rows > 0;
            } else {
                // New product, insert
                String insertSql = "INSERT INTO purchase (product_name, product_type, qty, mrp, purchase_date, mfg_date, exp_date, supplier_id, company_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                ps = con.prepareStatement(insertSql);
                ps.setString(1, productName);
                ps.setString(2, type);
                ps.setInt(3, qty);
                ps.setDouble(4, mrp);
                // Handle dates - set to NULL if empty
                if (purchaseDate != null && !purchaseDate.isEmpty()) {
                    ps.setString(5, purchaseDate);
                } else {
                    ps.setNull(5, Types.DATE);
                }
                if (mfgDate != null && !mfgDate.isEmpty()) {
                    ps.setString(6, mfgDate);
                } else {
                    ps.setNull(6, Types.DATE);
                }
                if (expDate != null && !expDate.isEmpty()) {
                    ps.setString(7, expDate);
                } else {
                    ps.setNull(7, Types.DATE);
                }
                // Set default values for foreign keys
                ps.setNull(8, Types.INTEGER); // supplier_id
                ps.setNull(9, Types.INTEGER); // company_id
                ps.setInt(10, 1); // user_id (default to 1)
                
                int rows = ps.executeUpdate();
                return rows > 0;
            }
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

    private boolean deleteFromDatabase(int purchaseId) {
        PreparedStatement ps = null;
        try {
            String sql = "DELETE FROM purchase WHERE purchase_id = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, purchaseId);
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

    private void refreshStockTable() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            model.setRowCount(0);
            
            String sql = "SELECT purchase_id, product_name, product_type, qty, mrp, " +
                        "purchase_date, mfg_date, exp_date FROM purchase ORDER BY product_name";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            
            SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            while (rs.next()) {
                // Format dates for display - handle null dates properly
                java.sql.Date purchaseDate = rs.getDate("purchase_date");
                java.sql.Date mfgDate = rs.getDate("mfg_date");
                java.sql.Date expDate = rs.getDate("exp_date");
                
                String purchaseDateStr = "N/A";
                String mfgDateStr = "N/A";
                String expDateStr = "N/A";
                
                // Check if dates are not null before formatting
                if (purchaseDate != null) {
                    try {
                        purchaseDateStr = displayDateFormat.format(purchaseDate);
                    } catch (Exception e) {
                        purchaseDateStr = "Invalid Date";
                    }
                }
                
                if (mfgDate != null) {
                    try {
                        mfgDateStr = displayDateFormat.format(mfgDate);
                    } catch (Exception e) {
                        mfgDateStr = "Invalid Date";
                    }
                }
                
                if (expDate != null) {
                    try {
                        expDateStr = displayDateFormat.format(expDate);
                    } catch (Exception e) {
                        expDateStr = "Invalid Date";
                    }
                }
                
                model.addRow(new Object[]{
                    rs.getInt("purchase_id"),
                    rs.getString("product_name"),
                    rs.getString("product_type"),
                    rs.getInt("qty"),
                    rs.getDouble("mrp"),
                    purchaseDateStr,
                    mfgDateStr,
                    expDateStr
                });
            }
            
            // If table is empty, show message
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No stock data found. Please add some products.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading stock data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void clearForm(JTextField txtProductName, JTextField txtQty, JTextField txtMRP, 
                          JComboBox<String> cmbProductType, JSpinner txtPurchaseDate, 
                          JSpinner txtMfgDate, JSpinner txtExpDate) {
        txtProductName.setText("");
        txtQty.setText("");
        txtMRP.setText("");
        cmbProductType.setSelectedIndex(0);
        txtPurchaseDate.setValue(new Date());
        txtMfgDate.setValue(new Date());
        txtExpDate.setValue(new Date());
        txtProductName.requestFocus();
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StockModule().setVisible(true));
    }
}