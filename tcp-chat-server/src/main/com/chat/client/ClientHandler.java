package com.chat.server;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String input;
            while ((input = in.readLine()) != null) {
                if (input.startsWith("0")) {
                    String response = input.substring(1).toUpperCase() + " [" + LocalDateTime.now() + "]";
                    out.println(response);
                } else if (input.startsWith("1")) {
                    if (input.substring(1).equalsIgnoreCase("SHUTDOWN")) {
                        out.println("Server shutting down...");
                        System.exit(0);
                    } else {
                        out.println("Unknown command [" + LocalDateTime.now() + "]");
                    }
                } else {
                    out.println("Unknown protocol type [" + LocalDateTime.now() + "]");
                }
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + socket.getInetAddress().getHostAddress());
        }
    }
}
