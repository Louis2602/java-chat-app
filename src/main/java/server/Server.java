package server;

import java.io.*;
import java.net.*;

public class Server {
    private static final int port = 8080;
    public static final String ACCOUNTS_FILE = "accounts.txt";

    private ServerSocket serverSocket;

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running at port: " + port + ". Waiting for clients...");
            while(!serverSocket.isClosed()) {
                // Waiting request from clients
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");

                new Thread(new ClientHandler(socket)).start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void closeServerSocket() {
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
