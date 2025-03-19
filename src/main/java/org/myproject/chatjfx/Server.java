package org.myproject.chatjfx;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private final int PORT;
    private final List<ServerWorker> workers = new CopyOnWriteArrayList<>();

    public Server(int port) {
        this.PORT = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                ServerWorker worker = new ServerWorker(clientSocket);
                workers.add(worker);
                new Thread(worker).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private void sendToAll(String message, ServerWorker sender) {
        for (ServerWorker worker : workers) {
            if (worker.getNickName() != null && worker != sender) {
                worker.send(sender.getNickName() + ": " + message);
            }
        }
    }

    private void whisper(String message, ServerWorker sender, String recipient) {
        for (ServerWorker worker : workers) {
            if (worker.getNickName() != null && worker.getNickName().equals(recipient)) {
                worker.send(sender.getNickName() + " [whisper]: " + message);
                return;
            }
        }
        sender.send("User '" + recipient + "' is offline or not found.");
    }

    public void removeWorker(ServerWorker worker) {
        workers.remove(worker);
    }

    private class ServerWorker implements Runnable {
        private final Socket socket;
        private BufferedReader inputReader;
        private BufferedWriter outputWriter;
        private String nickName;

        public ServerWorker(Socket socket) {
            this.socket = socket;
        }

        public String getNickName() {
            return nickName;
        }

        public void send(String message) {
            try {
                outputWriter.write(message);
                outputWriter.newLine();
                outputWriter.flush();
            } catch (IOException e) {
                System.err.println("Error sending message to " + nickName + ": " + e.getMessage());
            }
        }

        private void handleChangeNickname(String[] parts) {
            if (parts.length < 2 || parts[1].trim().isEmpty()) {
                send("Invalid Nickname. Use: /name <new_nickname>");
                return;
            }
            String newNickname = parts[1].trim();
            for (ServerWorker worker : workers) {
                if (worker.getNickName() != null && worker.getNickName().equals(newNickname)) {
                    send("Nickname already in use. Choose another.");
                    return;
                }
            }
            this.nickName = newNickname;
            send("Nickname changed to: " + nickName);
        }

        private void handleWhisperCommand(String[] parts) {
            if (parts.length < 3) {
                send("Usage: /whisper <nickname> <message>");
                return;
            }
            String recipient = parts[1].trim();
            String message = String.join(" ", parts).substring(parts[0].length() + recipient.length() + 2);
            whisper(message, this, recipient);
        }

        private void handleCommand(String command) {
            String[] parts = command.split(" ", 3);
            if (command.startsWith("/name")) {
                handleChangeNickname(parts);
            } else if (command.startsWith("/whisper")) {
                handleWhisperCommand(parts);
            } else {
                send("Invalid Command: " + command);
            }
        }

        @Override
        public void run() {
            try {
                inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                send("Welcome to the Chat! Use /name <your_nickname> to set your nickname.");
                send("To whisper just type /whisper <nickname> <message>.");

                String message;
                while ((message = inputReader.readLine()) != null) {
                    if (message.startsWith("/")) {
                        handleCommand(message);
                    } else if (this.nickName != null) {
                        sendToAll(message, this);
                    } else {
                        send("Set your nickname first: /name <your_nickname>");
                    }
                }
            } catch (IOException e) {
                System.err.println("Client disconnected: " + e.getMessage());
            } finally {
                try {
                    if (inputReader != null) inputReader.close();
                    if (outputWriter != null) outputWriter.close();
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
                removeWorker(this);
                if (this.nickName != null) {
                    sendToAll(this.nickName + " has left the chat.", null);
                }
            }
        }
    }

    public static void main(String[] args) {
        Server chatServer = new Server(8888);
        chatServer.startServer();
    }
}
