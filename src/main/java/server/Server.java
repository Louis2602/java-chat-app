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
                } catch (IOException ioe) {
                    System.out.println("Server or client socket closed");
                }
            }).start();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            for (Client client : connectedClient)
                client.socket.close();
            s.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
