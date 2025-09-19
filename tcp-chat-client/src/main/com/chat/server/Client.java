package com.chat.server;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private Socket socket = null;
    private Scanner scanner = null;
    private DataOutputStream out = null;

    public Client(String addr, int port) {
        try {
            socket = new Socket(addr, port);
            System.out.println("Connected");

            scanner = new Scanner(System.in);
            out = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException u) {
            System.out.println(u);
            return;
        } catch (IOException i) {
            System.out.println(i);
            return;
        }

        String m = "";

        while (!m.equals("Over")) {
            try {
                m = scanner.nextLine();
                out.writeUTF(m);
            } catch (IOException i) {
                System.out.println(i);
            }
        }
        try {
            scanner.close();
            out.close();
            socket.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }
}
