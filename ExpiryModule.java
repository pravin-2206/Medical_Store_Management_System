package medicalstore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Calendar;

public class ExpiryModule extends JFrame {

    public ExpiryModule() {
        setTitle("Expiry Module");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // === Form Panel ===
        JPanel form = new JPanel(new GridLayout(1, 3, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Add Product"));

        JTextField txtProductName = new JTextField();
        JSpinner txtExpiry = new JSpinner(new SpinnerDateModel());
        txtExpiry.setEditor(new JSpinner.DateEditor(txtExpiry, "MM/yyyy"));

        form.add(labeled("Product Name", txtProductName));
        form.add(labeled("Expiry (MM/YYYY)", txtExpiry));

        // === Table ===
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Product Name", "Expiry", "Status"}, 0
        );
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Products"));

        // === Action Buttons ===
        JPanel actions = new JPanel();
        JButton btnAdd = new JButton("Add Product");
        JButton btnSearch = new JButton("Search Product");
        JButton btnClear = new JButton("Clear Form");
        JButton btnCancel = new JButton("Cancel");

        // Add Product Action
        btnAdd.addActionListener(e -> {
            try {
                String product = txtProductName.getText().trim();
                String expiryStr = new java.text.SimpleDateFormat("MM/yyyy").format(txtExpiry.getValue());

                if (product.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Product Name is required!", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Check if expired
                String[] parts = expiryStr.split("/");
                int expMonth = Integer.parseInt(parts[0]);
                int expYear = Integer.parseInt(parts[1]);

                Calendar now = Calendar.getInstance();
                int currentMonth = now.get(Calendar.MONTH) + 1;
                int currentYear = now.get(Calendar.YEAR);

                String status = (expYear < currentYear || (expYear == currentYear && expMonth < currentMonth))
                        ? "Expired" : "Active";

                model.addRow(new Object[]{product, expiryStr, status});
                txtProductName.setText("");
                txtExpiry.setValue(new java.util.Date());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding product!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Search Product Action
        btnSearch.addActionListener(e -> {
            String searchName = JOptionPane.showInputDialog(this, "Enter Product Name to search:");
            if (searchName != null && !searchName.trim().isEmpty()) {
                boolean found = false;
                for (int i = 0; i < model.getRowCount(); i++) {
                    String product = model.getValueAt(i, 0).toString();
                    if (product.equalsIgnoreCase(searchName.trim())) {
                        String status = model.getValueAt(i, 2).toString();
                        JOptionPane.showMessageDialog(this, "Product: " + product + "\nStatus: " + status);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    JOptionPane.showMessageDialog(this, "Product not found!");
                }
            }
        });

        // Clear form
        btnClear.addActionListener(e -> {
            txtProductName.setText("");
            txtExpiry.setValue(new java.util.Date());
        });

        btnCancel.addActionListener(e -> dispose());

        actions.add(btnAdd);
        actions.add(btnSearch);
        actions.add(btnClear);
        actions.add(btnCancel);

        // === Layout ===
        setLayout(new BorderLayout(10, 10));
        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExpiryModule().setVisible(true));
    }
}
