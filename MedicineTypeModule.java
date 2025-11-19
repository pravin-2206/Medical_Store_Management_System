package medicalstore.masterdata;

import javax.swing.*;

public class MedicineTypeModule extends JFrame {
    public MedicineTypeModule() {
        setTitle("Medicine Type Module");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JLabel lbl = new JLabel("Medicine Type Module Window", JLabel.CENTER);
        add(lbl);
    }
}
