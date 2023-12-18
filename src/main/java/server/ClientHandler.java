package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String clientUsername;
    private boolean isLoggedIn;


    public ClientHandler(Socket socket) throws IOException {
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
                // Read request from client
                request = reader.readLine();
                if (request == null)
                    throw new IOException();
                System.out.println("[ClientHandler]: Request from " + clientUsername + ": " + request);
                switch (request) {
                    case "LOGOUT":
                        handleLogout(writer);
                        break;
                    case "MESSAGE":
                        broadcastMessageToClient();
                        break;
                    case "FILE":
                        handleReceiveFile();
                        break;
                }
            } catch (IOException e) {
                try {
                    closeEverything(socket, reader, writer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
        }
    }

    public BufferedWriter getWriter() {
        return this.writer;
    }

    public BufferedReader getReader() {
        return this.reader;
    }

    public String getUsername() {
        return this.clientUsername;
    }

    public void setUsername(String username) {
        this.clientUsername = username;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public boolean getIsLoggedIn() {
        return this.isLoggedIn;
    }

    private void handleLogout(BufferedWriter writer) throws IOException {
        writer.write("SAFE TO LEAVE" + "\n");
        writer.flush();
        closeEverything(socket, reader, writer);
        isLoggedIn = false;
        Server.updateOnlineUsers();
    }

    /*
    Send the message to all the clients in a group chat, except the one who send
     */
    public void broadcastMessage(String messageToSend) throws IOException {
        for (ClientHandler clientHandler : Server.clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.writer.write("MESSAGE" + "\n");
                    clientHandler.writer.write(clientUsername);
                    clientHandler.writer.newLine();
                    clientHandler.writer.write(messageToSend);
                    clientHandler.writer.newLine();
                    clientHandler.writer.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, reader, writer);
            }
        }
    }

    /*
    Send the message to the recipient
     */
    public void broadcastMessageToClient() throws IOException {
        String messageToSend = reader.readLine();
        String recipient = reader.readLine();
        for (ClientHandler clientHandler : Server.clientHandlers) {
            try {
                if (clientHandler.clientUsername.equals(recipient)) {
                    clientHandler.writer.write("MESSAGE" + "\n");
                    clientHandler.writer.write(this.clientUsername); // sender
                    clientHandler.writer.newLine();
                    clientHandler.writer.write(messageToSend);
                    clientHandler.writer.newLine();
                    clientHandler.writer.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, reader, writer);
            }
        }
    }

    public void handleReceiveFile() throws IOException {
        String recipient = reader.readLine();
        String filename = reader.readLine();
        int size = Integer.parseInt(reader.readLine());
        int bufferSize = 2048;
        byte[] buffer = new byte[bufferSize];

        for (ClientHandler client : Server.clientHandlers) {
            if (client.getUsername().equals(recipient)) {
                client.writer.write("FILE" + "\n");
                client.writer.write(this.clientUsername);
                client.writer.newLine();
                client.writer.write(filename);
                client.writer.newLine();
                client.writer.write(String.valueOf(size));
                client.writer.newLine();

                while (size > 0) {
                    // Gửi lần lượt từng buffer cho người nhận cho đến khi hết file
                    reader.read(Arrays.toString(buffer).toCharArray(), 0, Math.min(size, bufferSize));
                    client.writer.write(Arrays.toString(buffer), 0, Math.min(size, bufferSize));
                    client.writer.newLine();
                    size -= bufferSize;
                }
                client.writer.flush();
                break;
            }
        }
    }

    public void removeClientHandler() {
        Server.clientHandlers.remove(this);
    }

    public void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) throws IOException {
        removeClientHandler();
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
