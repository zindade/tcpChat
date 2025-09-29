package com.chat.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private ProtocolHandler protocolHandler;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.protocolHandler = new ProtocolHandler();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter outWriter = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = outWriter;

            // adicionar este cliente à lista global
            ChatServer.clientWriters.add(out);

            String input;
            while ((input = in.readLine()) != null) {
                String response = protocolHandler.process(input);

                if ("SHUTDOWN".equals(response)) {
                    ChatServer.broadcast("Server is shutting down...");
                    System.exit(0);
                } else {
                    // enviar para todos (broadcast)
                    ChatServer.broadcast("[" + socket.getInetAddress().getHostAddress() + "] " + response);
                }
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + socket.getInetAddress().getHostAddress());
        } finally {
            if (out != null) {
                ChatServer.clientWriters.remove(out); // remover cliente da lista
            }
        }
    }
}
