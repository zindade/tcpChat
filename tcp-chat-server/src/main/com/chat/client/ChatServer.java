package com.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 12345;


    static final Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();


    private static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());


                pool.execute(new ClientHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdownNow();
        }
    }


    public static void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            try {
                writer.println(message);

            } catch (Exception ex) {

                clientWriters.remove(writer);
            }
        }
    }

    //
    static void registerClient(PrintWriter w) {
        clientWriters.add(w);
    }

    static void unregisterClient(PrintWriter w) {
        if (w != null) clientWriters.remove(w);
    }
}
