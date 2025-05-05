package org.example.network_simulator.DragAndDrop;

import org.example.network_simulator.NetworkDevice;

import java.io.*;
import java.net.*;

public class PC extends NetworkDevice {
    private static int nextIpSuffix = 100;
    private static int nextPort = 6000;


    private String ipAddress;
    private String subnetMask = "255.255.255.0";

    private int port; // Unique port for this PC
    private ServerSocket serverSocket;
    private Thread serverThread;
    //protected ServerSocket serverSocket;


    public PC(double x, double y) {
        super("PC", x, y);
        this.ipAddress = "192.168.1." + nextIpSuffix++;
        this.port = nextPort++;

        startServer(); // Immediately start listening
    }

    public int getPort() {
        return port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }


    public void startServer() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            System.out.println(getName() + " server is already running on port " + port);
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println(getName() + " listening on port " + port);

            Thread serverThread = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                        String message = in.readLine();
                        System.out.println(getName() + " received: " + message);

                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        out.write("Reply from " + getName() + ": " + ipAddress);
                        out.newLine();
                        out.flush();


                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            System.err.println(getName() + " error while receiving message: " + e.getMessage());
                        }
                    }
                }
            });

            serverThread.setDaemon(true); // Won’t prevent app from closing
            serverThread.start();

        } catch (IOException e) {
            System.err.println(getName() + " failed to start server on port " + port + ": " + e.getMessage());
        }
    }




    private void handleClient(Socket clientSocket) {
        try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String received = in.readUTF();
            System.out.println(toString() + " received: " + received);

            // Respond back (optional)
            out.writeUTF("Reply from " + toString() + ": " + ipAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String ipAddress, int port, String message) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), 1000); // 1 sec timeout
            socket.setSoTimeout(1000); // Timeout for read

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.write(message);
            out.newLine();
            out.flush();

            String response = in.readLine(); // This will timeout if no response
            return response != null ? response : "No response received";

        } catch (SocketTimeoutException e) {
            return "Timeout: No response from " + ipAddress;
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }


    public void shutdown() {
        try {
            if (serverSocket != null) serverSocket.close();
            if (serverThread != null) serverThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getSubnetMask() {
        return subnetMask;
    }


    // Existing getters, setters, equals, hashCode, etc.
}
