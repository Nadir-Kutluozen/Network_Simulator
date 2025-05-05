package org.example.network_simulator.DragAndDrop;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends PC {
    public Server(double x, double y) {
        super(x, y);
        setName("Server" + getId()); // Override name
    }

    @Override
    public void startServer() {
        if (getServerSocket() != null && !getServerSocket().isClosed()) return;

        try {
            ServerSocket socket = new ServerSocket(getPort());
            System.out.println(getName() + " (Server) listening on port " + getPort());

            Thread serverThread = new Thread(() -> {
                while (!socket.isClosed()) {
                    try (Socket clientSocket = socket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                        String msg = in.readLine();
                        System.out.println(getName() + " received: " + msg);

                        // Response
                        out.write("HTTP/1.1 200 OK");
                        out.newLine();
                        out.write("Content-Length: 0");
                        out.newLine();
                        out.newLine();
                        out.flush();

                    } catch (IOException e) {
                        if (!socket.isClosed())
                            System.err.println(getName() + " error: " + e.getMessage());
                    }
                }
            });

            serverThread.setDaemon(true);
            serverThread.start();

        } catch (IOException e) {
            System.err.println(getName() + " failed to start server: " + e.getMessage());
        }
    }
}
