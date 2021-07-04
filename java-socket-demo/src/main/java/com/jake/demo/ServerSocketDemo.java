package com.jake.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerSocketDemo {

    public static void main(String[] args) {
        start(9000);
    }

    private static List<Socket> sockets = Collections.synchronizedList(new ArrayList<>());

    public static void start(final int port) {

        Socket socket;
        try (ServerSocket server = new ServerSocket(port)) {

            System.out.printf("Start listen on port: %d. %n", port);
            while (true) {
                socket = server.accept(); // blocking here

                new Thread(new ConnectionHanlder(socket)).start();
            }
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    public static void pushMessage(String message) throws IOException {
        for (int i = 0; i < sockets.size(); i++) {
            Socket socket = sockets.get(i);
            PrintStream printer = new PrintStream(socket.getOutputStream());
            printer.printf(message);
        }
    }

    public static class ConnectionHanlder implements Runnable {

        private final Socket socket;
        private String name;

        ConnectionHanlder(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream printer = new PrintStream(socket.getOutputStream());) {

                printer.printf("Hello, what's your name? %n", socket.getInetAddress().toString());
                name = reader.readLine();
                System.out.printf("%s connected. %n", name);
                sockets.add(socket);

                pushMessage(String.format("%s connected. %n", name));
                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.equals("/close")) {
                        System.out.printf("%s disconnected. %n", name);
                        socket.close();
                        break;
                    }
                    pushMessage(String.format("%s: %s %n", name, message));
                }
            } catch (IOException e) {
                System.err.println(e.toString());
            } finally {
                sockets.remove(socket);
            }
        }
    }
}
