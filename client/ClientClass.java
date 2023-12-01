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
                String Message = disReader.readUTF();
                boolean Looper = true;
                System.out.println("HELLO");

                if (Message.equals("CONNECTION SUCCESSFUL")) {
                    System.out.println("Connection successful!");

                    Name = registerUser (dosWriter);
                    Message = disReader.readUTF();

                    while (Message.equals("USER HANDLE ALREADY TAKEN") || 
                            Message.equals("NOT REGISTERED") || 
                            Message.equals("INVALID FUNCTION") || 
                            Message.equals("ALREADY CONNECTED")) {

                        if (Message.equals("USER HANDLE REGISTERED")) {
                            System.out.println("Registered successfully!");
                            break;
                        } else if (Message.equals("USER HANDLE ALREADY TAKEN")) {
                            System.out.println("That username is already taken! Please select another username...");
                        } else if (Message.equals("NOT REGISTERED")) {
                            System.out.println("This function is only available to registered users. Please register first.");
                        } else if (Message.equals("INVALID FUNCTION")) {
                            System.out.println("Invalid function. Please use '/?' to see the list of available functions.");
                        } else if (Message.equals("ALREADY CONNECTED")) {
                            System.out.println("User is already connected.");
                        }
                        
                        Name = registerUser (dosWriter);
                        Message = disReader.readUTF();
                    }

                    while (Looper) {
                        dosWriter.writeUTF(getInput(Name));
                        Message = disReader.readUTF();
                        Looper = verifyReply(Message);
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
        String Name = "NULL";

        if (command.split(" ").length > 1) {
            Name = command.split(" ")[1];
        }

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

    public static boolean verifyReply(String Reply) {
        switch (Reply) {
            case "ALREADY CONNECTED":
                System.out.println("User is already connected.");
                break;
            case "INVALID FUNCTION":
                System.out.println("Invalid function. Please use '/?' to see the list of available functions.");
                break;
            case "USER IS ALREADY REGISTERED":
                System.out.println("User is already registered.");
                break;
            case "USER LEFT":
                return false;
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

        return true;
    }
}