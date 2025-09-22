package com.chat.server;

public class ChatClient {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 12345;


        new Client(host, port);
    }
}
