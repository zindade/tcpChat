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


    private final List<String> history = new ArrayList<>();
    private static final int MAX_HISTORY = 2000;

    public Client(String addr, int port) {
        connectToServer(addr, port);
    }


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


    private void setupStreams() throws IOException {
        scanner = new Scanner(System.in);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }


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
                System.out.println("[Disconnected from server.");
            } catch (IOException e) {
                System.out.println("Connection closed: " + e.getMessage());
            }
        }, "server-listener");

        reader.setDaemon(true);
        reader.start();
    }


    private void sendMessages() {
        try {
            while (true) {
                System.out.print("You (type '/history [n]', '/repeat' or 'exit'): ");
                String msg = scanner.nextLine();

                if ("exit".equalsIgnoreCase(msg)) {
                    System.out.println("Closing connection...");
                    break;
                }

                if (msg.startsWith("/history")) {
                    String[] t = msg.trim().split("\\s+");
                    int n = (t.length > 1) ? parseIntSafe(t[1], 20) : 20; // default 20
                    List<String> snapshot;
                    synchronized (history) {
                        snapshot = new ArrayList<>(history);
                    }
                    if (snapshot.isEmpty()) {
                        System.out.println("[local] No history.");
                    } else {
                        int from = Math.max(0, snapshot.size() - n);
                        System.out.println("[local] Last " + (snapshot.size() - from) + " lines:");
                        for (int i = from; i < snapshot.size(); i++) System.out.println(snapshot.get(i));
                    }
                    continue;
                }


                if ("/repeat".equalsIgnoreCase(msg)) {
                    if (lastSent != null) {
                        System.out.println("[local] Resending: " + lastSent);
                        out.println(lastSent);

                        synchronized (history) {
                            history.add("You: " + lastSent + " [" + LocalTime.now().withNano(0) + "]");
                            if (history.size() > MAX_HISTORY) history.remove(0);
                        }
                    } else {
                        System.out.println("[local] Nothing to repeat.");
                    }
                    continue;
                }


                out.println(msg);
                lastSent = msg;


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
