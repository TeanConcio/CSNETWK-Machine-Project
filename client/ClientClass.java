package client;

import java.util.Scanner;
import java.net.*;
import java.io.*;

public class ClientClass {
    public static void main(String[] args) {

        String commandJoin = args[0];
        String ipAddress = args[1];
        int nPort = Integer.parseInt(args[2]);

        Socket clientEndpoint;

        try {
            // Send name and command to server
    
            if (commandJoin.equals("/join")) {
                if (args.length == 5) {
                    // Fetch file from server
                    clientEndpoint = new Socket(ipAddress, nPort);
                    System.out.println(ipAddress + ": Connecting to server at\n" + clientEndpoint.getRemoteSocketAddress() + "\n");
    
                    // Initialize input and output streams
                    DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                    DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());

                    // Close connection
                    clientEndpoint.close();
                } else {
                    System.out.println("Usage: /get [file_path]");
                }// Connect to server
    
                // Receive message from server
                // System.out.println(disReader.readUTF() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection terminated" + "\n");
        }
    }
}
