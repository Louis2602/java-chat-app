package client;

import server.Server;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ChatApp extends JFrame {
    private JList<String> onlineUsersList;
    private JList<String> groupsList;
    private JTextArea chatArea;
    private JTextField messageField;
    private JList<String> currentChatUsersList;
    private String username;
    private BufferedReader reader;
    private BufferedWriter writer;
    Thread clientReceiver;
    private JTabbedPane conversationsTabbedPane;

    public ChatApp(String username, BufferedReader reader, BufferedWriter writer) {
        this.username = username;
        this.writer = writer;
        this.reader = reader;
        clientReceiver = new Thread(new ClientReceiver(reader, this));
        clientReceiver.start();

        createChatFrame();
    }

    public void updateOnlineUsersList(DefaultListModel<String> model) {
        onlineUsersList.setModel(model);
    }

    public String getUsername() {
        return this.username;
    }

    private void createChatFrame() {
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
        JLabel serverIP = new JLabel("Server IP: " + Server.address);
        JLabel serverPort = new JLabel("Server Port: " + Server.port);
        JLabel userName = new JLabel("Current user: " + username);
        serverInfoPanel.add(serverIP, BorderLayout.NORTH);
        serverInfoPanel.add(serverPort, BorderLayout.CENTER);
        serverInfoPanel.add(userName, BorderLayout.SOUTH);

        topPanel.add(serverInfoPanel, BorderLayout.NORTH);

        // Online Users Panel
        JPanel onlineUsersPanel = createPanelWithBorderLayout("Online Users");
        onlineUsersList = new JList<>();
        JScrollPane onlineUsersScrollPane = new JScrollPane(onlineUsersList);
        onlineUsersPanel.add(onlineUsersScrollPane, BorderLayout.CENTER);
        onlineUsersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Detect a single click on a user
                    JList<String> list = (JList<String>) e.getSource();
                    int index = list.locationToIndex(e.getPoint()); // Get the index of the clicked item
                    if (index >= 0) {
                        String selectedUser = list.getModel().getElementAt(index);
                        // You can start a conversation with the selected user here
                        startConversation(selectedUser);
                    }
                }
            }
        });
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
        conversationsTabbedPane = new JTabbedPane();
        centerPanel.add(conversationsTabbedPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = createPanelWithBorderLayout("Current users in Room");
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

        // Request to close client socket when logout
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    writer.write("LOGOUT" + "\n");
                    writer.flush();

                    try {
                        clientReceiver.join();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    if (reader != null) {
                        reader.close();
                    }
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        add(mainPanel);
        pack();
    }

    private JPanel createPanelWithBorderLayout(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    private void startConversation(String selectedUser) {
        //chatArea.setText("Conversation started with: " + selectedUser + "\n");
        System.out.println("Conversation started with: " + selectedUser + "\n");
        int tabCount = conversationsTabbedPane.getTabCount();
        boolean tabExists = false;
        int tabIndex = 0;
        for (int i = 0; i < tabCount; i++) {
            if (conversationsTabbedPane.getTitleAt(i).equals(selectedUser)) {
                tabExists = true;
                tabIndex = i;
                break;
            }
        }

        if (!tabExists) {
            JPanel chatPanel = createChatPanel(selectedUser);
            conversationsTabbedPane.addTab(selectedUser, chatPanel);
            tabIndex = conversationsTabbedPane.indexOfTab(selectedUser);
        }

        // Select the tab for the selected user
        conversationsTabbedPane.setSelectedIndex(tabIndex);
     /*   String chatHistory = loadChatHistory(selectedUser); // Load chat history for the selected user

        // Display conversation information in the chat area
        if (chatHistory != null && !chatHistory.isEmpty()) {
            chatArea.append("Chat History:\n" + chatHistory); // Display loaded chat history
        } else {
            chatArea.append("No chat history found with " + selectedUser);
        }*/

    }

    private JPanel createChatPanel(String selectedUser) {
        JPanel chatPanel = new JPanel(new BorderLayout());

        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        JButton uploadButton = new JButton("Upload File");

        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        messagePanel.add(uploadButton, BorderLayout.WEST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> {
            sendMessage();
        });

       /* uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int rVal = fileChooser.showOpenDialog(mainPanel.getParent());
            if (rVal == JFileChooser.APPROVE_OPTION) {
                byte[] selectedFile = new byte[(int) fileChooser.getSelectedFile().length()];
                BufferedInputStream bis;
                try {
                    bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                    // Đọc file vào biến selectedFile
                    bis.read(selectedFile, 0, selectedFile.length);

                    writer.write("File" + "\n");
                    //writer.write(lbReceiver.getText());
                    writer.write(fileChooser.getSelectedFile().getName());
                    writer.write(String.valueOf(selectedFile.length));

                    int size = selectedFile.length;
                    int bufferSize = 2048;
                    int offset = 0;

                    // Lần lượt gửi cho server từng buffer cho đến khi hết file
                    *//*while (size > 0) {
                        writer.write(selectedFile, offset, Math.min(size, bufferSize));
                        offset += Math.min(size, bufferSize);
                        size -= bufferSize;
                    }

                    writer.flush();*//*

                    bis.close();

                    // In ra màn hình file
                    //newFile(username, fileChooser.getSelectedFile().getName(), selectedFile, true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });*/

        return chatPanel;
    }

    private String loadChatHistory(String selectedUser) {
        // Replace this block with your logic to retrieve chat history with the selected user
        // For demonstration, returning sample chat history as a placeholder
        if (selectedUser.equals("User A")) {
            return "User A: Hi\nYou: Hello!\n";
        } else if (selectedUser.equals("User B")) {
            return "User B: Hey there\nYou: How are you?\n";
        }
        // If no chat history found (replace with your logic)
        return null;
    }

    private void sendMessage() {
        try {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                displayMessage("You", message);
                // Here, you would typically send the message to the server or process it further
                messageField.setText(""); // Clear the message field after sending
            }
            writer.write("MESSAGE" + "\n");
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            displayMessage("ERROR", "Network error!");
        }
    }

    public void displayMessage(String _from, String message) {
        String formattedMessage = _from + ": " + message + "\n";
        chatArea.append(formattedMessage);
    }
}
