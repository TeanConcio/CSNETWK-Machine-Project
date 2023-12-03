package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientLogic {

    private Socket clientEndpoint;
    private DataOutputStream dosWriter;
    private DataInputStream disReader;
    private String userName;
    private ClientGUI clientGUI;
    private ClientClass clientClass;

    public ClientLogic(ClientGUI clientGUI, ClientClass clientClass) {
        this.clientGUI = clientGUI;
        this.clientClass = clientClass;
    }

    public ClientGUI getClientGUI() {
        return clientGUI;
    }

    public void init(String initCommand) {
        if (clientClass.isJoined) {
            clientClass.joinServer(initCommand);
        }
        return;
    }

    public void processCommand(String command) {
        try {
            dosWriter.writeUTF(command);
            String[] commandParts = command.split(" ");
            String mainCommand = commandParts[0];

            switch (mainCommand) {
                case "/get":
                    String fileName = commandParts[1];
                    downloadFile(fileName);
                    break;
                case "/store":
                    fileName = commandParts[1];
                    sendFile(fileName);
                    break;
                case "/dir":
                    String directories = disReader.readUTF();
                    //clientGUI.displayMessage(directories);
                    break;
                // Add other cases as needed
                default:
                    String message = disReader.readUTF();
                    verifyReply(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String registerUser() {
        try {
            System.out.print("Enter a command: ");
            String command = clientClass.registerUser(" ");
            String Name = "NULL";

            if (command.split(" ").length > 1) {
                Name = command.split(" ")[1];
            }

            dosWriter.writeUTF(command);

            return Name;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void downloadFile(String filename) {
        try {
            // Similar implementation as before
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String filename) {
        try {
            // Similar implementation as before
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verifyReply(String reply) {
        // Similar implementation as before
        // ...
    }
}
