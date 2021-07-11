package com.jake.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ServerSocketChannelDemo {

    public static void main(String[] args) {
        start(9000);
    }

    private static List<SocketChannel> sockets = Collections.synchronizedList(new ArrayList<>());

    public static void start(int port) {
        try {
            Selector selector = Selector.open();

            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(port));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);

            System.out.printf("Start listen on port: %d. %n", port);
            while (true) {
                selector.select(); // blocking here

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                while (it.hasNext()) {

                    SelectionKey selectionKey = it.next();

                    if (selectionKey.isAcceptable()) {
                        register(server.accept(), selector);
                    }

                    if (selectionKey.isReadable()) {
                        pushMessage((SocketChannel) selectionKey.channel());
                    }
                    it.remove();
                }
            }
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    private static void register(SocketChannel socket, Selector selector) {
        try {
            socket.configureBlocking(false);
            socket.register(selector, SelectionKey.OP_READ);
            sockets.add(socket);
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    private static void pushMessage(SocketChannel socket) throws IOException {

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            socket.read(buffer);
            if (new String(buffer.array()).trim().equals("/close")) {
                socket.close();
                sockets.remove(socket);
            } else {
                for (int i = 0; i < sockets.size(); i++) {
                    socket = sockets.get(i);
                    buffer.flip();
                    socket.write(buffer);
                }
                buffer.clear();
            }
        } catch (IOException e) {
            socket.close();
            sockets.remove(socket);
            System.err.println(e.toString());
        }
    }
}
