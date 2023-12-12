package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        initComponents();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around components

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span button across two columns
        gbc.anchor = GridBagConstraints.CENTER; // Align button to the center
        gbc.ipady = 10; // Increase the height of the button
        gbc.ipadx = 10; // Increase the width of the button
        loginPanel.add(loginButton, gbc);

        JPanel registerPanel = new JPanel(new GridLayout(1, 2, 5, 0)); // Panel to contain the labels
        JLabel staticTextLabel = new JLabel("Don't have an account?");
        staticTextLabel.setForeground(Color.GRAY); // Set color to gray
        registerPanel.add(staticTextLabel); // Add staticTextLabel to registerPanel
        JLabel registerLabel = new JLabel("Register");
        registerLabel.setForeground(Color.BLUE);
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerPanel.add(registerLabel); // Add registerLabel to registerPanel
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER; // Center align the registerPanel
        loginPanel.add(registerPanel, gbc);


        add(loginPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> performLogin());
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                RegisterFrame registerFrame = new RegisterFrame(); // Create an instance of RegisterFrame
                registerFrame.setVisible(true);
            }
        });


        add(loginPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> performLogin());
    }


    private void performLogin() {
        String username = usernameField.getText();
        char[] password = passwordField.getPassword();

        // Replace this with your actual authentication logic
        if (username.equals("your_username") && String.valueOf(password).equals("your_password")) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            // Add code to proceed after successful login
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Try again.");
            // Clear fields or perform other actions for failed login attempts
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame();
            }
        });
    }
}
