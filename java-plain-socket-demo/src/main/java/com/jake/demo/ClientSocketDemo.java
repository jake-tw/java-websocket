package com.jake.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class ClientSocketDemo {

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 9000;

        try {
            InetAddress address = InetAddress.getByName(host);
            Socket socket = new Socket(address, port);

            new Thread(new ReceiveHanlder(socket)).start();
            new Thread(new SendHanlder(socket)).start();

        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    public static class SendHanlder implements Runnable {
        private final Socket socket;

        public SendHanlder(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String message;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    PrintStream printer = new PrintStream(socket.getOutputStream());) {

                while (!socket.isClosed()) {
                    message = reader.readLine();
                    printer.println(message);
                }
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }
    }

    public static class ReceiveHanlder implements Runnable {
        private final Socket socket;

        public ReceiveHanlder(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {

                String message = null;
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
                socket.close();
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }
    }
}
