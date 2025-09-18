package com.chat.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 12345;

        try {
            Socket socket = new Socket(hostname, port);
            System.out.println("" + hostname + ":" + port);


            new Thread(new ServerListener(socket)).start();


            Scanner scanner = new Scanner(System.in);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


            while (true) {
                String msg = scanner.nextLine();
                out.println(msg);

                if (msg.equalsIgnoreCase("/quit")) {
                    System.out.println("");
                    socket.close();
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println(" " + e.getMessage());
        }
    }


    private static class ServerListener implements Runnable {
        private Socket socket;

        public ServerListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println(msg);
                }
            } catch (IOException e) {
                System.err.println("⚠️ : " + e.getMessage());
            }
        }
    }
}
