# tcpChat


# Concurrent TCP Chat

This project was developed during my **Code for All_ Software Engineering Bootcamp**.  
It is a simple **multi-threaded TCP chat** built in **Java**, where several clients can connect and talk in real time through a central server.

---

## Description

The goal of this project was to practice **network programming** and **concurrency** using Java sockets and threads.  
Each client connects to the server using TCP and sends messages that are shared with all other connected users.  
The server manages all connections using a **ThreadPool** and keeps track of the active clients in memory.

---

## How to Compile and Run (Manual Method)

### 1️⃣ Start the Server

Open a terminal and run:

```bash
cd ~/Documents/mytcpchat/tcp-chat-server
mkdir -p out
javac -d out src/main/com/chat/client/*.java
java -cp out com.chat.client.ChatServer


2️⃣ Start the Client

Open another terminal and run:

cd ~/Documents/mytcpchat/tcp-chat-client
mkdir -p out
javac -d out src/main/com/chat/server/*.java
java -cp out com.chat.server.Client 127.0.0.1 12345


When you connect, the program will ask for your username.
After that, you can send messages to all connected users.

Example Commands
/list → show online users
/whisper <user> <msg> → private message
/name <newName> → change username
/quit → leave the chat
0hello → send message in uppercase
1shutdown → send shutdown signal


