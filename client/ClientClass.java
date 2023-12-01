package client;

import java.util.Scanner;
import java.net.*;
import java.io.*;

public class ClientClass {
    public static void main(String[] args) {

        Socket clientEndpoint;

        try {
            // Send name and command to server
            //TODO : Please use '/?' to see the list of available functions.
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
                System.out.println("HELLO");

                if (disReader.readUTF().equals("CONNECTION SUCCESSFUL")) {
                    System.out.println("Connection successful!");
                    Name = registerUser (dosWriter);
                    while (disReader.readUTF().equals("USER HANDLE ALREADY TAKEN") || disReader.readUTF().equals("NOT REGISTERED")) {
                        
                        System.out.println("HI");

                        Name = registerUser (dosWriter);

                        if (disReader.readUTF().equals("USER HANDLE REGISTERED")) {
                            System.out.println("Registered successfully!");
                            break;
                        } else if (disReader.readUTF().equals("USER HANDLE ALREADY TAKEN")) {
                            System.out.println("That username is already taken! Please select another username...");
                        } else if (disReader.readUTF().equals("NOT REGISTERED")) {
                            System.out.println("This function is only available to registered users. Please register first.");
                        }
                    }

                    while (true) {
                        dosWriter.writeUTF(getInput(Name));
                        verifyReply(disReader.readUTF());
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

        System.out.print("Enter a command: ");
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

    public static void verifyReply(String Reply) {
        switch (Reply) {
            case "ALREADY CONNECTED":
                System.out.println("User is already connected. ");
                break;
            case "INVALID FUNCTION":
                System.out.println("Invalid function. Please use '/?' to see the list of available functions.");
                break;
            case "USER IS ALREADY REGISTERED":
                System.out.println("User is already registered.");
                break;
            case "USER LEFT":
                System.out.println("Connection terminated.");
                return;
            case "FILE STORED":
                System.out.println("File stored successfully!");
                break;
            case "FILE SENT":
                System.out.println("File received at " + "TODO : Directory");
                break;
            case "FILE NOT FOUND":
                System.out.println("File not found.");
                break;
            default:
        }
    }
}