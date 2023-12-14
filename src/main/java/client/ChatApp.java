package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatApp extends JFrame {
    private JList<String> onlineUsersList;
    private JList<String> groupsList;
    private JTextArea chatArea;
    private JTextField messageField;
    private JList<String> currentChatUsersList;

    public ChatApp() {
        setTitle("Chat Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setPreferredSize(new Dimension(800, 600)); // Setting preferred dimensions

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a split pane to separate online users and groups
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300); // Set the initial divider location

        JPanel topPanel = new JPanel(new BorderLayout());
        // Server Info Panel
        JPanel serverInfoPanel = createPanelWithBorderLayout("Server Info");
        JLabel serverIP = new JLabel("Server IP: 127.0.0.1");
        JLabel serverPort = new JLabel("Server Port: 8080");
        JLabel totalOnlineUsers = new JLabel("Total Online Users: 10");
        serverInfoPanel.add(serverIP, BorderLayout.NORTH);
        serverInfoPanel.add(serverPort, BorderLayout.CENTER);
        serverInfoPanel.add(totalOnlineUsers, BorderLayout.SOUTH);

        topPanel.add(serverInfoPanel, BorderLayout.NORTH);

        // Online Users Panel
        JPanel onlineUsersPanel = createPanelWithBorderLayout("Online Users");
        onlineUsersList = new JList<>(new String[]{"User 1", "User 2", "User 3"});
        JScrollPane onlineUsersScrollPane = new JScrollPane(onlineUsersList);
        onlineUsersPanel.add(onlineUsersScrollPane, BorderLayout.CENTER);

        topPanel.add(onlineUsersPanel, BorderLayout.CENTER);

        splitPane.setTopComponent(topPanel);

        JPanel groupsPanel = createPanelWithBorderLayout("Groups");
        groupsList = new JList<>(new String[]{"Group 1", "Group 2", "Group 3"});
        JScrollPane groupsScrollPane = new JScrollPane(groupsList);
        JButton createGroupButton = new JButton("Create Group");
        groupsPanel.add(groupsScrollPane, BorderLayout.CENTER);
        groupsPanel.add(createGroupButton, BorderLayout.SOUTH);

        splitPane.setBottomComponent(groupsPanel);

        mainPanel.add(splitPane, BorderLayout.WEST);

        JPanel centerPanel = createPanelWithBorderLayout("Chat");
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        centerPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField(25);
        JButton sendButton = new JButton("Send");
        JButton uploadButton = new JButton("Upload File");

        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        messagePanel.add(uploadButton, BorderLayout.WEST);

        centerPanel.add(messagePanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = createPanelWithBorderLayout("Current Chat Users");
        rightPanel.setLayout(new GridBagLayout());

        currentChatUsersList = new JList<>(new String[]{"User A", "User B", "User C"}); // Sample data for current chat users
        JScrollPane currentChatUsersScrollPane = new JScrollPane(currentChatUsersList);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(currentChatUsersScrollPane, gbc);

        // Set preferred width for the right panel
        Dimension rightPanelPreferred = new Dimension(200, rightPanel.getPreferredSize().height);
        rightPanel.setPreferredSize(rightPanelPreferred);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel);
        pack();
    }

    private JPanel createPanelWithBorderLayout(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            displayMessage("You: " + message + "\n");
            // Here, you would typically send the message to the server or process it further
            messageField.setText(""); // Clear the message field after sending
        }
    }

    public void displayMessage(String message) {
        chatArea.append(message);
    }
}
