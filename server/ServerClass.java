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
	private static final String FILE_DIRECTORY = "files";

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
                            System.out.printf("[%s] CONNECTION SUCCESSFUL\n", "Unknown");

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
    
    
	public static boolean decideFunction(Socket serverEndpoint, UserClass user) {

		try {
			// Receive the function to be performed
			String input[] = user.disReader.readUTF().split(" ");

			// Decide which function to perform
			switch (input[0]) {

				case "/join":
					user.dosWriter.writeUTF("ALREADY CONNECTED");
					if (user.userHandle == null) System.out.printf("[%s] /join: ALREADY CONNECTED\n", "Unknown");
					else System.out.printf("[%s] /join: ALREADY CONNECTED\n", user.userHandle);
					return true;

				case "/register":
					if (input.length != 2) {
						user.dosWriter.writeUTF("INVALID NUMBER OF ARGUMENTS");
						if (user.userHandle == null) System.out.printf("[%s] /register: INVALID NUMBER OF ARGUMENTS\n", "Unknown");
						else System.out.printf("[%s] /register: INVALID NUMBER OF ARGUMENTS\n", user.userHandle);
						return true;
					}
					register(user, input[1]);
					return true;

				case "/leave":
					leave(user);
					return false;

				case "/dir":
					if (user.userHandle == null) {
						user.dosWriter.writeUTF("NOT REGISTERED");
						if (user.userHandle == null) System.out.printf("[%s] /dir: NOT REGISTERED\n", "Unknown");
						else System.out.printf("[%s] /dir: NOT REGISTERED\n", user.userHandle);
						return true;
					}
					dir(user);
					return true;

				case "/store":
					if (user.userHandle == null) {
						user.dosWriter.writeUTF("NOT REGISTERED");
						if (user.userHandle == null) System.out.printf("[%s] /store: NOT REGISTERED\n", "Unknown");
						else System.out.printf("[%s] /store: NOT REGISTERED\n", user.userHandle);
						return true;
					}
					if (input.length != 2) {
						user.dosWriter.writeUTF("INVALID NUMBER OF ARGUMENTS");
						System.out.printf("[%s] /store: INVALID NUMBER OF ARGUMENTS\n", user.userHandle);
						return true;
					}
					store(user, input[1]);
					return true;

				case "/get":
					if (user.userHandle == null) {
						user.dosWriter.writeUTF("NOT REGISTERED");
						System.out.printf("[%s] /get: NOT REGISTERED\n", user.userHandle);
						return true;
					}
					if (input.length != 2) {
						user.dosWriter.writeUTF("INVALID NUMBER OF ARGUMENTS");
						if (user.userHandle == null) System.out.printf("[%s] /get: INVALID NUMBER OF ARGUMENTS\n", "Unknown");
						else System.out.printf("[%s] /get: INVALID NUMBER OF ARGUMENTS\n", user.userHandle);
						return true;
					}
					get(user, input[1]);
					return true;

				default:
					user.dosWriter.writeUTF("INVALID FUNCTION");
					if (user.userHandle == null) System.out.printf("[%s] INVALID FUNCTION\n", "Unknown");
					else System.out.printf("[%s] INVALID FUNCTION\n", user.userHandle);
					return true;
			}
		}
		catch (SocketException e) {
			if (user.userHandle == null) System.out.printf("Server: Client %s disconnected\n", "Unknown");
			else System.out.printf("Server: Client %s disconnected\n", user.userHandle);
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
				System.out.printf("[%s] /register: USER IS ALREADY REGISTERED\n", user.userHandle);
			}
			else if (getUserIndex(userHandle) != -1) {
				user.dosWriter.writeUTF("USER HANDLE ALREADY TAKEN");
				System.out.printf("[%s] /register: USER HANDLE ALREADY TAKEN\n", "Unknown");
			}
			else {
				user.userHandle = userHandle;

				// Send Response
				user.dosWriter.writeUTF("USER HANDLE REGISTERED");
				System.out.printf("[%s] /register: USER HANDLE REGISTERED\n", user.userHandle);
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
			System.out.printf("[%s] /leave: USER LEFT\n", user.userHandle);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void dir(UserClass user) {

		try {
			// Get the list of filenames in the directory
			String[] filenames = (new File(FILE_DIRECTORY)).list();

			// Send the number of files
			user.dosWriter.writeInt(filenames.length);

			// Send the filenames
			for (int i = 0; i < filenames.length; i++) {
				user.dosWriter.writeUTF(filenames[i]);
			}

			// Send Response
			user.dosWriter.writeUTF("DIRECTORY SENT");
			System.out.printf("[%s] /dir: DIRECTORY SENT\n", user.userHandle);
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
			File file = new File(filename);
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			// Write the file using the byte array
			fileOutputStream.write(fileContentBytes);

			fileOutputStream.close();

			// Send Response
			user.dosWriter.writeUTF("FILE STORED");
			System.out.printf("[%s] /store: FILE STORED\n", user.userHandle);
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
				File file = new File(filename);
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
				System.out.printf("[%s] /get: FILE SENT\n", user.userHandle);
			}
			else {
				user.dosWriter.writeUTF("FILE NOT FOUND");
				System.out.printf("[%s] /get: FILE NOT FOUND\n", user.userHandle);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
