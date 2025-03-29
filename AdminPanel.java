// AdminPanel.java
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;

public class AdminPanel extends JFrame {
    private Connection connection;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField usernameField;
    private JTextField messageField;
    private JTextField searchField;

    public AdminPanel() {
        super("Admin Panel");
        setLayout(new BorderLayout());
        setupDatabase();

        // Tạo model và bảng hiển thị dữ liệu tin nhắn
        tableModel = new DefaultTableModel(new String[] {"ID", "Username", "Message", "Timestamp"}, 0);
        table = new JTable(tableModel);
        refreshTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel cho thao tác CRUD
        JPanel crudPanel = new JPanel(new GridLayout(2, 1));

        // Panel nhập liệu
        JPanel inputPanel = new JPanel(new FlowLayout());
        usernameField = new JTextField(10);
        messageField = new JTextField(30);
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Message:"));
        inputPanel.add(messageField);
        crudPanel.add(inputPanel);

        // Panel chứa các nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton searchButton = new JButton("Search");
        searchField = new JTextField(15);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(new JLabel("Search:"));
        buttonPanel.add(searchField);
        buttonPanel.add(searchButton);
        crudPanel.add(buttonPanel);

        add(crudPanel, BorderLayout.SOUTH);

        // Xử lý sự kiện các nút
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addMessage();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateMessage();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteMessage();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchMessages();
            }
        });

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupDatabase() {
        try {
            // Sửa thông tin kết nối cho phù hợp với MySQL của bạn
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatdb", "root", "");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Làm mới bảng dữ liệu hiển thị từ DB
    private void refreshTable() {
        tableModel.setRowCount(0);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM messages")) {
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("message"),
                    rs.getTimestamp("timestamp")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Thêm tin nhắn mới vào DB
    private void addMessage() {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO messages (username, message) VALUES (?, ?)")) {
            ps.setString(1, usernameField.getText());
            ps.setString(2, messageField.getText());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        refreshTable();
    }

    // Cập nhật tin nhắn đã chọn
    private void updateMessage() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (Integer) tableModel.getValueAt(selectedRow, 0);
            try (PreparedStatement ps = connection.prepareStatement("UPDATE messages SET username = ?, message = ? WHERE id = ?")) {
                ps.setString(1, usernameField.getText());
                ps.setString(2, messageField.getText());
                ps.setInt(3, id);
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Chọn dòng cần cập nhật");
        }
    }

    // Xóa tin nhắn đã chọn
    private void deleteMessage() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (Integer) tableModel.getValueAt(selectedRow, 0);
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM messages WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa");
        }
    }

    // Tìm kiếm tin nhắn theo username hoặc nội dung tin nhắn
    private void searchMessages() {
        String searchTerm = searchField.getText();
        tableModel.setRowCount(0);
        String query = "SELECT * FROM messages WHERE username LIKE ? OR message LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            String term = "%" + searchTerm + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("message"),
                    rs.getTimestamp("timestamp")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminPanel());
    }
}
