package medicalstore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;

public class CustomerModule extends JFrame {
    public CustomerModule() {
        setTitle("Customer Module");
        setSize(800, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // === Form Panel ===
        JPanel form = new JPanel(new GridLayout(2, 3, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Customer Details"));

        JTextField txtFirstName = new JTextField();
        JTextField txtLastName = new JTextField();
        JTextField txtAddress = new JTextField();
        JTextField txtPhone = new JTextField();
        JSpinner txtDate = new JSpinner(new SpinnerDateModel());
        txtDate.setEditor(new JSpinner.DateEditor(txtDate, "dd/MM/yyyy"));
        JTextField txtEmail = new JTextField();

        form.add(labeled("First Name", txtFirstName));
        form.add(labeled("Last Name", txtLastName));
        form.add(labeled("Address", txtAddress));
        form.add(labeled("Phone", txtPhone));
        form.add(labeled("Date", txtDate));
        form.add(labeled("Email", txtEmail));

        // === Table ===
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"First Name", "Last Name", "Address", "Phone", "Date", "Email"}, 0);
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Customer Records"));

        // === Buttons ===
        JPanel actions = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        JButton btnCancel = new JButton("Cancel");

        // === Save Action ===
        btnSave.addActionListener(e -> {
            try {
                String firstName = txtFirstName.getText().trim();
                String lastName = txtLastName.getText().trim();
                String address = txtAddress.getText().trim();
                String phone = txtPhone.getText().trim();
                String email = txtEmail.getText().trim();
                String date = new SimpleDateFormat("dd/MM/yyyy").format(txtDate.getValue());

                if (firstName.isEmpty() || lastName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "First and Last name are required!",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                model.addRow(new Object[]{firstName, lastName, address, phone, date, email});

                // Optional: clear after saving
                txtFirstName.setText("");
                txtLastName.setText("");
                txtAddress.setText("");
                txtPhone.setText("");
                txtEmail.setText("");
                txtDate.setValue(new java.util.Date());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving data!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // === Clear Action ===
        btnClear.addActionListener(e -> {
            txtFirstName.setText("");
            txtLastName.setText("");
            txtAddress.setText("");
            txtPhone.setText("");
            txtEmail.setText("");
            txtDate.setValue(new java.util.Date());
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

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerModule().setVisible(true));
    }
}
