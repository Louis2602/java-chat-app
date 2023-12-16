package client;

import java.io.*;
import java.util.Arrays;

public class ChatHistory {

    public static String getChatHistoryFolderPath() {
        // Define the folder path for storing chat history
        return "./history/";
    }
    public static String getChatHistoryFileName(String user1, String user2) {
        // To ensure consistency, always arrange the usernames in the same order
        String[] users = {user1, user2};
        Arrays.sort(users);

        // Generate a unique filename based on the sorted usernames
        return getChatHistoryFolderPath() + users[0] + "_" + users[1] + "_chat_history.txt";
    }

    public static void saveChatHistory(String sender, String recipient, String message) {
        String fileName = getChatHistoryFileName(sender, recipient);

        try {
            // Create the directory if it doesn't exist
            new File(getChatHistoryFolderPath()).mkdirs();

            // Write the chat history to the specified file
            try (FileWriter fileWriter = new FileWriter(fileName, true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(sender + ": " + message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String loadChatHistory(String sender, String recipient) {
        String fileName = getChatHistoryFileName(sender, recipient);
        StringBuilder chatHistory = new StringBuilder();

        try (FileReader fileReader = new FileReader(fileName);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(": ", 2);
                if (parts.length == 2) {
                    String senderName = parts[0];
                    String message = parts[1];
                    // Append the sender and message in the expected format
                    chatHistory.append(senderName).append(": ").append(message).append("\n");
                }
            }
        } catch (FileNotFoundException e) {
            // If the file doesn't exist, return an empty chat history
            return "";
        } catch (IOException e) {
            // Handle file not found or other IO exceptions
            e.printStackTrace();
        }

        return chatHistory.toString();
    }
}
