package hcmus.java.server;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Server {
    public static final String address = "localhost";
    public static final int port = 8080;
    public static final String ACCOUNTS_FILE = "accounts.txt";

    private ServerSocket serverSocket;

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    public static ArrayList<Group> groups = new ArrayList<>();

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running at port: " + port + ". Waiting for clients...");
            while (!serverSocket.isClosed()) {
                // Waiting request from clients
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                String request = reader.readLine();
                if (request == null)
                    throw new IOException();
                System.out.println("[SERVER]: Request from client: " + request);

                switch (request) {
                    case "REGISTER":
                        handleRegister(reader, writer);
                        break;
                    case "LOGIN":
                        handleLogin(socket, reader, writer);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void updateOnlineUsers() {
        StringBuilder message = new StringBuilder();
        for (ClientHandler client : clientHandlers) {
            if (client.getIsLoggedIn()) {
                message.append(client.getUsername());
                message.append(",");
            }
        }
        for (ClientHandler client : clientHandlers) {
            if (client.getIsLoggedIn()) {
                try {
                    client.getWriter().write("ONLINE USERS" + "\n");
                    client.getWriter().write(message + "\n");
                    client.getWriter().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void handleLogin(Socket socket, BufferedReader reader, BufferedWriter writer) throws IOException {
        String username = reader.readLine();
        String password = reader.readLine();

        try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    // If login successful
                    ClientHandler newHandler = new ClientHandler(socket);
                    clientHandlers.add(newHandler);
                    newHandler.setIsLoggedIn(true);
                    newHandler.setUsername(username);

                    writer.write("LoginSuccessful");
                    writer.newLine();
                    writer.flush();
                    // Create a thread for this user
                    new Thread(newHandler).start();
                    updateOnlineUsers();

                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.write("InvalidCredentials");
        writer.newLine();
        writer.flush();
    }

    private void handleRegister(BufferedReader reader, BufferedWriter writer) throws IOException {
        String username = reader.readLine();
        String password = reader.readLine();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ACCOUNTS_FILE, true))) {
            try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length > 0 && parts[0].equals(username)) {
                        writer.write("UsernameExists");
                        writer.newLine();
                        writer.flush();
                        return; // Username already exists
                    }
                }
            }
            bw.write(username + ":" + password + "\n");
            writer.write("RegistrationSuccessful");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
