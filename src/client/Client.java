package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    private static final String IP = "localhost";
    private static final int PORT = 1234;
    private static byte[] buffer = new byte[1024];

    public static void main(String[] args) {
        try {
            // Read the userName
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your name: ");
            String userName = scanner.nextLine();

            // Create a socket
            DatagramSocket socket = new DatagramSocket();

            // Send the username to the server
            InetAddress serverAddress = InetAddress.getByName(IP);
            DatagramPacket userNamePacket = new DatagramPacket(userName.getBytes(), userName.length(), serverAddress, PORT);
            socket.send(userNamePacket);
            System.out.println("Message sent to the server: " + userName);

            // Start a new thread to handle incoming messages
            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                        socket.receive(responsePacket);
                        String receivedMessage = new String(responsePacket.getData(), 0, responsePacket.getLength());
                        System.out.println(receivedMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            // Allow the user to send messages
            while (true) {
                String message = scanner.nextLine();
                DatagramPacket messagePacket = new DatagramPacket(message.getBytes(), message.length(), serverAddress, PORT);
                socket.send(messagePacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}