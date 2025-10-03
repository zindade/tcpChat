package com.chat.server;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private Scanner scanner;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String addr, int port) {
        connectToServer(addr, port);
    }

    /**
     * Connects to the server and starts communication
     */
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

    /**
     * Initializes input/output streams
     */
    private void setupStreams() throws IOException {
        scanner = new Scanner(System.in);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Separate thread that keeps listening for messages from the server
     */
    private void listenForMessages() {
        Thread reader = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("[INFO] Disconnected from server.");
            } catch (IOException e) {
                System.out.println("[INFO] Connection closed: " + e.getMessage());
            }
        }, "server-listener");

        reader.setDaemon(true);
        reader.start();
    }

    /**
     * Loop to send messages to the server
     */
    private void sendMessages() {
        try {
            while (true) {
                System.out.print("You (prefix with 0 or 1, type 'exit' to quit): ");
                String msg = scanner.nextLine();

                if ("exit".equalsIgnoreCase(msg)) {
                    System.out.println("Closing connection...");
                    break;
                }
                out.println(msg);
            }
        } catch (Exception e) {
            System.out.println("Error sending messages: " + e.getMessage());
        }
    }

    /**
     * Closes all resources safely
     */
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
        new Client("127.0.0.1", 12345);
    }
}
