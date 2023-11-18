package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static final int PORT = 1234;
    private static final Set<InetSocketAddress> connectedClients = new HashSet<>();
    private static final byte[] buffer = new byte[1024];

    public static void main(String[] args) throws IOException {
        // Create a DatagramSocket to listen for incoming UDP packets on the specified port
        DatagramSocket socket = new DatagramSocket(PORT);
        System.out.println("Server started");

        while (true) {
            // Create a DatagramPacket to store the received data
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            // Receive the packet from a client
            socket.receive(packet);

            // Extract the client's InetSocketAddress from the received packet
            InetSocketAddress clientAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());

            // Extract the username from the received packet's data
            String username = extractUsername(packet);
            System.out.println("Client " + clientAddress + " connected with username: " + username);

            // Add the client's address to the set of connected clients
            connectedClients.add(clientAddress);

            // Broadcast a message to all clients, indicating that a new client has joined
            broadcastMessage(socket, username + " has joined the chat.", clientAddress);

            // Start a new thread to handle incoming messages from this client
            new Thread(() -> handleClientMessages(socket, clientAddress)).start();
        }
    }

    // Extracts the username from the received DatagramPacket's data
    private static String extractUsername(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }

    // Broadcasts a message to all connected clients, excluding the sender
    private static void broadcastMessage(DatagramSocket socket, String message, InetSocketAddress sender) {
        for (InetSocketAddress clientAddress : connectedClients) {
            // Exclude the sender from the broadcast
            if (!clientAddress.equals(sender)) {
                // Create a DatagramPacket to send the message to each client
                DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(),
                        clientAddress.getAddress(), clientAddress.getPort());
                
                try {
                    // Send the packet to the client
                    socket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Handles incoming messages from a specific client
    private static void handleClientMessages(DatagramSocket socket, InetSocketAddress clientAddress) {
        try {
            while (true) {
                // Create a DatagramPacket to store the incoming message
                DatagramPacket messagePacket = new DatagramPacket(buffer, buffer.length);
                
                // Receive the message from the client
                socket.receive(messagePacket);

                // Extract the message from the received packet's data
                String message = new String(messagePacket.getData(), 0, messagePacket.getLength());
                
                // Print the received message along with the client's address
                System.out.println("Received message from " + clientAddress + ": " + message);

                // Broadcast the received message to all other clients
                broadcastMessage(socket, message, clientAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}