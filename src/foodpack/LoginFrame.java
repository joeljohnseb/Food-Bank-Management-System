package foodpack;

import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    public LoginFrame() {
        setTitle("Community Food Bank - Login");
        setSize(360, 230);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Title label ──────────────────────────────────────────────
        JLabel title = new JLabel("Community Food Bank", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));

        // ── Form panel ───────────────────────────────────────────────
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        roleCombo     = new JComboBox<>(new String[]{"User", "Admin"});

        form.add(new JLabel("Username:"));  form.add(usernameField);
        form.add(new JLabel("Password:"));  form.add(passwordField);
        form.add(new JLabel("Login As:"));  form.add(roleCombo);
        
        // ── Button panel ─────────────────────────────────────────────
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> handleLogin());

        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        btnPanel.add(loginBtn);

        // ── Assemble ─────────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(title,   BorderLayout.NORTH);
        add(form,    BorderLayout.CENTER);
        add(btnPanel,BorderLayout.SOUTH);

        // Allow pressing Enter to login
        getRootPane().setDefaultButton(loginBtn);

        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role     = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // Check the correct table based on chosen role
            String table = role.equals("Admin") ? "admins" : "users";
            String query = "SELECT * FROM " + table + " WHERE username = ? AND password = ?";

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                dispose();
                if (role.equals("Admin")) {
                    new AdminFrame();
                } else {
                    new UserFrame(id, username);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid credentials. Please try again.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Entry point ───────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
