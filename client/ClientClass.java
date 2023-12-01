package client;

import java.util.Scanner;
import java.net.*;
import java.io.*;

public class ClientClass {
    public static void main(String[] args) {

        String sServerAddress = args[0];
        int nPort = Integer.parseInt(args[1]);
        String clientName = args[2];

        try {
            // Connect to server
            Socket clientEndpoint = new Socket(sServerAddress, nPort);
            System.out.println(clientName + ": Connecting to server at\n" + clientEndpoint.getRemoteSocketAddress() + "\n");

            // Initialize input and output streams
            DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
            DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());

            // Send name to server
            dosWriter.writeUTF(clientName);

            // Start a loop to handle user input
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print(clientName + ": Enter command (or 'exit' to quit): ");
                String command = scanner.nextLine();

                // Send command to server
                dosWriter.writeUTF(command);

                if (command.equals("/get")) {
                    System.out.print("Enter file path: ");
                    String filePath = scanner.nextLine();

                    // Send file path to server
                    dosWriter.writeUTF(filePath);

                    // Receive and save the file from the server
                    receiveFile(disReader, filePath);
                } else {
                    // Receive and print the server's response for other commands
                    System.out.println(disReader.readUTF());
                }
                // Exit loop if the user enters 'exit'
                if (command.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            // Close connection
            clientEndpoint.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(clientName + ": Connection terminated" + "\n");
        }
    }

    private static void receiveFile(DataInputStream disReader, String filePath) throws IOException {
        try {
            // Receive file name and size from the server
            String receivedFileName = disReader.readUTF();
            long fileSize = disReader.readLong();

            System.out.println("Client: Receiving file from server: " + receivedFileName + " (" + fileSize + " bytes)");

            // Receive file content
            byte[] buffer = new byte[4096];
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                int bytesRead;
                while ((bytesRead = disReader.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("Client: File received successfully");
        } catch (EOFException e) {
            // Handle end of stream gracefully
            System.out.println("Client: Server closed the connection unexpectedly.");
        }
    }
}
