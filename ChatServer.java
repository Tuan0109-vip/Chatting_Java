// ChatServer.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

public class ChatServer {
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Connection connection;

    public static void main(String[] args) {
        System.out.println("Server is running...");
        // Thiết lập kết nối với database (chatdb đã tồn tại)
        setupDatabase();
        // Khởi chạy server lắng nghe client kết nối
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setupDatabase() {
        try {
            // Kết nối đến MySQL
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatdb", "root", "");
            System.out.println("✅ Kết nối MySQL thành công!");
    
            Statement statement = connection.createStatement();
            // Tạo bảng messages nếu chưa tồn tại
            statement.execute("CREATE TABLE IF NOT EXISTS messages ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(255), "
                    + "message TEXT, "
                    + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
    
            System.out.println("✅ Bảng 'messages' đã được kiểm tra hoặc tạo thành công!");
    
        } catch (SQLException e) {
            System.out.println("❌ Lỗi kết nối MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    //123
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }
                // Nhận tên người dùng từ client
                username = in.readLine();
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(username + ": " + message);
                    saveMessage(username, message);
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(username + ": " + message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Lưu tin nhắn vào bảng messages
    private static void saveMessage(String username, String message) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO messages (username, message) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
