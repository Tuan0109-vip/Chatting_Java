// ChatClient.java
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chat Client");
    private JTextArea messageArea = new JTextArea(20, 50);
    private JTextField inputField = new JTextField(50);
    private String username;

    public ChatClient() {
        // Yêu cầu người dùng nhập tên
        username = JOptionPane.showInputDialog(frame, "Enter your username:");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        messageArea.setEditable(false);
        frame.pack();
        frame.setVisible(true);

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(inputField.getText());
                inputField.setText("");
            }
        });
    }

    public void connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // Gửi tên người dùng đến server
            out.println(username);

            String message;
            while ((message = in.readLine()) != null) {
                messageArea.append(message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.connect();
    }
}
