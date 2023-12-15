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
                System.out.println("[Client]: Request from client: " + request);

                switch (request) {
                    case "ONLINE USERS":
                        retrieveOnlineUsers();
                        break;
                    case "SAFE TO LEAVE":
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        //onlineUsers.removeAllItems();

        //String chatting = lbReceiver.getText();

        //boolean isChattingOnline = false;

       /* for (String user: users) {
            if (user.equals(username) == false) {
                // Cập nhật danh sách các người dùng trực tuyến vào ComboBox onlineUsers (trừ bản thân)
                ChatApp.onlineUsersList.addItem(user);
                if (chatWindows.get(user) == null) {
                    JTextPane temp = new JTextPane();
                    temp.setFont(new Font("Arial", Font.PLAIN, 14));
                    temp.setEditable(false);
                    chatWindows.put(user, temp);
                }
            }
            if (chatting.equals(user)) {
                isChattingOnline = true;
            }
        }*/
    }
}
