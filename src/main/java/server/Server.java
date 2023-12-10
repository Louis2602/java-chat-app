package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    /*
    Server Config
     */
    final int port = 8080;
    ServerSocket s;
    public List<Client> connectedClient;
    public List<Group> allGroups;
    private String accountLists = "data\\accounts.txt";

    public Server() {
        try {
            s = new ServerSocket(port);
            connectedClient = new ArrayList<Client>();
            allGroups = new ArrayList<Group>();

            new Thread(() -> {
                try {
                    do {
                        System.out.println("Waiting for client");

                        Socket clientSocket = s.accept();

                        ClientThread clientCommunicator = new ClientThread(clientSocket);
                        clientCommunicator.start();

                    } while (s != null && !s.isClosed());
                } catch (IOException e) {
                    System.out.println("Server or client socket closed");
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CloseSocket() {
        try {
            for (Client client : connectedClient)
                client.socket.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIPAddress() {
        String ip = "";
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            ip = socket.getLocalAddress().getHostAddress();
            socket.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
        return ip;
    }
}
