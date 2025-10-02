package com.chat.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private final ProtocolHandler protocolHandler;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.protocolHandler = new ProtocolHandler();
    }

    @Override
    public void run() {
        String remote = socket.getInetAddress().getHostAddress();

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // manter referência no campo e autoFlush=true
                PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            this.out = outWriter;

            // registar cliente
            ChatServer.registerClient(out);

            ChatServer.broadcast("[SERVER] " + remote + " entrou no chat.");
            ChatServer.logThreadPoolStatus();

            String input;
            while ((input = in.readLine()) != null) {
                String response = protocolHandler.process(input);

                if ("SHUTDOWN".equals(response)) {
                    ChatServer.broadcast("[SERVER] A encerrar por pedido...");
                    ChatServer.stopServer(); // agora faz shutdown gracioso
                    return;
                } else {
                    // difundir a resposta processada
                    ChatServer.broadcast("[" + remote + "] " + response);
                }
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + remote + " (" + e.getMessage() + ")");
        } finally {
            // remover e avisar saída
            ChatServer.unregisterClient(out);
            ChatServer.broadcast("[SERVER] " + remote + " saiu do chat.");
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
