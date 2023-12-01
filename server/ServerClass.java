/* 
 * CSNETWK S12
 * 
 * Chan, Dane
 * Concio, Tean
 * Sia, Dominic
*/

package server;

import java.net.*;
import java.io.*;

import server.UserClass;
import server.FileClass;

public class ServerClass
{
	private static final int SERVER_PORT = 4000;
	private static final String SERVER_ADDRESS = "127.0.0.1";

	private static UserClass userList[] = new UserClass[10];
	private static FileClass fileList[] = new FileClass[10];

	public static void main(String[] args)
	{
		System.out.println("Server: Listening on port " + SERVER_PORT + "...");

		// Loop to accept multiple clients
		while (true) {
			try 
			{
				// Initialize ServerSocket and Socket and accept connection
				ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
				Socket serverEndpoint = serverSocket.accept();
				System.out.println("Server: New client connected: " + serverEndpoint.getRemoteSocketAddress());
				
				// 

				// Reply to client
				dosWriter.writeUTF("CONNECTION SUCCESSFUL");

				// Receive the function to be performed

				serverEndpoint.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				System.out.println("Server: Connection is terminated.");
			}
		}
	}


	public static void decideFunction(DataInputStream disReader, DataOutputStream dosWriter) {

		try {

			// Receive the function to be performed
			String function = disReader.readUTF();

			// Decide which function to perform
			switch (function) {
				case "register":

					break;
				case "dir":

					break;
				case "store":

					break;
				case "get":

					break;
				case "leave":

					break;
				default:
					dosWriter.writeUTF("INVALID FUNCTION");
					break;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static boolean checkUserHandle(String userHandle) {

		for (int i = 0; i < userHandleList.length; i++) {
			if (userHandleList[i] == userHandle) {
				return true;
			}
		}

		return false;
	}


	public static void register(DataInputStream disReader, DataOutputStream dosWriter) {

		try {

			// Receive the user handle
			String userHandle = disReader.readUTF();

			// Check if user handle is already taken
			if (checkUserHandle(userHandle)) {
				dosWriter.writeUTF("USER HANDLE ALREADY TAKEN");
			}
			else {
				// Add user handle to list
				for (int i = 0; i < userHandleList.length; i++) {
					if (userHandleList[i] == null) {
						userHandleList[i] = userHandle;
						break;
					}
				}

				dosWriter.writeUTF("USER HANDLE REGISTERED");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			dosWriter.writeUTF("ERROR: " + e.getMessage());
		}
	}


	public static void dir(DataInputStream disReader, DataOutputStream dosWriter) {

		try {

			// Send the number of files
			dosWriter.writeInt(fileList.length);

			// Send the file names
			for (int i = 0; i < fileList.length; i++) {
				dosWriter.writeUTF(fileList[i].filename);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			dosWriter.writeUTF("ERROR: " + e.getMessage());
		}
	}


	public static void store(DataInputStream disReader, DataOutputStream dosWriter) {

		try {

			// Receive the file name
			String filename = disReader.readUTF();

			// Receive the file size
			int fileSize = disReader.readInt();

			// Receive the file contents
			byte[] fileContentBytes = new byte[fileSize];
			disReader.readFully(fileContentBytes, 0, fileSize);

			// Add file to list
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i] == null) {
					fileList[i] = new FileClass(filename, fileContentBytes, fileSize, null);
					break;
				}
			}

			dosWriter.writeUTF("FILE STORED");
		}
		catch (Exception e) {
			e.printStackTrace();
			dosWriter.writeUTF("ERROR: " + e.getMessage());
		}
	}



	public static void sendFile(DataInputStream disReader, DataOutputStream dosWriter) {

		try {

			// Declare File and FileInputStream
			String filename = "Download.txt";
			File file = new File(filename);
			FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());

			// Sending File Prompt
			String sendingFilePrompt = String.format("Server: Sending file \"%s\" (%d bytes)", filename, (int)file.length());
			System.out.println(sendingFilePrompt);

			// Get File contents into byte array
			byte[] fileContentBytes = new byte[(int) file.length()];
			fileInputStream.read(fileContentBytes);

			// Send the size of the byte array, then the actual byte array
			dosWriter.writeInt(fileContentBytes.length);
			dosWriter.write(fileContentBytes);

			fileInputStream.close();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}