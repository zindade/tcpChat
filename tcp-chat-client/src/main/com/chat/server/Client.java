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
        try {
            socket = new Socket(addr, port);
            System.out.println("Connected to " + addr + ":" + port);

            scanner = new Scanner(System.in);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String msg;

            while (true) {
                System.out.print("You (0=message, 1=command, exit=quit): ");
                msg = scanner.nextLine();

                if (msg.equalsIgnoreCase("exit")) {
                    System.out.println("Closing connection...");
                    break;
                }

                out.println(msg);

                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server closed the connection.");
                    break;
                }
                System.out.println("Server: " + response);
            }

            close();

        } catch (UnknownHostException u) {
            System.out.println("Unknown host: " + u.getMessage());
        } catch (IOException i) {
            System.out.println("IOException: " + i.getMessage());
        }
    }

    private void close() {
        try {
            if (scanner != null) scanner.close();
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException i) {
            System.out.println("Error closing resources: " + i.getMessage());
        }
    }
}
