package com.chat.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private final ProtocolHandler protocolHandler;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.protocolHandler = new ProtocolHandler();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // ask for username
            out.println("Enter your username:");
            username = in.readLine();

            // check duplicates
            while (username == null || username.isBlank() || ChatServer.usernameExists(username)) {
                out.println("Invalid or taken. Enter a different username:");
                username = in.readLine();
            }

            ChatServer.registerClient(username, this);
            ChatServer.broadcast("[SERVER] " + username + " joined the chat.");

            String input;
            while ((input = in.readLine()) != null) {
                if (input.startsWith("/")) {
                    handleCommand(input);
                } else {
                    String response = protocolHandler.process(input);
                    ChatServer.broadcast("[" + username + "] " + response);
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error with " + username + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleCommand(String msg) {
        String[] parts = msg.split(" ", 3);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/quit":
                sendMessage("[SERVER] You left the chat.");
                cleanup();
                break;

            case "/list":
                sendMessage("Online users: " + String.join(", ", ChatServer.clients.keySet()));
                break;

            case "/name":
                if (parts.length < 2) {
                    sendMessage("Usage: /name <newName>");
                } else {
                    String newName = parts[1];
                    if (ChatServer.usernameExists(newName)) {
                        sendMessage("Name already in use.");
                    } else {
                        String old = this.username;
                        ChatServer.unregisterClient(old);
                        this.username = newName;
                        ChatServer.registerClient(newName, this);
                        ChatServer.broadcast("[SERVER] " + old + " is now " + newName);
                    }
                }
                break;

            case "/whisper":
                if (parts.length < 3) {
                    sendMessage("Usage: /whisper <user> <msg>");
                } else {
                    String target = parts[1];
                    String text = parts[2];
                    ClientHandler ch = ChatServer.clients.get(target);
                    if (ch != null) {
                        ch.sendMessage("(whisper) " + username + ": " + text);
                    } else {
                        sendMessage("User '" + target + "' not found.");
                    }
                }
                break;

            case "/help":
                sendMessage("Available commands:");
                sendMessage("/quit - Leave chat");
                sendMessage("/list - List online users");
                sendMessage("/name <newName> - Change nickname");
                sendMessage("/whisper <user> <msg> - Private message");
                sendMessage("/help - Show help");
                break;

            default:
                sendMessage("Unknown command. Type /help for list.");
        }
    }

    private void cleanup() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        ChatServer.unregisterClient(username);
        ChatServer.broadcast("[SERVER] " + username + " left the chat.");
    }

    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
}
