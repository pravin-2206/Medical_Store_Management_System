package medicalstore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login extends JFrame implements ActionListener {

    JTextField txtUser;
    JPasswordField txtPass;
    JButton btnLogin, btnExit;

    public Login() {
        setTitle("Login - Medical Store");
        
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3,2,10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));

        panel.add(new JLabel("Username:👤"));
        txtUser = new JTextField();
        panel.add(txtUser);

        panel.add(new JLabel("Password:🔒"));
        txtPass = new JPasswordField();
        panel.add(txtPass);

        btnLogin = new JButton("Login");
        btnExit = new JButton("Exit");

        btnLogin.addActionListener(this);
        btnExit.addActionListener(this);

        panel.add(btnLogin);
        panel.add(btnExit);

        add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin) {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());
            Connection con = DBConnection.getConnection();    
            PreparedStatement ps = null;
            ResultSet rs=null;

            // Try block to check if exception/s occurs
            try {
                String sql = "select * from users where user_name='"+user+"' AND password='"+pass+"'";
                ps =con.prepareStatement(sql);
                rs =ps.executeQuery();
                
                // Condition check
                if (rs.next()) {

                	String u_name = rs.getString("user_name");
                    String password = rs.getString("password");
                    
                    if (user.equals(u_name) && pass.equals(password)) {
                        JOptionPane.showMessageDialog(this, "Login Successful!");
                        dispose(); 
                        new Dashboard().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid Username or Password");
                    }
                } 
                else {
                    JOptionPane.showMessageDialog(this, "Invalid Username or Password");
                }
            }

            catch (Exception ex) {

                // Print the exception
                System.out.println(ex);
            }
            

        } else if (e.getSource() == btnExit) {
            System.exit(0);
        }
    }
        public static void main(String[] args) {
        new Login().setVisible(true);
    }
}
