package foodpack;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminFrame extends JFrame {

    public AdminFrame() {
        setTitle("Food Bank — Admin Panel");
        setSize(660, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ── Top bar with logout button ────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topBar.add(new JLabel("Admin Panel"), BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();           // close admin window
            new LoginFrame();    // go back to login
        });
        topBar.add(logoutBtn, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("🕐 Pending Requests", createPendingTab());
        tabs.addTab("📦 Inventory",        createInventoryTab());

        add(topBar, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────
    // TAB 1 : Pending Requests
    // ─────────────────────────────────────────────────────────────────
    private JPanel createPendingTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Request ID", "Username", "Item Name", "Quantity"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide Request ID column (used internally only)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        loadPending(model);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        JButton approveBtn = new JButton("✔ Approve");
        JButton rejectBtn  = new JButton("✘ Reject");
        JButton refreshBtn = new JButton("Refresh");

        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(refreshBtn);

        // ── Approve ──────────────────────────────────────────────────
        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a request first.");
                return;
            }

            int    reqId    = (int)    model.getValueAt(row, 0);
            String itemName = (String) model.getValueAt(row, 2);
            int    qty      = (int)    model.getValueAt(row, 3);

            try (Connection conn = DBConnection.getConnection()) {

                PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM inventory WHERE item_name = ?");
                check.setString(1, itemName);
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    PreparedStatement update = conn.prepareStatement(
                        "UPDATE inventory SET quantity = quantity + ? WHERE item_name = ?");
                    update.setInt(1, qty);
                    update.setString(2, itemName);
                    update.executeUpdate();
                } else {
                    PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO inventory (item_name, quantity) VALUES (?, ?)");
                    insert.setString(1, itemName);
                    insert.setInt(2, qty);
                    insert.executeUpdate();
                }

                PreparedStatement approve = conn.prepareStatement(
                    "UPDATE requests SET status = 'approved' WHERE id = ?");
                approve.setInt(1, reqId);
                approve.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Request approved! \"" + itemName + "\" added to inventory.");
                loadPending(model);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // ── Reject ───────────────────────────────────────────────────
        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a request first.");
                return;
            }

            int reqId = (int) model.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reject this request?",
                "Confirm Reject", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE requests SET status = 'rejected' WHERE id = ?");
                ps.setInt(1, reqId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Request rejected.");
                loadPending(model);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        refreshBtn.addActionListener(e -> loadPending(model));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel,               BorderLayout.SOUTH);
        return panel;
    }

    private void loadPending(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT id, username, item_name, quantity FROM requests " +
                "WHERE status = 'pending' ORDER BY id ASC");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("item_name"),
                    rs.getInt("quantity")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading requests: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // TAB 2 : Inventory
    // ─────────────────────────────────────────────────────────────────
    private JPanel createInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Item Name", "Quantity"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        loadInventory(model);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadInventory(model));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(refreshBtn,             BorderLayout.SOUTH);
        return panel;
    }

    private void loadInventory(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT item_name, quantity FROM inventory ORDER BY item_name");
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
}