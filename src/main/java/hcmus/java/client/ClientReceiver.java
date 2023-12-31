package hcmus.java.client;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientReceiver implements Runnable {
    private BufferedReader reader;
    private ChatApp chatApp;

    public ClientReceiver(BufferedReader reader, ChatApp chatApp) {
        this.chatApp = chatApp;
        this.reader = reader;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String request = reader.readLine();
                if (request == null)
                    throw new IOException();
                System.out.println("[Client]: Request from client " + chatApp.getUsername() + ": " + request);

                switch (request) {
                    case "ONLINE USERS":
                        retrieveOnlineUsers();
                        break;
                    case "GROUPS":
                        retrieveGroupUsers();
                        break;
                    case "MESSAGE":
                        handleMessageFromClient();
                        break;
                    case "MESSAGE GROUP":
                        handleMessageFromGroup();
                        break;
                    case "USERS IN GROUP":
                        retrieveUsersInGroup();
                        break;
                    case "FILE":
                        handleFileFromClient();
                        break;
                    case "SAFE TO LEAVE":
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFileFromClient() throws IOException {
        String groupName = reader.readLine();
        String sender = reader.readLine();
        String filename = reader.readLine();

        int size = Integer.parseInt(reader.readLine());
        System.out.println("Receive file " + filename + " with size: " + size + " from " + sender);

        int bufferSize = 1000000; // 1mb
        byte[] buffer = new byte[bufferSize];
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        InputStream in = this.chatApp.socket.getInputStream();
        int bytesRead;
        while ((bytesRead = in.read(buffer, 0, Math.min(bufferSize, size))) != -1 && size > 0) {
            file.write(buffer, 0, bytesRead);
            size -= bytesRead;
        }
        if(groupName.isEmpty()) {
            chatApp.displayFile(sender, filename, file.toByteArray(), false, "");
        } else {
            chatApp.displayFile(sender, filename, file.toByteArray(), false, groupName);
        }
    }

    private void handleMessageFromClient() throws IOException {
        String sender = reader.readLine();
        String message = reader.readLine();
        chatApp.displayMessage(sender, message, false, "");
    }

    private void handleMessageFromGroup() throws IOException {
        String sender = reader.readLine();
        String groupName = reader.readLine();
        String message = reader.readLine();

        chatApp.displayMessage(sender, message, false, groupName);
    }

    private void retrieveOnlineUsers() throws IOException {
        String users = reader.readLine();
        String[] userArray = users.split(",");
        List<String> onlineUsers = new ArrayList<>(Arrays.asList(userArray));
        onlineUsers.remove(chatApp.getUsername());

        SwingUtilities.invokeLater(() -> {
            // Clear the current list and update with received online users
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addAll(onlineUsers);
            chatApp.updateOnlineUsersList(model);
        });
        for (String user : onlineUsers) {
            if (!user.equals(chatApp.getUsername())) {
                if (chatApp.userChatPanes.get(user) == null) {
                    System.out.println("CREATING CHAT AREA FOR ALL ONLINE USERS...");
                    System.out.println("USER: " + user);
                    JTextPane userChatPane = new JTextPane();
                    userChatPane.setEditable(false);
                    chatApp.userChatPanes.put(user, userChatPane);
                }
            }
        }
    }

    private void retrieveUsersInGroup() throws IOException {
        String usersInGroup = reader.readLine();
        usersInGroup = usersInGroup.substring(1, usersInGroup.length() - 1); // Remove the square brackets
        String[] userArray = usersInGroup.split(", "); // Split the string by ", "
        List<String> userList = Arrays.asList(userArray);

        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addAll(userList);
            chatApp.updateListUsersInGroup(model);
        });
    }

    private void retrieveGroupUsers() throws IOException {
        String groups = reader.readLine();
        String[] groupArray = groups.split(",");
        List<String> groupsName = new ArrayList<>();
        for(String group : groupArray) {
            String[] parts = group.split("-");
            Integer groupId = Integer.parseInt(parts[0]);
            String groupName = parts[1];
            chatApp.groups.put(groupId, groupName);
            groupsName.add(groupName);
        }
        List<String> userGroups = new ArrayList<>(groupsName);

        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addAll(userGroups);
            chatApp.updateUserGroupsList(model);
        });

        for (String groupName : userGroups) {
            if (chatApp.userChatPanes.get(groupName) == null) {
                System.out.println("CREATING CHAT AREA FOR ALL GROUPS OF USER...");
                System.out.println("GROUP: " + groupName);
                JTextPane userChatPane = new JTextPane();
                userChatPane.setEditable(false);
                chatApp.userChatPanes.put(groupName, userChatPane);
            }
        }
    }
}
