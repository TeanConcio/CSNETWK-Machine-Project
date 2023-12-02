package client;

import java.util.Scanner;

import server.UserClass;

import java.net.*;
import java.io.*;

public class ClientClass {
    
    private static final String FILE_DIRECTORY = System.getProperty("user.dir") + "\\client\\files\\";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        Socket clientEndpoint;

        try {
            // Send name and command to server
            //TODO : Please use '/?' to see the list of available functions.
            System.out.print("Enter a command: ");

            String command[] = scanner.nextLine().split(" ");

            String mainCommand = command[0];
            String ipAddress = command[1];
            int nPort = Integer.parseInt(command[2]);

            //TODO: Error Checking for other than /join and /?
            if (mainCommand.equals("/join")) {

                clientEndpoint = new Socket(ipAddress, nPort);
                System.out.println(ipAddress + ": Connecting to server at " + clientEndpoint.getRemoteSocketAddress() + "\n");
    
                // Initialize input and output streams
                DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());

                String Name;
                String fullCommand;
                String Command;
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
                        fullCommand = getInput(Name);
                        dosWriter.writeUTF(fullCommand);
                        System.out.println(fullCommand);
                        Command = fullCommand.split(" ")[0];

                        if (Command.equals("/get")) {

                        }
                        else if (Command.equals("/store")) {
                            String fileName = fullCommand.split(" ")[1];
                            sendFile (disReader, dosWriter, fileName);
                        }
                        else if (Command.equals("/dir")) {
                            String Directories = disReader.readUTF();
                            System.out.println(Directories);
                        }

                        Message = disReader.readUTF();
                        Looper = verifyReply(Message, Name);
                    }
                }

                // Close connection
                clientEndpoint.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection terminated" + "\n");
            scanner.close();
        }
    }

    public static String registerUser (DataOutputStream dosWriter) {

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
        System.out.print("[" + Name + "] Enter command: ");
        String command = scanner.nextLine();

        return command;
    }

    public static boolean verifyReply(String Reply, String Name) {
        switch (Reply) {
            case "ALREADY CONNECTED":
                System.out.println("[" + Name + "] User is already connected.");
                break;
            case "INVALID FUNCTION":
                System.out.println("[" + Name + "] Invalid function. Please use '/?' to see the list of available functions.");
                break;
            case "USER IS ALREADY REGISTERED":
                System.out.println("[" + Name + "] User is already registered.");
                break;
            case "USER LEFT":
                return false;
            case "FILE STORED":
                System.out.println("[" + Name + "] File stored successfully!");
                break;
            case "FILE SENT":
                System.out.println("[" + Name + "] File received at " + "TODO : Directory");
                break;
            case "FILE NOT FOUND":
                System.out.println("[" + Name + "] File not found.");
                break;
            case "DIRECTORY SENT":
                break;
            default:
        }

        return true;
    }

    public static void get(DataInputStream disReader, DataOutputStream dosWriter, String filename) {

		try {
			// Receive the file size
			int fileSize = disReader.readInt();

			// Receive the file contents
			byte[] fileContentBytes = new byte[fileSize];
			disReader.readFully(fileContentBytes, 0, fileSize);

			// Initialize File and FileOutputStream
			File file = new File(filename);
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			// Write the file using the byte array
			fileOutputStream.write(fileContentBytes);

			fileOutputStream.close();

			// Send Response
			System.out.printf("FILE STORED\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void sendFile (DataInputStream disReader, DataOutputStream dosWriter, String filename) {

		try {
			// Get the list of filenames in the directory
            String[] filenames = (new File(FILE_DIRECTORY)).list();
            System.out.println(filenames[0]);
            
			// Find the index of the file
			int fileIndex = -1;
			for (int i = 0; i < filenames.length; i++) {
				if (filenames[i].equals(filename)) {
					fileIndex = i;
					break;
				}
			}

			// Check if file exists
			if (fileIndex != -1) {

				// Initialize File and FileInputStream
				File file = new File(FILE_DIRECTORY + filename);
				FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());

				// Get File contents into byte array
				byte[] fileContentBytes = new byte[(int) file.length()];
                fileInputStream.read(fileContentBytes);
                
                for (int i = 0; i < file.length(); i++) {
                    System.out.print(fileContentBytes[i]);
                }
                System.out.println("");
				

				// Send the size of the byte array, then the actual byte array
				dosWriter.writeInt(fileContentBytes.length);
				dosWriter.write(fileContentBytes);

				fileInputStream.close();

				// Send Response
				System.out.printf("/store: FILE SENT\n");
			}
			else {
				System.out.printf("/store: FILE NOT FOUND\n");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}