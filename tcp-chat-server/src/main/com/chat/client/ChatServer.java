package com.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;

    // conjunto de clientes conectados
    static final Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    // configuração do pool
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final long KEEP_ALIVE = 60L;

    // fila limitada para evitar overload
    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(50), // até 50 clientes em espera
            new ThreadPoolExecutor.AbortPolicy() // rejeita novas conexões quando cheio
    );

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());

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
        for (PrintWriter writer : clientWriters) {
            try {
                writer.println(message);
            } catch (Exception ex) {
                clientWriters.remove(writer);
            }
        }
    }

    static void registerClient(PrintWriter w) {
        clientWriters.add(w);
    }

    static void unregisterClient(PrintWriter w) {
        if (w != null) clientWriters.remove(w);
    }

    // método de shutdown gracioso
    public static void stopServer() {
        System.out.println("[SERVER] Encerrando thread pool...");
        pool.shutdown(); // não aceita novas tarefas
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("[SERVER] Forçando encerramento...");
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
        System.out.println("[SERVER] Servidor encerrado com sucesso.");
        System.exit(0);
    }

    // log do estado do pool
    public static void logThreadPoolStatus() {
        System.out.println("[POOL] Ativos: " + pool.getActiveCount() +
                " | PoolSize: " + pool.getPoolSize() +
                " | Queue: " + pool.getQueue().size() +
                " | Completed: " + pool.getCompletedTaskCount());
    }
}
