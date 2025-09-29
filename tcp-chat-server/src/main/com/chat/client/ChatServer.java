package com.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 12345;

    static Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());

                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }
}
