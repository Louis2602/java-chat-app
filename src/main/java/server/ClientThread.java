package server;
import java.net.Socket;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ClientThread extends Thread {
    Client client;

    public ClientThread(Socket clientSocket) {
        try {
            client = new Client();
            client.socket = clientSocket;
            OutputStream os = clientSocket.getOutputStream();
            client.sender = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            InputStream is = clientSocket.getInputStream();
            client.receiver = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            client.port = clientSocket.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // handling client communication logic here
    }
}
