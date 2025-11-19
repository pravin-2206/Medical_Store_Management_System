package medicalstore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SupplierModule extends JFrame {
    public SupplierModule() {
        setTitle("Supplier Module");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(2,3,10,10));
        form.setBorder(BorderFactory.createTitledBorder("Supplier Details"));
        JTextField txtName = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtAddr = new JTextField();
        JTextField txtGST = new JTextField();
        JTextField txtContact = new JTextField();

        form.add(labeled("Supplier Name", txtName));
        form.add(labeled("Phone", txtPhone));
        form.add(labeled("Email", txtEmail));
        form.add(labeled("Address", txtAddr));
        form.add(labeled("GST No", txtGST));
        form.add(labeled("Contact Person", txtContact));

        JPanel actions = new JPanel();
        actions.add(new JButton("Add"));
        actions.add(new JButton("Update"));
        actions.add(new JButton("Delete"));
        actions.add(new JButton("Clear"));

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Name","Phone","Email","GST","Contact","Address"}, 0);
        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Suppliers"));

        setLayout(new BorderLayout(10,10));
        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5,5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}
