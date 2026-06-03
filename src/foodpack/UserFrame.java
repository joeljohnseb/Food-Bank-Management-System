package foodpack;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;

public class UserFrame extends JFrame {

    private final int    userId;
    private final String username;

    public UserFrame(int userId, String username) {
        this.userId   = userId;
        this.username = username;

        setTitle("Food Bank — User: " + username);
        setSize(620, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ── Top bar with logout button ────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topBar.add(new JLabel("Welcome, " + username + "!"), BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();               // close this window
            new LoginFrame();        // go back to login
        });
        topBar.add(logoutBtn, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📦 Inventory",    createInventoryTab());
        tabs.addTab("➕ Donate Item",  createDonateTab());
        tabs.addTab("📋 My Requests",  createMyRequestsTab());

        add(topBar, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────
    // TAB 1 : Inventory
    // ─────────────────────────────────────────────────────────────────
    private JPanel createInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Item Name", "Available Quantity"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadInventory(model);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("Quantity to Take:"));
        JTextField qtyField = new JTextField(6);
        bottom.add(qtyField);

        JButton takeBtn    = new JButton("Take Item");
        JButton refreshBtn = new JButton("Refresh");
        bottom.add(takeBtn);
        bottom.add(refreshBtn);

        takeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an item from the table.");
                return;
            }

            String itemName  = (String) model.getValueAt(row, 0);
            int    available = (int)    model.getValueAt(row, 1);
            String qtyText   = qtyField.getText().trim();

            if (qtyText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter the quantity you want to take.");
                return;
            }

            int qty;
            try {
                qty = Integer.parseInt(qtyText);
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive number.");
                return;
            }

            if (qty > available) {
                JOptionPane.showMessageDialog(this,
                    "Only " + available + " units available for \"" + itemName + "\".");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE inventory SET quantity = quantity - ? WHERE item_name = ?");
                ps.setInt(1, qty);
                ps.setString(2, itemName);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Successfully took " + qty + " unit(s) of \"" + itemName + "\".");
                qtyField.setText("");
                loadInventory(model);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        refreshBtn.addActionListener(e -> loadInventory(model));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void loadInventory(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT item_name, quantity FROM inventory WHERE quantity > 0 ORDER BY item_name");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("item_name"),
                    rs.getInt("quantity")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // TAB 2 : Donate Item
    // ─────────────────────────────────────────────────────────────────
    private JPanel createDonateTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JTextField itemField = new JTextField(18);
        JTextField qtyField  = new JTextField(18);

        g.gridx = 0; g.gridy = 0; panel.add(new JLabel("Item Name:"), g);
        g.gridx = 1;               panel.add(itemField, g);

        g.gridx = 0; g.gridy = 1; panel.add(new JLabel("Quantity:"), g);
        g.gridx = 1;               panel.add(qtyField, g);

        JButton submitBtn = new JButton("Submit Donation Request");
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        panel.add(submitBtn, g);

        JLabel noteLabel = new JLabel(
            "<html><i>Your request will be reviewed by the admin before being added to inventory.</i></html>",
            SwingConstants.CENTER);
        noteLabel.setForeground(Color.GRAY);
        g.gridy = 3;
        panel.add(noteLabel, g);

        submitBtn.addActionListener(e -> {
            String item    = itemField.getText().trim();
            String qtyText = qtyField.getText().trim();

            if (item.isEmpty() || qtyText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in both fields.");
                return;
            }

            int qty;
            try {
                qty = Integer.parseInt(qtyText);
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive quantity.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO requests (user_id, username, item_name, quantity, status) " +
                    "VALUES (?, ?, ?, ?, 'pending')");
                ps.setInt(1,    userId);
                ps.setString(2, username);
                ps.setString(3, item);
                ps.setInt(4,    qty);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Request submitted! Waiting for admin approval.");
                itemField.setText("");
                qtyField.setText("");

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    // TAB 3 : My Requests — shows pending, approved AND rejected
    // ─────────────────────────────────────────────────────────────────
    private JPanel createMyRequestsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Item Name", "Quantity", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        // Color the status column: green=approved, red=rejected, orange=pending
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String status = value == null ? "" : value.toString();
                switch (status) {
                    case "approved" -> setForeground(new Color(0, 140, 0));
                    case "rejected" -> setForeground(Color.RED);
                    default         -> setForeground(new Color(200, 120, 0)); // pending = orange
                }
                return this;
            }
        });

        loadMyRequests(model);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadMyRequests(model));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(refreshBtn,             BorderLayout.SOUTH);
        return panel;
    }

    private void loadMyRequests(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            // ── CHANGE: now shows ALL statuses including rejected ──
            PreparedStatement ps = conn.prepareStatement(
                "SELECT item_name, quantity, status FROM requests " +
                "WHERE user_id = ? ORDER BY id DESC");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}