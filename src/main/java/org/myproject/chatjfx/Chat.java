package org.myproject.chatjfx;

import java.io.BufferedReader;
import java.io.IOException;

public class Chat implements Runnable {
    private final BufferedReader serverInputReader;
    private final ChatController chatController;

    public Chat(BufferedReader serverInputReader, ChatController chatController) {
        this.serverInputReader = serverInputReader;
        this.chatController = chatController;
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            while ((serverMessage = serverInputReader.readLine()) != null) {
                chatController.appendToChatArea(serverMessage);
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            System.err.println("Error reading messages from the server: " + e.getMessage());
        }
    }
}
