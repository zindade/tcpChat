package com.chat.server;

import java.time.LocalDateTime;

public class ProtocolHandler {

    public String process(String input) {
        if (input.startsWith("0")) {
            return input.substring(1).toUpperCase() + " [" + LocalDateTime.now() + "]";
        } else if (input.startsWith("1")) {
            if (input.substring(1).equalsIgnoreCase("SHUTDOWN")) {
                return "SHUTDOWN"; // sinal especial para o servidor
            } else {
                return "Unknown command [" + LocalDateTime.now() + "]";
            }
        } else {
            return "Unknown protocol type [" + LocalDateTime.now() + "]";
        }
    }
}