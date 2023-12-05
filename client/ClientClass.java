package client;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;

public class ClientClass {
    
    private String FILE_DIRECTORY;
    private DataOutputStream dosWriter;
    private DataInputStream disReader;
    private Socket clientEndpoint;
    public boolean isJoined;
    public boolean isRegistered;
    public String command;
    public String stringAppend;
    public String Name;

    public ClientClass() {
        this.FILE_DIRECTORY = System.getProperty("user.dir") + "\\client\\";
        this.isJoined = false;
        this.isRegistered = false;
        this.command = "";
        this.Name = "";
        this.stringAppend = "";
    }

    public void checkJoin (String initCommand) {
        try {
            String command[] = initCommand.split(" ");

            String mainCommand = command[0];

            if (mainCommand.equals("/join") && command.length == 3) {

                joinServer(initCommand);

                if (clientEndpoint != null) {
                    String Message = disReader.readUTF();

                    if (Message.equals("CONNECTION SUCCESSFUL")) {
                        System.out.println("Connection successful!");
                        isJoined = true;
                        verifyReply(Message, "");
                        return;
                    }

                    verifyReply(Message, "");
                }
                else
                    stringAppend = "Unable to connect to specified server. Please recheck your server credentials and try again.\n";
            }
            else if (mainCommand.equals("/?")) {
                getHelp(""); 
            }
            else {
                stringAppend = "Invalid command or command parameters. Please use '/?' to view available commands.\n";
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

            //String mainCommand = command[0];
            String ipAddress = command[1];
            int nPort = Integer.parseInt(command[2]);

            // Prevent joining non-local servers

            if (!ipAddress.equals("localhost") && !ipAddress.equals("127.0.0.1"))
                throw new Exception();

            clientEndpoint = new Socket(ipAddress, nPort);
            System.out.println(ipAddress + ": Connecting to server at " + clientEndpoint.getRemoteSocketAddress() + "\n");
    
            // Initialize input and output streams
            dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
            disReader = new DataInputStream(clientEndpoint.getInputStream());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getHelp (String Name) {
        stringAppend = Name + "List of available commands: \n";

        if (!isJoined) {
            stringAppend = stringAppend + "/join: Connects to the server [Usage: /join {IP Address} {Port Number}]\n";
        }
        else if (isJoined && !isRegistered) {
            stringAppend = stringAppend + "/register: Registers a unique alias [Usage: /register {Username}]\n";
        }
        else if (isJoined && isRegistered) {
            stringAppend = stringAppend + "/leave: Disconnects from the server [usage: /leave]\n";
            stringAppend = stringAppend + "/store: Stores a file in the server [usage: /store {File Name}]\n";
            stringAppend = stringAppend + "/dir: Requests a directory file list from the server [usage: /dir]\n";
            stringAppend = stringAppend + "/get: Downloads a file from the server [usage: /get {File Name}]\n";
        }
        System.out.println("");
    }

    public void checkRegister (String regCommand) {
        try {
            //String command[] = regCommand.split(" ");

            //String mainCommand = command[0];

            Name = registerUser(regCommand);
            String Message = "NULL";
            if (!Name.equals("NULL")) {
                Message = disReader.readUTF();
            }

            if (Message.equals("USER HANDLE REGISTERED")) {
                System.out.println("User handle [" + Name + "] registered successfully!");
                isRegistered = true;
                FILE_DIRECTORY = FILE_DIRECTORY + Name + "\\";
                verifyReply(Message, Name);
                return;
            }
            
            if (stringAppend.equals("Username [NULL] is a reserved username, please choose another username.")) {
                verifyReply(Message, "");
            }
            
            isRegistered = false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String registerUser (String command) {

        String Name = "NULL";

        if (command.split(" ").length > 1) {
            Name = command.split(" ")[1];
            System.out.println(Name.equals("NULL"));
            if (Name.equals("NULL")) {
                stringAppend = "Username [NULL] is a reserved username, please choose another username.\n";
            }
        }

        try {
            if (!Name.equals("NULL")) {
                System.out.println("Hello");
                dosWriter.writeUTF(command);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return Name;
    }


    public void routeCommands (String fullCommand) {
        try {

            // Check if server is still connected
            if (clientEndpoint.isClosed()) {
                stringAppend = "Current server is no longer connected. Please join another server.\n";
                this.isJoined = false;
                this.isRegistered = false;
                return;
            }

            dosWriter.writeUTF(fullCommand);
            String Command = fullCommand.split(" ")[0];
            String Message;
            System.out.println(Command);

            if (Command.equals("/get")) {
                String fileName = fullCommand.split(" ")[1];
                downloadFile(fileName, Name);
            }
            else if (Command.equals("/store")) {
                String fileName = fullCommand.split(" ")[1];
                sendFile (fileName, Name);
                Message = disReader.readUTF();
                System.out.println(Message);
            }
            else if (Command.equals("/dir")) {
                String Directories = disReader.readUTF();
                stringAppend = "[" + Name + "] List of available files for download:\n" + Directories + "\n";
                Message = disReader.readUTF();
                verifyReply(Message, Name);
            }
            else {
                Message = disReader.readUTF();
                System.out.println(Message);
                verifyReply(Message, Name);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void leaveServer () {
        try {
            clientEndpoint.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean verifyReply(String Reply, String Name) {
        if (Name != "") {
            Name = "[" + Name + "]";
        }
        System.out.println(Reply);
        switch (Reply) {
            case "CONNECTION SUCCESSFUL":
                stringAppend = "Connection successful!\n";
                break;
            case "USER HANDLE REGISTERED":
                stringAppend = "User handle " + Name + " registered successfully!\n";
                break;
            case "USER HANDLE ALREADY TAKEN":
                stringAppend = "That username is already taken. Please select another username...\n";
                break;
            case "NOT REGISTERED":
                stringAppend = "This command is only available to registered users. Please register first.\n";
                break;
            case "ALREADY CONNECTED":
                stringAppend = Name + " User is already connected.\n";
                break;
            case "INVALID FUNCTION":
                stringAppend =  Name + " Invalid command. Please use '/?' to view available commands.\n";
                break;
            case "USER IS ALREADY REGISTERED":
                stringAppend = Name + " User is already registered.\n";
                break;
            case "USER LEFT":
                stringAppend = Name + " Connection terminated.\n";
                leaveServer();
                return false;
            case "FILE STORED":
                stringAppend = Name + " File successfully downloaded at ";
                break;
            case "FILE SENT":
                stringAppend = Name + " File successfully stored in the server.\n";
                break;
            case "FILE NOT FOUND":
                stringAppend = Name + " File not found.\n";
                break;
            case "DIRECTORY SENT":
                break;
            case "DISPLAY COMMANDS":
                getHelp(Name);
                break;
            default:
                break;
        }

        return true;
    }

    public void downloadFile(String filename, String Name) {

		try {
            Path path = Paths.get(FILE_DIRECTORY);
            Files.createDirectories(path);

            System.out.println("This is a test");
            String Message = disReader.readUTF();
            System.out.println(Message);
			// Receive the file size
            if (!Message.equals("FILE NOT FOUND")) {
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
                verifyReply("FILE STORED", Name);
                stringAppend = stringAppend + FILE_DIRECTORY + filename + "\n";
            }
			else {
                verifyReply("FILE NOT FOUND", Name);
                System.out.println("\n[" + Name + "] File not found.\n");
            }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void sendFile (String filename, String Name) {

		try {
            Path path = Paths.get(FILE_DIRECTORY);
            Files.createDirectories(path);

			// Get the list of filenames in the directory
            String[] filenames = (new File(FILE_DIRECTORY)).list();
            
			// Find the index of the file
			int fileIndex = -1;
            if (filenames != null) {
                for (int i = 0; i < filenames.length; i++) {
                    if (filenames[i].equals(filename)) {
                        fileIndex = i;
                        break;
                    }
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
                verifyReply("FILE SENT", Name);
				System.out.printf("\n[" + Name + "] File successfully stored in the server.\n");
			}
			else {
                dosWriter.writeUTF("FILE NOT FOUND");
                verifyReply("FILE NOT FOUND", Name);
				System.out.printf("\n[" + Name + "] File not found.\n");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}