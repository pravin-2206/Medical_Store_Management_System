package medicalstore;

import javax.swing.*;
import medicalstore.masterdata.Masterdatamodule;
import java.awt.*;
import java.awt.event.*;

public class Dashboard extends JFrame implements ActionListener {

    JButton btnPurchase, btnSales, btnStock, btnMasterdata, btnReturn, btnReports, btnLogout;

    public Dashboard() {
        setTitle("Medical Store Management System - Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initializeUI();
        setVisible(true); 
    }

    private void initializeUI() {
        JLabel title = new JLabel("Medical Store Management System", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(34,45,65));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        titlePanel.add(title);

        // Use 2x3 grid since we have 6 functional buttons + logout
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        btnPurchase = new JButton("Purchase");
        btnSales = new JButton("Sales");
        btnStock = new JButton("Stock");
        btnMasterdata = new JButton("Masterdata");
        btnReturn = new JButton("Return");
        btnReports = new JButton("Reports");
        btnLogout = new JButton("Logout");

        JButton[] all = { btnPurchase, btnSales, btnStock, btnMasterdata, btnReturn, btnReports };
        for (JButton b : all) {
            b.setFocusPainted(false);
            buttonPanel.add(b);
            b.addActionListener(this);
        }
        
        // Add logout button separately
        btnLogout.setFocusPainted(false);
        buttonPanel.add(btnLogout);
        btnLogout.addActionListener(this);
        
        // Add empty panels to fill remaining grid cells
        buttonPanel.add(new JLabel()); // Empty cell
        buttonPanel.add(new JLabel()); // Empty cell

        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        try {
            if (s == btnPurchase) {
                // Check if PurchaseModule exists
                new PurchaseModule().setVisible(true);
            } else if (s == btnSales) {
                new SalesModule().setVisible(true);
            } else if (s == btnStock) {
                new StockModule().setVisible(true);
            } else if (s == btnMasterdata) {
                new Masterdatamodule().setVisible(true);
            } else if (s == btnReturn) {
                new ReturnModule().setVisible(true);
            } else if (s == btnReports) {
                new ReportsModule().setVisible(true);
            } else if (s == btnLogout) {
                int c = JOptionPane.showConfirmDialog(this, 
                    "Logout and return to Login?", "Logout", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    dispose();
                    new Login().setVisible(true);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Module not available: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    // Add main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard());
    }
}