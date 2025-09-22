package com.chat.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                    System.out.println("Client connected from " + socket.getInetAddress().getHostAddress());

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Received: " + line);
                        writer.println("Echo: " + line); // devolve ao cliente
                    }

                    System.out.println("Client disconnected");
                } catch (IOException e) {
                    System.out.println("Exception caught: " + e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
