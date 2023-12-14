package server;

import javax.swing.*;
import java.awt.*;

public class ServerFrame {
    private static void addComponents(Container contentPane) {
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10); // Padding

        JButton startBtn = new JButton("Start server");
        startBtn.setFont(new Font("Times New Roman", Font.BOLD, 14));

        JLabel text = new JLabel("Click here to start server");
        text.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        contentPane.add(startBtn, constraints);

        constraints.gridy = 1;
        contentPane.add(text, constraints);

        // Start the server
        startBtn.addActionListener(e -> {
            // Start the server in a separate thread
            SwingUtilities.invokeLater(() -> {
                text.setText("Server started successfully");
                startBtn.setEnabled(false);
            });
            new Thread(() -> {
                Server server = new Server();
                server.startServer();
            }).start();
        });
    }

    private static void createAndShowGUI() {
        // Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the content pane.
        addComponents(frame.getContentPane());

        // Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
