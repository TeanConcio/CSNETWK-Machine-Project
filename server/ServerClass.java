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

public class ServerClass
{
	private static final int SERVER_PORT = 4000;

	private static ArrayList<UserClass> userList = new ArrayList<UserClass>();
	private static ArrayList<FileClass> fileList = new ArrayList<FileClass>();

	public static void main(String[] args)
	{

		ServerSocket serverSocket = null;

		try {
			// Initialize ServerSocket
			serverSocket = new ServerSocket(SERVER_PORT);
			System.out.println("Server: Listening on port " + SERVER_PORT + "...");

			// Loop to accept multiple clients
			while (true) {
				// Initialize Socket and accept connection
				Socket serverEndpoint = serverSocket.accept();
				System.out.println("Server: New client connected: " + serverEndpoint.getRemoteSocketAddress());
				
				// Initialize user
				UserClass user = new UserClass(
					new DataInputStream(serverEndpoint.getInputStream()), 
					new DataOutputStream(serverEndpoint.getOutputStream()));

				// Add user to list
				userList.add(user);

				// Reply to client
				user.dosWriter.writeUTF("CONNECTION SUCCESSFUL");
				System.out.printf("[%s] CONNECTION SUCCESSFUL\n", "Unknown");

				// Receive the function to be performed
				while (decideFunction(serverEndpoint, user)){}

				serverEndpoint.close();
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

			user.dosWriter.writeUTF("USER LEFT");
			System.out.printf("[%s] /leave: USER LEFT\n", user.userHandle);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void dir(UserClass user) {

		try {
			// Send the number of files
			user.dosWriter.writeInt(fileList.size());

			// Send the file names
			for (int i = 0; i < fileList.size(); i++) {
				user.dosWriter.writeUTF(fileList.get(i).filename);
			}

			System.out.printf("[%s] /dir: %d FILENAMES SENT\n", user.userHandle, fileList.size());
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

			// Add file to list
			fileList.add(new FileClass(filename, fileContentBytes, fileSize, user.userHandle));

			user.dosWriter.writeUTF("FILE STORED");
			System.out.printf("[%s] /store: FILE STORED\n", user.userHandle);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void get(UserClass user, String filename) {

		try {
			// Find the file index
			int fileIndex = -1;
			for (int i = 0; i < fileList.size(); i++) {
				if (fileList.get(i).filename.equals(filename)) {
					fileIndex = i;
					break;
				}
			}

			// Check if file exists
			if (fileIndex != -1) {

				// Send the file size
				user.dosWriter.writeInt(fileList.get(fileIndex).length);

				// Send the file contents
				user.dosWriter.write(fileList.get(fileIndex).data);

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