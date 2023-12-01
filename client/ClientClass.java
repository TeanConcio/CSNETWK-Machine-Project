package client;

import java.util.Scanner;
import java.net.*;
import java.io.*;

public class ClientClass {
    public static void main(String[] args) {

        Socket clientEndpoint;

        try {
            // Send name and command to server
            System.out.print("Enter a command: ");

            Scanner scanner = new Scanner(System.in);

            String command[] = scanner.nextLine().split(" ");

            String mainCommand = command[0];
            String ipAddress = command[1];
            int nPort = Integer.parseInt(command[2]);
    
            if (mainCommand.equals("/join")) {
                clientEndpoint = new Socket(ipAddress, nPort);
                System.out.println(ipAddress + ": Connecting to server at " + clientEndpoint.getRemoteSocketAddress() + "\n");
    
                // Initialize input and output streams
                DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
                String Name;
                String Message = disReader.readUTF();

                if (Message.equals("CONNECTION SUCCESSFUL")) {
                    while (Message.equals("USER HANDLE ALREADY TAKEN") || Message.equals("CONNECTION SUCCESSFUL")) {
                        Name = registerUser (dosWriter);
                        if (Message.equals("USER HANDLE REGISTERED")) {
                            while (true) {
                                dosWriter.writeUTF(getInput(Name));
                            }
                        } else if (Message.equals("USER HANDLE ALREADY TAKEN")) {
                            System.out.println("That username is already taken! Please select another username...\n");
                        }
                    }
                }
                // Close connection
                clientEndpoint.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection terminated" + "\n");
        }
    }

    public static String registerUser (DataOutputStream dosWriter) {
        Scanner scanner = new Scanner(System.in);

        String command = scanner.nextLine();
        String Name = command.split(" ")[1];
        try {
            dosWriter.writeUTF(command);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return Name;
    }

    public static String getInput(String Name) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("[" + Name + "] Enter command: ");
        String command = scanner.nextLine();

        return command;
    }

    public static void listenForReply(String Reply) {

    }
}