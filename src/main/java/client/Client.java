package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    private static final int SERVER_PORT = 8080;
    private static final String SERVER_ADDRESS = "localhost";

    private Socket socket;

    private BufferedReader reader;
    private BufferedWriter writer;

    public Client() {
        setTitle("Login/Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false); // Prevent resizing
        setLocationRelativeTo(null);

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding around the panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around components

        Font labelFont = new Font("Arial", Font.BOLD, 14);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        loginButton = new JButton("Login");
        loginButton.setFont(labelFont);
        loginButton.setBackground(Color.BLUE); // Customize button color
        loginButton.setForeground(Color.WHITE); // Text color

        registerButton = new JButton("Register");
        registerButton.setFont(labelFont);
        registerButton.setBackground(Color.BLUE); // Customize button color
        registerButton.setForeground(Color.WHITE); // Text color

        // Center the login and register buttons with space between them
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0)); // Using GridLayout for button arrangement

        // Create an empty panel for spacing
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;

        buttonPanel.add(loginButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;

        buttonPanel.add(registerButton, gbc);

        loginPanel.add(buttonPanel, gbc);

        add(loginPanel, BorderLayout.CENTER);

        pack(); // Adjust frame size

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            sendRequest("LOGIN", username, password);
        });

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(Client.this, "Username or Password must not be empty!");
                return;
            }
            sendRequest("REGISTER", username, password);
        });
    }

    private void sendRequest(String requestType, String username, String password) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            writer.write(requestType + "\n");
            writer.write(username + "\n");
            writer.write(password + "\n");
            writer.flush();

            String response = reader.readLine();
            handleResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleResponse(String response) {
        if (response != null) {
            switch (response) {
                case "LoginSuccessful":
                    JOptionPane.showMessageDialog(Client.this, "Login successful!");
                    openChatApp();
                    dispose();
                    break;
                case "InvalidCredentials":
                    JOptionPane.showMessageDialog(Client.this, "Invalid username or password!");
                    break;
                case "RegistrationSuccessful":
                    JOptionPane.showMessageDialog(Client.this, "Registration successful!");
                    break;
                case "UsernameExists":
                    JOptionPane.showMessageDialog(Client.this, "Username already exists!");
                    break;
                default:
                    JOptionPane.showMessageDialog(Client.this, "Unexpected response from server");
                    break;
            }
        } else {
            JOptionPane.showMessageDialog(Client.this, "No response from server");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client app = new Client();
            app.setVisible(true);

            // Centering the frame on screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (screenSize.width - app.getWidth()) / 2;
            int y = (screenSize.height - app.getHeight()) / 2;
            app.setLocation(x, y);
        });
    }

    private void openChatApp() {
        SwingUtilities.invokeLater(() -> {
            String username = usernameField.getText();
            ChatApp chatApp = new ChatApp(username, socket, reader, writer);
            chatApp.setVisible(true);

            // Centering the frame on screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (screenSize.width - chatApp.getWidth()) / 2;
            int y = (screenSize.height - chatApp.getHeight()) / 2;
            chatApp.setLocation(x, y);
        });
    }
}
