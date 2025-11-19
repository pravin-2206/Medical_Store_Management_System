package medicalstore.masterdata;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Masterdatamodule extends JFrame implements ActionListener {

    JButton btnUser, btnSupplier, btnMedicineCompany, btnBack;

    public Masterdatamodule() {
        setTitle("Medical Store Management System - Master Data");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Title
        JLabel title = new JLabel("Master Data Management", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(34, 45, 65));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        titlePanel.add(title);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        btnUser = new JButton("User");
        btnSupplier = new JButton("Supplier");
        btnMedicineCompany = new JButton("Medicine Company");
        btnBack = new JButton("Back to Dashboard");

        JButton[] all = { btnUser, btnSupplier, btnMedicineCompany, btnBack };
        for (JButton b : all) {
            b.setFocusPainted(false);
            b.setFont(new Font("Arial", Font.BOLD, 16));
            b.setBackground(new Color(52, 152, 219));
            b.setForeground(Color.WHITE);
            buttonPanel.add(b);
            b.addActionListener(this);
        }

        // Layout
        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == btnUser) {
            new UserModule().setVisible(true);
        } else if (s == btnSupplier) {
            new SupplierModule().setVisible(true);
        } else if (s == btnMedicineCompany) {
            new MedicineCompanyModule().setVisible(true);
        } else if (s == btnBack) {
            JOptionPane.showMessageDialog(this, "Back to the main dashboard!");
            dispose();
            new medicalstore.Dashboard().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Masterdatamodule().setVisible(true));
    }
}
