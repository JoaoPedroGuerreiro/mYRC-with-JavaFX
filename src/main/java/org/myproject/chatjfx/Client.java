package org.myproject.chatjfx;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedWriter socketOutputWriter;
    private BufferedReader socketInputReader;

    public Client(String serverAddress, int serverPort, ChatController chatController) {
        try {
            socket = new Socket(serverAddress, serverPort);
            socketOutputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            socketInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread messageReceiver = new Thread(new Chat(socketInputReader, chatController));
            messageReceiver.start();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        try {
            socketOutputWriter.write(message);
            socketOutputWriter.newLine();
            socketOutputWriter.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}