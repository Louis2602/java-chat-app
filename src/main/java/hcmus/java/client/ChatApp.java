package hcmus.java.client;

import hcmus.java.server.ClientHandler;
import hcmus.java.server.Server;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class ChatApp extends JFrame {
    public JTextPane tabChatPane;
    public HashMap<String, JTextPane> userChatPanes = new HashMap<>();
    Thread clientReceiver;
    JScrollPane chatScrollPane;
    StyledDocument doc;
    Socket socket;
    private JList<String> onlineUsersList;
    private JList<String> groupsList;
    private JTextField messageField;
    private JList<String> listUsersInGroup;
    private String username;
    private String recipient = "";
    private BufferedReader reader;
    private BufferedWriter writer;
    private JTabbedPane conversationsTabbedPane;
    HashMap<Integer, String> groups = new HashMap<>();

    public ChatApp(String username, Socket socket, BufferedReader reader, BufferedWriter writer) {
        this.username = username;
        this.socket = socket;
        this.writer = writer;
        this.reader = reader;
        clientReceiver = new Thread(new ClientReceiver(reader, this));
        clientReceiver.start();

        createChatFrame();
    }

    public void updateOnlineUsersList(DefaultListModel<String> model) {
        onlineUsersList.setModel(model);
    }

    public void updateUserGroupsList(DefaultListModel<String> model) {
        groupsList.setModel(model);
    }

    public void updateListUsersInGroup(DefaultListModel<String> model) {
        listUsersInGroup.setModel(model);
    }

    public String getUsername() {
        return this.username;
    }

    private void updateListUsersInSelectedGroup(String selectedGroup) throws IOException {
        writer.write("GET USERS IN GROUP\n");
        writer.write(selectedGroup + "\n");
        writer.flush();
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
                if (e.getClickCount() == 2) { // Detect a single click on a user
                    JList<String> list = (JList<String>) e.getSource();
                    int index = list.locationToIndex(e.getPoint()); // Get the index of the clicked item
                    if (index >= 0) {
                        recipient = list.getModel().getElementAt(index);
                        // You can start a conversation with the selected user here
                        startConversation(recipient, false);
                    }
                }
            }
        });
        topPanel.add(onlineUsersPanel, BorderLayout.CENTER);

        // Chat area for current user
        tabChatPane = new JTextPane();
        tabChatPane.setEditable(false);
        userChatPanes.put(this.username, tabChatPane);
        chatScrollPane = new JScrollPane();
        chatScrollPane.setViewportView(tabChatPane);

        splitPane.setTopComponent(topPanel);

        JPanel groupsPanel = createPanelWithBorderLayout("Groups");

        groupsList = new JList<>();
        JScrollPane groupsScrollPane = new JScrollPane(groupsList);
        groupsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Detect a double-click
                    JList<String> list = (JList<String>) e.getSource();
                    int index = list.locationToIndex(e.getPoint()); // Get the index of the clicked item
                    if (index >= 0) {
                        String selectedGroup = list.getModel().getElementAt(index);
                        recipient = selectedGroup;
                        // Fetch users in the selected group and update the listUsersInGroup
                        try {
                            updateListUsersInSelectedGroup(selectedGroup);
                            // You can start a conversation with the selected user here
                            startConversation(selectedGroup, true);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        });

        JButton createGroupButton = new JButton("Create Group");
        createGroupButton.addActionListener(e -> {
            // Create a dialog
            JDialog dialog = new JDialog(this, "Create Group", true);
            dialog.setLayout(new BorderLayout());

            // Panel for components
            JPanel dialogPanel = new JPanel(new BorderLayout());

            // Field to enter group name
            JTextField groupNameField = new JTextField(20);
            JPanel groupNamePanel = new JPanel();
            groupNamePanel.add(new JLabel("Group Name:"));
            groupNamePanel.add(groupNameField);

            // List to choose users for the group
            ListModel<String> onlineUsersModel = onlineUsersList.getModel();
            JList<String> usersList = new JList<>(onlineUsersModel);
            JScrollPane usersScrollPane = new JScrollPane(usersList);

            // Add components to the dialog panel
            dialogPanel.add(groupNamePanel, BorderLayout.NORTH);
            dialogPanel.add(usersScrollPane, BorderLayout.CENTER);

            // Buttons for confirmation and cancel
            JPanel buttonsPanel = new JPanel();
            JButton confirmButton = new JButton("Create");
            confirmButton.addActionListener(confirmEvent -> {
                // Handle the creation of the group here
                String groupName = groupNameField.getText();
                if (groupName.isEmpty()) {
                    JOptionPane.showMessageDialog(ChatApp.this, "Tên group không được trống", "Lỗi tạo group",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                List<String> selectedUsers = usersList.getSelectedValuesList();
                selectedUsers.add(this.username);
                try {
                    writer.write("CREATE GROUP\n");
                    writer.write(groupName);
                    writer.newLine();
                    writer.write(selectedUsers.toString());
                    writer.newLine();
                    writer.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                dialog.dispose(); // Close the dialog after processing
            });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(cancelEvent -> dialog.dispose());

            buttonsPanel.add(confirmButton);
            buttonsPanel.add(cancelButton);

            // Add components to the dialog
            dialog.add(dialogPanel, BorderLayout.CENTER);
            dialog.add(buttonsPanel, BorderLayout.SOUTH);

            dialog.pack();
            dialog.setVisible(true);
        });
        groupsPanel.add(groupsScrollPane, BorderLayout.CENTER);
        groupsPanel.add(createGroupButton, BorderLayout.SOUTH);

        splitPane.setBottomComponent(groupsPanel);

        mainPanel.add(splitPane, BorderLayout.WEST);

        JPanel centerPanel = createPanelWithBorderLayout("Chat");
        conversationsTabbedPane = new JTabbedPane();
        centerPanel.add(conversationsTabbedPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = createPanelWithBorderLayout("Current users in Room");
        rightPanel.setLayout(new GridBagLayout());
        listUsersInGroup = new JList<>();

        JScrollPane currentChatUsersScrollPane = new JScrollPane(listUsersInGroup);
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

    private void startConversation(String selectedObject, Boolean isGroupChat) {
        /*
        selectedObject: could be a single user or a group chat
         */
        int tabCount = conversationsTabbedPane.getTabCount();
        boolean tabExists = false;
        int tabIndex = 0;
        for (int i = 0; i < tabCount; i++) {
            if (conversationsTabbedPane.getTitleAt(i).equals(selectedObject)) {
                tabExists = true;
                tabIndex = i;
                break;
            }
        }

        if (!tabExists) {
            JPanel chatPanel = createChatPanel(selectedObject, isGroupChat);
            JPanel tabHeader = createTabHeader(selectedObject);
            conversationsTabbedPane.addTab(selectedObject, chatPanel);
            conversationsTabbedPane.setTabComponentAt(conversationsTabbedPane.indexOfTab(selectedObject), tabHeader);
            tabIndex = conversationsTabbedPane.indexOfTab(selectedObject);
        }

        // Select the tab for the selected user
        conversationsTabbedPane.setSelectedIndex(tabIndex);
        String chatHistory = ChatHistory.loadChatHistory(username, selectedObject); // Load chat history
        tabChatPane = userChatPanes.get(selectedObject);
        if (tabChatPane != null && chatHistory != null && !chatHistory.isEmpty()) {
            tabChatPane.setText("");
            String[] lines = chatHistory.split("\n"); // Split the history into individual lines

            for (String line : lines) {
                String[] parts = line.split(": ", 2); // Split into sender and message parts
                String sender = parts[0];
                String message = parts[1];
                // If it is file
                if (message.startsWith("<") && message.endsWith(">")) {
                    String formattedFileName = message.substring(1, message.length() - 1); // Extract filename

                    String[] fileParts = formattedFileName.split("\\.", 2);
                    String fileNamePrefix = fileParts[0];
                    String realFileName = fileNamePrefix.split("_")[0];
                    String fileExt = fileParts[1];
                    String filename = realFileName + "." + fileExt;

                    // Read data back from files folder
                    try {
                        String saveFileName = "files/" + formattedFileName;
                        File file = new File(saveFileName);
                        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

                        if (file.exists()) {
                            FileInputStream inputStream = new FileInputStream(file);

                            byte[] buffer = new byte[(int) file.length()];
                            int bytesRead;
                            // Read file content into the buffer
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                byteArray.write(buffer, 0, bytesRead);
                            }

                            inputStream.close();
                        } else {
                            System.out.println("File does not exist!");
                        }

                        // Display file entry using displayFile method
                        if (sender.equals(this.username)) {
                            displayFile(sender, filename, byteArray.toByteArray(), true, "");
                        } else {
                            displayFile(sender, filename, byteArray.toByteArray(), false, "");
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                } else {
                    if (sender.equals(this.username)) {
                        displayMessage(sender, message, true, "");
                    } else {
                        displayMessage(sender, message, false, "");
                    }
                }
            }
        }
    }

    private JPanel createTabHeader(String selectedObject) {
        JPanel tabHeader = new JPanel(new BorderLayout());
        tabHeader.setOpaque(false); // Make the tab header transparent

        JLabel titleLabel = new JLabel(selectedObject);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5)); // Add padding between label and button

        JButton closeButton = new JButton("x");
        closeButton.setFocusable(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setForeground(Color.RED);
        closeButton.addActionListener(e -> {
            int tabIndex = conversationsTabbedPane.indexOfTab(selectedObject);
            if (tabIndex >= 0) {
                conversationsTabbedPane.remove(tabIndex);
            }
        });

        tabHeader.add(titleLabel, BorderLayout.CENTER);
        tabHeader.add(closeButton, BorderLayout.EAST);

        return tabHeader;
    }

    private JPanel createChatPanel(String selectedObject, Boolean isGroupChat) {
        JPanel chatPanel = new JPanel(new BorderLayout());
        String groupName = isGroupChat ? selectedObject : "";

        if (tabChatPane != userChatPanes.get(selectedObject)) {
            tabChatPane = userChatPanes.get(selectedObject);

        }
        chatScrollPane.setViewportView(tabChatPane);
        chatScrollPane.validate();
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
            sendMessage(selectedObject, isGroupChat);
        });

        // handle file
        uploadButton.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle("Chọn file để gửi");
            int result = jfc.showDialog(null, "Chọn file");
            if (result == JFileChooser.APPROVE_OPTION) {
                String fileName = jfc.getSelectedFile().getName();
                String filePath = jfc.getSelectedFile().getAbsolutePath();
                byte[] selectedFile = new byte[(int) jfc.getSelectedFile().length()];
                BufferedInputStream bis;

                File file = new File(filePath);
                try {
                    bis = new BufferedInputStream(new FileInputStream(jfc.getSelectedFile()));
                    bis.read(selectedFile, 0, selectedFile.length);

                    System.out.println("Send file " + fileName + " to user: " + selectedObject);
                    String[] fileParts = fileName.split("\\.", 2);
                    String fileNamePrefix = fileParts[0];
                    String fileExt = fileParts[1];
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    String formattedFileName = fileNamePrefix + "_" + timeStamp + "." + fileExt;
                    ChatHistory.saveChatHistory(username, selectedObject, "<" + formattedFileName + ">");

                    Integer groupId = -1;
                    for (Map.Entry<Integer, String> entry : groups.entrySet()) {
                        if (entry.getValue().equals(selectedObject)) {
                            groupId = entry.getKey();
                            break;
                        }
                    }
                    assert groupId != null;
                    writer.write("FILE" + "\n");
                    writer.write(groupId.toString());
                    writer.newLine();
                    writer.write(groupName);
                    writer.newLine();
                    writer.write(recipient);
                    writer.newLine();
                    writer.write(formattedFileName);
                    writer.newLine();
                    writer.write(String.valueOf(file.length()));
                    writer.newLine();
                    writer.flush();

                    // Send the file content
                    int bufferSize = 1000000;
                    byte[] buffer = new byte[bufferSize];
                    InputStream in = new FileInputStream(file);
                    OutputStream out = socket.getOutputStream();

                    int count;
                    while ((count = in.read(buffer)) > 0) {
                        out.write(buffer, 0, count);
                    }

                    in.close();
                    out.flush();

                    displayFile(username, fileName, selectedFile, true, "");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        return chatPanel;
    }


    private void sendMessage(String selectedObject, Boolean isGroupChat) {
        if (!isGroupChat) {
            try {
                String message = messageField.getText();
                if (!message.isEmpty()) {
                    displayMessage(username, message, true, "");
                    ChatHistory.saveChatHistory(username, selectedObject, message);
                    messageField.setText(""); // Clear the message field after sending
                }
                writer.write("MESSAGE" + "\n");
                writer.write(message);
                writer.newLine();
                writer.write(selectedObject); // recipient name
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                String message = messageField.getText();
                if (!message.isEmpty()) {
                    displayMessage(username, message, true, selectedObject);
                    ChatHistory.saveChatHistory(username, selectedObject, message);
                    messageField.setText(""); // Clear the message field after sending
                }
                Integer groupId = null;
                for (Map.Entry<Integer, String> entry : groups.entrySet()) {
                    if (entry.getValue().equals(selectedObject)) {
                        groupId = entry.getKey();
                        break;
                    }
                }
                writer.write("MESSAGE GROUP CHAT" + "\n");
                writer.write(message);
                writer.newLine();
                assert groupId != null;
                writer.write(groupId.toString()); // group id
                writer.newLine();
                writer.write(selectedObject); // group name
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void displayMessage(String sender, String message, Boolean yourMessage, String groupName) {
        if(groupName.isEmpty()) {
            if (sender.equals(this.username)) {
                doc = userChatPanes.get(recipient).getStyledDocument();
            } else {
                doc = userChatPanes.get(sender).getStyledDocument();
            }
        } else {
            if (sender.equals(this.username)) {
                doc = userChatPanes.get(recipient).getStyledDocument();
            } else {
                doc = userChatPanes.get(groupName).getStyledDocument();
            }
        }

        Style userStyle = doc.getStyle("User style");

        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }

        if (yourMessage) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }

        // In ra tên người gửi
        try {
            if (yourMessage) {
                doc.insertString(doc.getLength(), "You: ", userStyle);
            } else {
                doc.insertString(doc.getLength(), sender + ": ", userStyle);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        Style messageStyle = doc.getStyle("Message style");
        if (messageStyle == null) {
            messageStyle = doc.addStyle("Message style", null);
            StyleConstants.setForeground(messageStyle, Color.BLACK);
            StyleConstants.setBold(messageStyle, false);
        }

        // In ra nội dung tin nhắn
        try {
            doc.insertString(doc.getLength(), message + "\n", messageStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void displayFile(String sender, String filename, byte[] file, Boolean yourMessage, String groupName) {
        String window = null;
        if(groupName.isEmpty()) {
            if (sender.equals(this.username)) {
                window = recipient;
            } else {
                window = sender;
            }
        } else {
            if (sender.equals(this.username)) {
                window = recipient;
            } else {
                window = groupName;
            }
        }

        doc = userChatPanes.get(window).getStyledDocument();
        Style userStyle = doc.getStyle("User style");

        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }
        if (yourMessage) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }

        try {
            if (yourMessage) {
                doc.insertString(doc.getLength(), "You: ", userStyle);
            } else {
                doc.insertString(doc.getLength(), sender + ": ", userStyle);
            }
        } catch (BadLocationException e) {
        }

        Style linkStyle = doc.getStyle("Link style");
        if (linkStyle == null) {
            linkStyle = doc.addStyle("Link style", null);
            StyleConstants.setForeground(linkStyle, Color.BLUE);
            StyleConstants.setUnderline(linkStyle, true);
            StyleConstants.setBold(linkStyle, true);
            linkStyle.addAttribute("link", new HyberlinkListener(filename, file));
        }

        if (userChatPanes.get(window).getMouseListeners() != null) {
            // Tạo MouseListener cho các đường dẫn tải về file
            userChatPanes.get(window).addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Element ele = doc.getCharacterElement(tabChatPane.viewToModel(e.getPoint()));
                    AttributeSet as = ele.getAttributes();
                    HyberlinkListener listener = (HyberlinkListener) as.getAttribute("link");
                    if (listener != null) {
                        listener.execute();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

            });
        }

        // In ra đường dẫn tải file
        try {
            doc.insertString(doc.getLength(), "<" + filename + ">", linkStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        // Xuống dòng
        try {
            doc.insertString(doc.getLength(), "\n", userStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }

    class HyberlinkListener extends AbstractAction {
        String filename;
        byte[] file;

        public HyberlinkListener(String filename, byte[] file) {
            this.filename = filename;
            this.file = Arrays.copyOf(file, file.length);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            execute();
        }

        public void execute() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(filename));
            int rVal = fileChooser.showSaveDialog(tabChatPane.getParent());
            if (rVal == JFileChooser.APPROVE_OPTION) {

                // Mở file đã chọn sau đó lưu thông tin xuống file đó
                File saveFile = fileChooser.getSelectedFile();
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(saveFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // Hiển thị JOptionPane cho người dùng có muốn mở file vừa tải về không
                int nextAction = JOptionPane.showConfirmDialog(null, "Saved file to " + saveFile.getAbsolutePath() + "\nDo you want to open this file?", "Successful", JOptionPane.YES_NO_OPTION);
                if (nextAction == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().open(saveFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (bos != null) {
                    try {
                        bos.write(this.file);
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}


