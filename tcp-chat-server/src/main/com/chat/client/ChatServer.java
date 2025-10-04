package com.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;

    // map of connected clients (username → handler)
    static final ConcurrentMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    // thread pool config
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final long KEEP_ALIVE = 60L;

    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(50),
            new ThreadPoolExecutor.AbortPolicy()
    );

    public static void main(String[] args) {
        System.out.println("[SERVER] Starting server on port " + PORT);

        // >>> ADICIONA ISTO <<<
        startAdminConsole();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[SERVER] Connection from " + socket.getInetAddress().getHostAddress());

                pool.execute(new ClientHandler(socket));
                logThreadPoolStatus();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public static void registerClient(String username, ClientHandler handler) {
        clients.put(username, handler);
    }

    public static void unregisterClient(String username) {
        clients.remove(username);
    }

    public static boolean usernameExists(String username) {
        return clients.containsKey(username);
    }

    public static void stopServer() {
        System.out.println("[SERVER] Shutting down...");
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("[SERVER] Forcing shutdown...");
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("[SERVER] Server stopped.");
        System.exit(0);
    }

    public static void logThreadPoolStatus() {
        System.out.println("[POOL] Active: " + pool.getActiveCount() +
                " | PoolSize: " + pool.getPoolSize() +
                " | Queue: " + pool.getQueue().size() +
                " | Completed: " + pool.getCompletedTaskCount());
    }

    private static void startAdminConsole() {
        new Thread(() -> {
            try (java.util.Scanner sc = new java.util.Scanner(System.in)) {
                while (true) {
                    String cmd = sc.nextLine().trim();
                    switch (cmd) {
                        case "/stats" -> logThreadPoolStatus();
                        case "/clients" -> System.out.println("Users: " + String.join(", ", clients.keySet()));
                        case "/shutdown" -> { stopServer(); return; }
                        default -> System.out.println("Unknown: /stats | /clients | /shutdown");
                    }
                }
            } catch (java.util.NoSuchElementException e) {
                System.out.println("[ADMIN] STDIN closed; admin console exiting.");
            }
        }, "admin-console").start();
    }
}

