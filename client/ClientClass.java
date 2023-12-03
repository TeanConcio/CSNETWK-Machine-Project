package client;

import server.UserClass;

import java.net.*;
import java.io.*;

public class ClientClass {
    
    private String FILE_DIRECTORY = System.getProperty("user.dir") + "\\client\\files\\";
    private DataOutputStream dosWriter;
    private DataInputStream disReader;
    private Socket clientEndpoint;
    public boolean isJoined;
    public boolean isRegistered;
    public String command;
    public String Name;

    public ClientClass() {
        this.FILE_DIRECTORY = System.getProperty("user.dir") + "\\client\\files\\";
        this.isJoined = false;
        this.isRegistered = false;
        this.command = "";
        this.Name = "";
    }

    public void initializeClient(String initCommand) {

        try {

            String Name;
            String fullCommand = "";
            String Command;
            String Message = disReader.readUTF();

            boolean Looper = true;
            
            if (Message.equals("CONNECTION SUCCESSFUL")) {
                System.out.println("Connection successful!");

                Name = registerUser (fullCommand);
                Message = disReader.readUTF();

                while (Message.equals("USER HANDLE ALREADY TAKEN") || 
                        Message.equals("NOT REGISTERED") || 
                        Message.equals("INVALID FUNCTION") || 
                        Message.equals("ALREADY CONNECTED") ||
                        Message.equals("DISPLAY COMMANDS")) {

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
                    } else if (Message.equals("DISPLAY COMMANDS")) {
                        getHelp ("Before Register", "");
                    }
                        
                    Name = registerUser (fullCommand);
                    Message = disReader.readUTF();
                }

                while (Looper) {
                    fullCommand = getInput(Name);
                    dosWriter.writeUTF(fullCommand);
                    Command = fullCommand.split(" ")[0];

                    if (Command.equals("/get")) {
                        String fileName = fullCommand.split(" ")[1];
                        downloadFile(fileName, Name);
                    }
                    else if (Command.equals("/store")) {
                        String fileName = fullCommand.split(" ")[1];
                        sendFile (fileName, Name);
                    }
                    else if (Command.equals("/dir")) {
                        String Directories = disReader.readUTF();
                        System.out.println(Directories);
                    }
                    else {
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
        }
    }

    public void checkJoin (String initCommand) {
        try {
            String command[] = initCommand.split(" ");

            String mainCommand = command[0];

            if (mainCommand.equals("/join")) {

                joinServer(initCommand);

                String Message = disReader.readUTF();

                if (Message.equals("CONNECTION SUCCESSFUL")) {
                    System.out.println("Connection successful!");
                    isJoined = true;
                    return;
                }
            }

            isJoined = false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void joinServer (String initCommand) {
        try {
            System.out.print("Enter a command: ");
            String command[] = initCommand.split(" ");

            String mainCommand = command[0];
            String ipAddress = "NULL";
            int nPort = 0;

            if (mainCommand.equals("/join")) {
                ipAddress = command[1];
                nPort = Integer.parseInt(command[2]);
            }

            if (!mainCommand.equals("/join")) {
                if (mainCommand.equals("/?")) {
                    getHelp("Before Join", "");
                }
                else {
                    System.out.println("\nInvalid function. Please use '/?' to see the list of available functions.\n");
                }

                System.out.print("Enter a command: ");
                //command = scanner.nextLine().split(" ");

                mainCommand = command[0];

                if (mainCommand.equals("/join")) {
                    ipAddress = command[1];
                    nPort = Integer.parseInt(command[2]);
                }
            }
            else if (mainCommand.equals("/join")) {

                clientEndpoint = new Socket(ipAddress, nPort);
                System.out.println(ipAddress + ": Connecting to server at " + clientEndpoint.getRemoteSocketAddress() + "\n");
    
                // Initialize input and output streams
                dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                disReader = new DataInputStream(clientEndpoint.getInputStream());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getHelp (String whatDoYouNeedHelpWith, String Name) {
        System.out.println("\n" + Name + "List of available commands: ");
        switch (whatDoYouNeedHelpWith) {
            case "Before Join":
                System.out.println("/join: Connects to the server [Usage: /join {IP Address} {Port Number}]");
                break;
            case "Before Register":
                System.out.println("/register: Registers a unique alias [Usage: /register {Username}]");
                break;
            case "After Register":
                System.out.println("/leave: Disconnects from the server [usage: /leave]");
                System.out.println("/store: Stores a file in the server [usage: /store {File Name}]");
                System.out.println("/dir: Requests a directory file list from the server [usage: /dir]");
                System.out.println("/get: Downloads a file from the server [usage: /get {File Name}]");
                break;
        }
        System.out.println("");
    }

    public void checkRegister (String regCommand) {
        try {
            String command[] = regCommand.split(" ");

            String mainCommand = command[0];

            if (mainCommand.equals("/register")) {

                Name = registerUser(regCommand);

                String Message = disReader.readUTF();

                if (Message.equals("USER HANDLE REGISTERED")) {
                    System.out.println("User handle [" + Name + "] registered successfully!");
                    isRegistered = true;
                    return;
                }
            }

            isRegistered = false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String registerUser (String command) {

        System.out.print("Enter a command: ");

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

    public String getInput(String Name) {
        System.out.print("[" + Name + "] Enter command: ");
        //String command = scanner.nextLine();

        return command;
    }

    public boolean verifyReply(String Reply, String Name) {
        switch (Reply) {
            case "ALREADY CONNECTED":
                System.out.println("\n[" + Name + "] User is already connected.\n");
                break;
            case "INVALID FUNCTION":
                System.out.println("\n[" + Name + "] Invalid function. Please use '/?' to see the list of available functions.\n");
                break;
            case "USER IS ALREADY REGISTERED":
                System.out.println("\n[" + Name + "] User is already registered.\n");
                break;
            case "USER LEFT":
                return false;
            case "FILE STORED":
                System.out.println("\n[" + Name + "] File stored successfully!\n");
                break;
            case "FILE SENT":
                System.out.println("\n[" + Name + "] File sent successfully!\n");
                break;
            case "FILE NOT FOUND":
                System.out.println("\n[" + Name + "] File not found.\n");
                break;
            case "DIRECTORY SENT":
                break;
            case "DISPLAY COMMANDS":
                getHelp("After Register", "[" + Name + "]");
            default:
        }

        return true;
    }

    public void downloadFile(String filename, String Name) {

		try {
			// Receive the file size
            if (!disReader.readUTF().equals("FILE NOT FOUND")) {
                int fileSize = disReader.readInt();

                // Receive the file contents
                byte[] fileContentBytes = new byte[fileSize];
                disReader.readFully(fileContentBytes, 0, fileSize);

                // Initialize File and FileOutputStream
                File file = new File(FILE_DIRECTORY + filename);
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                // Write the file using the byte array
                fileOutputStream.write(fileContentBytes);

                fileOutputStream.close();
                System.out.println(disReader.readUTF());
                System.out.println("\n[" + Name + "] File successfully downloaded at " + FILE_DIRECTORY + filename + "\n");
            }
			else {
                System.out.println("\n[" + Name + "] File not found.\n");
            }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void sendFile (String filename, String Name) {

		try {
			// Get the list of filenames in the directory
            String[] filenames = (new File(FILE_DIRECTORY)).list();
            
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
                dosWriter.writeUTF("FILE FOUND");
                
				// Initialize File and FileInputStream
				File file = new File(FILE_DIRECTORY + filename);
				FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());

				// Get File contents into byte array
				byte[] fileContentBytes = new byte[(int) file.length()];
                fileInputStream.read(fileContentBytes);
				
				// Send the size of the byte array, then the actual byte array
				dosWriter.writeInt(fileContentBytes.length);
				dosWriter.write(fileContentBytes);

				fileInputStream.close();

				// Send Response
				System.out.printf("\n[" + Name + "] File successfully stored in the server.\n");
			}
			else {
                dosWriter.writeUTF("FILE NOT FOUND");
				System.out.printf("\n[" + Name + "] File not found.\n");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}