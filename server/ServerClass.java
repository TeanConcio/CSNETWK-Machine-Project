/* 
 * CSNETWK S12
 * 
 * Chan, Dane
 * Concio, Tean
 * Sia, Dominic
*/

package server;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class ServerClass {

    private static final int SERVER_PORT = 4000;
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

                try {
                    // Initialize ServerSocket
                    serverEndpoint = serverSocket.accept();
                    System.out.println("Server: New client connected: " + serverEndpoint.getRemoteSocketAddress());

                    // Create final variable for thread
                    final Socket finalServerEndpoint = serverEndpoint;

                    Thread userThread = new Thread(() -> {
                        try {
                            // Initialize user
                            UserClass user = new UserClass(
                                new DataInputStream(finalServerEndpoint.getInputStream()), 
                                new DataOutputStream(finalServerEndpoint.getOutputStream()));

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
				return false;
			}
			else
				return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
    
    
	public static boolean decideFunction(Socket serverEndpoint, UserClass user) {

		try {
			// Receive the function to be performed
			String input[] = user.disReader.readUTF().split(" ");

			// Decide which function to perform
			switch (input[0]) {

				case "/join":
					user.dosWriter.writeUTF("ALREADY CONNECTED");
					logUserAction(user, "/join: ALREADY CONNECTED");
					return true;

				case "/register":
					register(user, input[1]);
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
					store(user, input[1]);
					return true;

				case "/get":
					if (checkUserNotRegistered(user)) {
						return true;
					}
					get(user, input[1]);
					return true;

				default:
					user.dosWriter.writeUTF("INVALID FUNCTION");
					logUserAction(user, input[0] + ": INVALID FUNCTION");
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
			
			// Merge the filenames into a single string
			String filenames = "";
			for (int i = 0; i < filenamesList.length; i++) {
				filenames += filenamesList[i] + "\n";
			}

			// Send the filenames
			user.dosWriter.writeUTF(filenames);

			logUserAction(user, "/dir: " + filenames);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void store(UserClass user, String filename) {

		try {
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
}
