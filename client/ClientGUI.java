/* 
 * CSNETWK S12
 * 
 * Chan, Dane
 * Concio, Tean
 * Sia, Dominic
*/

package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI {

    private ClientClass clientClass;
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton submitButton;
    private String previousMessage;

    public ClientGUI(ClientClass clientClass) {
        this.clientClass = clientClass;
        initializeGUI();
    }

    private void initializeGUI() {
        JFrame frame = new JFrame("Chat Client");
    
        messageArea = new JTextArea(10, 40);
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
    
        inputField = new JTextField(40);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleInitialCommand();
            }
        });
        
        submitButton = new JButton("Submit Command"); // Assign to the class-level submitButton
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleInitialCommand();
            }
        });
    
        JPanel panel = new JPanel();
        panel.add(scrollPane);
        panel.add(inputField);
        panel.add(submitButton);
    
        frame.add(panel);
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void handleInitialCommand() {
        String initCommand = inputField.getText();
        if (!initCommand.isEmpty()) {
            // Optionally, you can display the result in the message area
            System.out.println(clientClass.isJoined);
    
            // Disable input during the operation
            inputField.setEnabled(false);
            submitButton.setEnabled(false);
    
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        System.out.println(clientClass.isJoined + " " + clientClass.isRegistered);
                        if (!clientClass.isJoined) {
                            clientClass.checkJoin(initCommand);
                        }
                        else if (!clientClass.isRegistered) {
                            System.out.println(initCommand);
                            clientClass.checkRegister(initCommand);
                        }
                        else {
                            System.out.println("route commands1");
                            System.out.println(initCommand);
                            clientClass.routeCommands(initCommand);
                            System.out.println("route commands2");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            
                    return null;
                }
            
                @Override
                protected void done() {
                    try {
                        // Re-enable input after the operation is complete
                        inputField.setEnabled(true);
                        submitButton.setEnabled(true);
                        
                        messageArea.append(clientClass.stringAppend);
                        clientClass.stringAppend = "";

                        inputField.setText("");
                        inputField.requestFocus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            
            worker.execute();
            previousMessage = "";
            // New thread to listen for new data
            new Thread(() -> {
                try {
                    while (true) {
                        // Check if there is new data from the server
                        if (clientClass.newMessage) {
                            // Update the GUI using SwingUtilities.invokeLater()
                            SwingUtilities.invokeLater(() -> {
                                clientClass.newMessage = false;
                                if (!clientClass.receivedMessage.equals(previousMessage)) {
                                    messageArea.append(clientClass.receivedMessage + "\n");
                                }
                                previousMessage = clientClass.receivedMessage;
                                System.out.println(previousMessage);
                            });
                        }
                        // Sleep for a while to avoid busy waiting
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientClass clientClass = new ClientClass();
            new ClientGUI(clientClass);
        });
    }
}
