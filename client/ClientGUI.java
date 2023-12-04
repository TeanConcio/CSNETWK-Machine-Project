package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI {

    private ClientClass clientClass;
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton submitButton;

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
                    System.out.println(clientClass.isJoined + " " + clientClass.isRegistered);
                    if (!clientClass.isJoined) {
                        clientClass.checkJoin(initCommand);
                    }
                    else if (!clientClass.isRegistered) {
                        System.out.println(initCommand);
                        clientClass.checkRegister(initCommand);
                    }
                    else {
                        System.out.println(initCommand);
                        clientClass.routeCommands(initCommand);
                    }
                    return null;
                }
    
                @Override
                protected void done() {
                    // Re-enable input after the operation is complete
                    inputField.setEnabled(true);
                    submitButton.setEnabled(true);
                    
                    messageArea.append(clientClass.stringAppend);

                    inputField.setText("");
                    inputField.requestFocus();
                }
            };
    
            worker.execute();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientClass clientClass = new ClientClass();
            new ClientGUI(clientClass);
        });
    }
}
