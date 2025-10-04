package com.chat.server;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private Scanner scanner;
    private PrintWriter out;
    private BufferedReader in;

    private String lastSent = null;

    // Histórico local
    private final List<String> history = new ArrayList<>();
    private static final int MAX_HISTORY = 2000;

    public Client(String addr, int port) {
        connectToServer(addr, port);
    }

    /** Connects to the server and starts communication */
    private void connectToServer(String addr, int port) {
        try {
            socket = new Socket(addr, port);
            System.out.println("Connected to " + addr + ":" + port);

            setupStreams();
            listenForMessages();
            sendMessages();

        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    /** Initializes input/output streams */
    private void setupStreams() throws IOException {
        scanner = new Scanner(System.in);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /** Separate thread that keeps listening for messages from the server */
    private void listenForMessages() {
        Thread reader = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String stamped = line + " [" + LocalTime.now().withNano(0) + "]";
                    synchronized (history) {
                        history.add(stamped);
                        if (history.size() > MAX_HISTORY) history.remove(0);
                    }
                    System.out.println(stamped);
                }
                System.out.println("[INFO] Disconnected from server.");
            } catch (IOException e) {
                System.out.println("[INFO] Connection closed: " + e.getMessage());
            }
        }, "server-listener");

        reader.setDaemon(true);
        reader.start();
    }

    /** Loop to send messages to the server */
    private void sendMessages() {
        try {
            while (true) {
                System.out.print("You (type '/history [n]', '/repeat' or 'exit'): ");
                String msg = scanner.nextLine();

                if ("exit".equalsIgnoreCase(msg)) {
                    System.out.println("Closing connection...");
                    break;
                }

                // -------- comandos locais --------
                if (msg.startsWith("/history")) {
                    String[] t = msg.trim().split("\\s+");
                    int n = (t.length > 1) ? parseIntSafe(t[1], 20) : 20; // default 20
                    List<String> snapshot;
                    synchronized (history) {
                        snapshot = new ArrayList<>(history);
                    }
                    if (snapshot.isEmpty()) {
                        System.out.println("[local] Sem histórico.");
                    } else {
                        int from = Math.max(0, snapshot.size() - n);
                        System.out.println("[local] Últimas " + (snapshot.size() - from) + " linhas:");
                        for (int i = from; i < snapshot.size(); i++) System.out.println(snapshot.get(i));
                    }
                    continue; // não enviar ao servidor
                }

                if ("/repeat".equalsIgnoreCase(msg)) {
                    if (lastSent != null) {
                        System.out.println("[local] Resending: " + lastSent);
                        out.println(lastSent);
                        // opcional: registar também no histórico local
                        synchronized (history) {
                            history.add("You: " + lastSent + " [" + LocalTime.now().withNano(0) + "]");
                            if (history.size() > MAX_HISTORY) history.remove(0);
                        }
                    } else {
                        System.out.println("[local] Nothing to repeat.");
                    }
                    continue; // não enviar ao servidor (já reenviámos se havia)
                }
                // -------- fim comandos locais --------

                out.println(msg);
                lastSent = msg;

                // opcional: guardar o que enviaste no histórico local
                synchronized (history) {
                    history.add("You: " + msg + " [" + LocalTime.now().withNano(0) + "]");
                    if (history.size() > MAX_HISTORY) history.remove(0);
                }
            }
        } catch (Exception e) {
            System.out.println("Error sending messages: " + e.getMessage());
        }
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }

    /** Closes all resources safely */
    private void closeEverything() {
        try {
            if (scanner != null) scanner.close();
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "127.0.0.1";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 12345;
        new Client(host, port);
    }
}
