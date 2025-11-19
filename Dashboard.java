package medicalstore.masterdata;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Dashboard extends JFrame implements ActionListener {

    JButton btnBack;

    public Dashboard() {
        setTitle("Sub Dashboard");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Back button
        btnBack = new JButton("Back");
        btnBack.addActionListener(this); // ✅ Correct way

        JPanel panel = new JPanel();
        panel.add(btnBack);
        add(panel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            JOptionPane.showMessageDialog(this, "Back to the main dashboard!");

            // close current window
            dispose();

            // go back to the main dashboard in package medicalstore
            new medicalstore.Dashboard().setVisible(true);
        }
    }

    // For testing standalone
    public static void main(String[] args) {
        new Dashboard().setVisible(true);
    }
}
