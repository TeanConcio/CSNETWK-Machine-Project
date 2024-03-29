/* 
 * CSNETWK S12
 * 
 * Chan, Dane
 * Concio, Tean
 * Sia, Dominic
*/

package server;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.io.*;

import java.util.Date;  

public class ServerClass {

    private static final int SERVER_PORT = 12345;
	private static final String FILE_DIRECTORY = System.getProperty("user.dir") + "\\server\\files\\";

    private static ArrayList<UserClass> userList = new ArrayList<UserClass>();


    public static void main(String[] args) {

        ServerSocket serverSocket = null;

        try {
            // Initialize ServerSocket
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server: Listening on port " + SERVER_PORT + "...");

            while (true) {

                Socket serverEndpoint = null;
				Socket serverMessageEndpoint = null;

                try {
                    // Initialize ServerSocket
                    serverEndpoint = serverSocket.accept();
					serverMessageEndpoint = serverSocket.accept();
                    System.out.println("Server: New client connected: " + serverEndpoint.getRemoteSocketAddress());
					System.out.println("Server: New client messenger connected: " + serverMessageEndpoint.getRemoteSocketAddress());

                    // Create final variable for thread
                    final Socket finalServerEndpoint = serverEndpoint;
					final Socket finalServerMessageEndpoint = serverMessageEndpoint;

                    Thread userThread = new Thread(() -> {
                        try {
                            // Initialize user
                            UserClass user = new UserClass(
                                new DataInputStream(finalServerEndpoint.getInputStream()), 
                                new DataOutputStream(finalServerEndpoint.getOutputStream()),
								new DataOutputStream(finalServerMessageEndpoint.getOutputStream()));

                            // Add user to list
                            userList.add(user);

                            // Reply to client
                            user.dosWriter.writeUTF("CONNECTION SUCCESSFUL");
                            logUserAction(user, "CONNECTION SUCCESSFUL");

                            // Receive the function to be performed
                            while (decideFunction(finalServerEndpoint, user)){}
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }); 

                    // Start thread
                    userThread.start();
                }
                catch (Exception e) {
                    serverEndpoint.close();
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Server: Connection is terminated.");
		}
    }


	public static void logUserAction(UserClass user, String message) {

		if (user.userHandle == null) System.out.printf("[%s] %s\n", "Unknown", message);
		else System.out.printf("[%s] %s\n", user.userHandle, message);
	}


	public static boolean checkUserNotRegistered(UserClass user) {

		try {
			// Check if user handle is already taken
			if (user.userHandle == null) {
				user.dosWriter.writeUTF("NOT REGISTERED");
				logUserAction(user, "/dir: NOT REGISTERED");
				return true;
			}
			else
				return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
    
    
	public static boolean decideFunction(Socket serverEndpoint, UserClass user) {

		try {
			// Receive the function to be performed
			String input = user.disReader.readUTF().trim();
			logUserAction(user, input);
			
			// Split the input into command and parameter
			String command = input.split(" ")[0];
			String parameters = input.split(command, 2)[1].trim();

			// Decide which function to perform
			switch (command) {

				case "/join":
					user.dosWriter.writeUTF("ALREADY CONNECTED");
					logUserAction(user, "/join: ALREADY CONNECTED");
					return true;

				case "/register":
					register(user, parameters);
					return true;

				case "/leave":
					leave(user);
					return false;

				case "/dir":
					if (checkUserNotRegistered(user)) {
						return true;
					}
					dir(user);
					return true;

				case "/store":
					if (checkUserNotRegistered(user)) {
						return true;
					}
					store(user, parameters);
					return true;

				case "/get":
					if (checkUserNotRegistered(user)) {
						return true;
					}
					get(user, parameters);
					return true;
				
				case "/message":
					if (checkUserNotRegistered(user)) {
						return true;
					}
					message(user, parameters);
					return true;
				
				case "/broadcast":
					if (checkUserNotRegistered(user)) {
						return true;
					}
					broadcast(user, parameters);
					return true;
				
				case "/?":
					user.dosWriter.writeUTF("DISPLAY COMMANDS");
					logUserAction(user, "/?: DISPLAY COMMANDS");
					return true;

				default:
					user.dosWriter.writeUTF("INVALID FUNCTION");
					logUserAction(user, command + ": INVALID FUNCTION");
					return true;
			}
		}
		catch (SocketException e) {
			logUserAction(user, "CONNECTION TERMINATED");
			userList.remove(user);
			try {
				serverEndpoint.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}


	public static int getUserIndex(String userHandle) {

		for (int i = 0; i < userList.size(); i++) {
			if (userList.get(i).userHandle != null)
				if (userList.get(i).userHandle.equals(userHandle))
					return i;
		}

		return -1;
	}


	public static void register(UserClass user, String userHandle) {

		try {
			// Check if user handle is already taken
			if (user.userHandle != null) {
				user.dosWriter.writeUTF("USER IS ALREADY REGISTERED");
				logUserAction(user, "/register: USER IS ALREADY REGISTERED");
			}
			else if (getUserIndex(userHandle) != -1) {
				user.dosWriter.writeUTF("USER HANDLE ALREADY TAKEN");
				logUserAction(user, "/register: USER HANDLE ALREADY TAKEN");
			}
			else {
				user.userHandle = userHandle;

				// Send Response
				user.dosWriter.writeUTF("USER HANDLE REGISTERED");
				logUserAction(user, "/register: USER HANDLE REGISTERED");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void leave(UserClass user) {

		try {
			// Remove user from list
			userList.remove(user);

			// Send Response
			user.dosWriter.writeUTF("USER LEFT");
			logUserAction(user, "/leave: USER LEFT");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void dir(UserClass user) {

		try {
			// Get the list of filenames in the directory
			String[] filenamesList = (new File(FILE_DIRECTORY)).list();

			if (filenamesList.length == 0) {
				user.dosWriter.writeUTF("");

				user.dosWriter.writeUTF("DIRECTORY EMPTY");
				logUserAction(user, "/dir: DIRECTORY EMPTY");
			}
			else {
				// Merge the filenames into a single string
				String filenames = "";
				for (int i = 0; i < filenamesList.length - 1; i++) {
					filenames += "\t" + filenamesList[i] + "\n";
				}
				filenames += filenamesList[filenamesList.length - 1];

				// Send the filenames
				user.dosWriter.writeUTF(filenames);

				user.dosWriter.writeUTF("DIRECTORY SENT");
			logUserAction(user, "/dir: " + filenames);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void store(UserClass user, String filename) {

		try {
			// Ask if file exists in client
			if (user.disReader.readUTF().equals("FILE NOT FOUND")) {
				user.dosWriter.writeUTF("FILE NOT FOUND");
				logUserAction(user, "/store: FILE NOT FOUND");
				return;
			}

			// Receive the file size
			int fileSize = user.disReader.readInt();

			// Receive the file contents
			byte[] fileContentBytes = new byte[fileSize];
			user.disReader.readFully(fileContentBytes, 0, fileSize);

			// Initialize File and FileOutputStream
			File file = new File(FILE_DIRECTORY + filename);
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			// Write the file using the byte array
			fileOutputStream.write(fileContentBytes);

			fileOutputStream.close();

			// Send Response
			user.dosWriter.writeUTF("FILE STORED");
			logUserAction(user, "/store: FILE STORED");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void get(UserClass user, String filename) {

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

				user.dosWriter.writeUTF("FILE FOUND");
				logUserAction(user, "/get: FILE FOUND");

				// Initialize File and FileInputStream
				File file = new File(FILE_DIRECTORY + filename);
				FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());

				// Get File contents into byte array
				byte[] fileContentBytes = new byte[(int) file.length()];
				fileInputStream.read(fileContentBytes);

				// Send the size of the byte array, then the actual byte array
				user.dosWriter.writeInt(fileContentBytes.length);
				user.dosWriter.write(fileContentBytes);

				fileInputStream.close();

				// Send Response
				user.dosWriter.writeUTF("FILE SENT");
				logUserAction(user, "/get: FILE SENT");
			}
			else {
				user.dosWriter.writeUTF("FILE NOT FOUND");
				logUserAction(user, "/get: FILE NOT FOUND");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void message(UserClass user, String parameters) {

		try {
			// Split the input into user handle and message
			String userHandle = parameters.split(" ")[0];
			String message = parameters.split(userHandle, 2)[1].trim();

			// Find the index of the user
			int userIndex = getUserIndex(userHandle);

			// Check if user exists
			if (userIndex == -1) {
				user.dosWriter.writeUTF("USER NOT FOUND");
				logUserAction(user, "/message: USER NOT FOUND");
				return;
			}

			SimpleDateFormat Sent = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
			Date date = new Date();  
			String dateTimeSent = "[" + Sent.format(date) + "] ";
			// Send message to user
			userList.get(userIndex).dosWriterMessage.writeUTF(dateTimeSent + user.userHandle + ": " + message);
			user.dosWriterMessage.writeUTF(dateTimeSent + "To " + userList.get(userIndex).userHandle + ": " + message);

			// Send Response
			user.dosWriter.writeUTF("MESSAGE SENT");
			logUserAction(user, "/message: MESSAGE SENT");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void broadcast(UserClass user, String message) {

		try {
			SimpleDateFormat Sent = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
			Date date = new Date();  
			String dateTimeSent = "[" + Sent.format(date) + "] ";
			// Send message to all users
			for (UserClass userClass : userList) {
				if (userClass.userHandle != null) {
					userClass.dosWriterMessage.writeUTF(dateTimeSent + "BROADCAST [" + user.userHandle + "]: " + message);
				}
			}

			// Send Response
			user.dosWriter.writeUTF("MESSAGE BROADCASTED");
			logUserAction(user, "/broadcast: MESSAGE BROADCASTED");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}