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
            System.out.println("Ligado a " + addr + ":" + port);

            scanner = new Scanner(System.in);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                    System.out.println("[INFO] Desligado do servidor.");
                } catch (IOException e) {
                    System.out.println("[INFO] Ligação encerrada: " + e.getMessage());
                }
            }, "server-listener");
            reader.setDaemon(true);
            reader.start();


            while (true) {
                System.out.print("Tu (prefixa com 0 ou 1, 'exit' para sair): ");
                String msg = scanner.nextLine();

                if ("exit".equalsIgnoreCase(msg)) {
                    System.out.println("A encerrar ligação...");
                    break;
                }
                out.println(msg);
            }

            close();

        } catch (UnknownHostException u) {
            System.out.println("Host desconhecido: " + u.getMessage());
        } catch (IOException i) {
            System.out.println("IOException: " + i.getMessage());
        }
    }

    private void close() {
        try {
            if (scanner != null) scanner.close();
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException i) {
            System.out.println("Erro ao fechar recursos: " + i.getMessage());
        }
    }

    //private void connectToServer
    //private void setupStreams ()
    //private void sendMessages()
    //private void coseEverthing()

    public static void main(String[] args) {
        new Client("127.0.0.1", 12345);
    }
}
