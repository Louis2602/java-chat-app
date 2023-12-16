package client;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
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
                    case "MESSAGE":
                        handleMessageFromClient();
                        break;
                    case "SAFE TO LEAVE":
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleMessageFromClient() throws IOException {
        String sender =	reader.readLine();
        String message = reader.readLine();
        chatApp.displayMessage(sender, message);
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
    }
}
