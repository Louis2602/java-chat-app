package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String clientUsername;


    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    @Override
    public void run() {
        String request;
        while (socket.isConnected()) {
            try {
                request = reader.readLine();
                System.out.println("Request from client: " + request);
                //broadcastMessage(messageFromClient);
                switch (request) {
                    case "REGISTER":
                        handleRegister(reader, writer);
                        break;
                    case "LOGIN":
                        handleLogin(reader, writer);
                        break;
                }
            } catch (IOException e) {
                closeEverything(socket, reader, writer);
                break;
            }
        }
    }

    private void handleLogin(BufferedReader reader, BufferedWriter writer) throws IOException {
        String username = reader.readLine();
        String password = reader.readLine();

        try (BufferedReader br = new BufferedReader(new FileReader(Server.ACCOUNTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    writer.write("LoginSuccessful");
                    writer.newLine();
                    writer.flush();
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

        System.out.println(username);
        System.out.println(password);


        try (BufferedWriter bw = new BufferedWriter(new FileWriter(Server.ACCOUNTS_FILE, true))) {
            try (BufferedReader br = new BufferedReader(new FileReader(Server.ACCOUNTS_FILE))) {
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

    /*
    Send the message to all the clients in a group chat, except the one who send
     */
    public void broadcastMessage(String messageToSend) {
        for(ClientHandler clientHandler : clientHandlers) {
            try {
                if(!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.writer.write(messageToSend);
                    clientHandler.writer.newLine();
                    clientHandler.writer.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, reader, writer);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage(clientUsername + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
        removeClientHandler();
        try {
            if(reader != null) {
                reader.close();
            }
            if(writer != null) {
                writer.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
