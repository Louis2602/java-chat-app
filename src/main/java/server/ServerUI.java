package server;

import javax.swing.*;
import java.awt.*;

public class ServerUI {
    private static void addComponents(Container contentPane) {
        JButton startBtn = new JButton("Start server");
        startBtn.setFont(new Font("Times New Roman", Font.BOLD, 14));

        JPanel panel = new JPanel();
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
                gl_contentPane.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(panel, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addGap(90)
                                .addComponent(startBtn, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                .addGap(81))
        );
        gl_contentPane.setVerticalGroup(
                gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addGap(43)
                                .addComponent(startBtn, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(panel, GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                .addContainerGap())
        );

        JLabel text = new JLabel("Click here to start server");
        text.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        panel.add(text);
        contentPane.setLayout(gl_contentPane);
        // Start the server
        startBtn.addActionListener(e -> {
            Thread serverThread = new Thread(Server::new);
            serverThread.start();
            text.setText("Start server successful");
            startBtn.setEnabled(false);
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
