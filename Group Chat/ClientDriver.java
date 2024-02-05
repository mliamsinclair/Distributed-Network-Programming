import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.RemoteException;

/*
 * java -Djava.security.policy=all.policy  ClientDriver
 */

public class ClientDriver {
    private JTextArea messageArea;
    private JTextArea sendArea;
    private JButton sendButton;
    private JButton logoutButton;
    private JButton privateButton;
    private JButton privateChatButton;
    private JButton exitPrivateChatButton;
    private Server server;
    private Client client;
    private String name;
    private String address;
    private boolean privateChat = false;
    private String privateChatName;

    // constructor that creates the GUI and runs the client program
    public ClientDriver() {
        // create the GUI
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // get the server address and name from the user
        address = JOptionPane.showInputDialog(frame, "Enter the address of the server");
        name = JOptionPane.showInputDialog(frame, "Enter your name");
        // create the GUI components
        messageArea = new JTextArea();
        sendArea = new JTextArea();
        sendButton = new JButton("Send");
        logoutButton = new JButton("Logout"); // Initialize the logoutButton
        privateButton = new JButton("Private Message"); // Initialize the privateButton
        privateChatButton = new JButton("Private Chat"); // Initialize the privateChatButton
        exitPrivateChatButton = new JButton("Exit Private Chat"); // Initialize the exitPrivateChatButton
        // button panel
        JPanel buttonPanel = new JPanel(new FlowLayout()); // Create a new JPanel for the buttons
        buttonPanel.add(sendButton); // Add the sendButton to the buttonPanel
        buttonPanel.add(logoutButton); // Add the logoutButton to the buttonPanel
        buttonPanel.add(privateButton); // Add the privateButton to the buttonPanel
        // private chat button panel
        JPanel privateChatPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Create a new JPanel for the private
                                                                                 // chat buttons
        privateChatPanel.add(privateChatButton); // Add the privateChatButton to the privateChatPanel

        // connect to the server
        try {
            server = (Server) Naming.lookup("rmi://" + address + "/server");
            client = new ClientImpl(name, server, messageArea);
            server.login(name, client);
            messageArea.append("Connected to server.\n");
        } catch (Exception ex) {
            messageArea.append("Error connecting to server. Please try again.\n");
        }

        // logout button
        // logs the user out of the server and exits the program
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    server.send(name + " has logged out."); // Send a message to the server that the client has
                                                            // logged out
                    server.logout(client);
                    System.exit(0); // Exit the program
                } catch (RemoteException ex) {
                    messageArea.append("Error logging out.\n");
                }
            }
        });

        // send button
        // sends a message to all users in the global chat
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String message = sendArea.getText();
                    if (privateChat) {
                        if (!server.checkPrivate(privateChatName).equals(name)) {
                            messageArea.append(privateChatName + " is not currently in the private chat.\n");
                        } else {
                            server.sendPrivate(name + ": " + message, client, privateChatName);
                            messageArea.append(name + ": " + message + "\n");
                        }
                    } else {
                        server.send(name + ": " + message);
                    }
                    sendArea.setText(""); // Clear the send area
                } catch (RemoteException ex) {
                    messageArea.append("Error sending message.\n");
                }
            }
        });

        // private message button
        // sends a private message to another user
        privateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String tempName = JOptionPane.showInputDialog(frame,
                            "Enter the name of the person you want to send a private message to");
                    String message = JOptionPane.showInputDialog(frame,
                            "Enter the message you want to send to " + tempName);
                    server.sendPrivate("(Private) " + name + ": " + message, client, tempName);
                    messageArea.append("(Private) " + name + ": " + message + "\n");
                    messageArea.setCaretPosition(messageArea.getDocument().getLength());
                    sendArea.setText(""); // Clear the send area
                } catch (RemoteException ex) {
                    messageArea.append("Error sending message.\n");
                }
            }
        });

        // private chat button
        // creates a private chat with another user
        privateChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    privateChat = true;
                    privateChatName = JOptionPane.showInputDialog(frame,
                            "Enter the name of the person you want to enter a private chat with");
                    if (server.userExists(privateChatName) && (server.checkPrivate(privateChatName).equals("none")
                            || server.checkPrivate(privateChatName).equals(name))) {
                        messageArea.setText("You are now in a private chat with " + privateChatName + ".\n");
                        server.setPrivate(client, true, privateChatName);
                        if (server.checkPrivate(privateChatName).equals("none")) {
                            server.sendPrivate(name
                                    + " has entered a private chat with you!\nPress the 'Private Chat' button and enter their\nname to enter the private chat!",
                                    client,
                                    privateChatName);
                        } else {
                            messageArea.append(privateChatName + " is also in the private chat!\n");
                        }
                        privateChatPanel.remove(privateChatButton);
                        privateChatPanel.add(exitPrivateChatButton);
                        privateChatPanel.revalidate();
                        privateChatPanel.repaint();
                        panel.revalidate();
                        panel.repaint();
                    } else {
                        if (!server.userExists(privateChatName)) {
                            messageArea.append("User does not exist.\n");
                        } else {
                            messageArea.append("User is already in a private chat.\n");
                        }
                        privateChat = false;
                    }
                } catch (RemoteException ex) {
                    messageArea.append("Error sending message.\n");
                }
            }
        });

        // exit private chat button
        // exits the private chat
        exitPrivateChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    privateChat = false;
                    if (!server.checkPrivate(privateChatName).equals("none")) {
                        server.sendPrivate(name + " has left the private chat.", client, privateChatName);
                    }
                    messageArea.append("You are now in the global chat.\n");
                    server.setPrivate(client, false, "none");
                    privateChatPanel.remove(exitPrivateChatButton);
                    privateChatPanel.add(privateChatButton);
                    privateChatPanel.revalidate();
                    privateChatPanel.repaint();
                    panel.revalidate();
                    panel.repaint();
                } catch (RemoteException ex) {
                    messageArea.append("Error sending message.\n");
                }
            }
        });

        // add the components to the panel
        messageArea.setEditable(false); // Make the messageArea not editable
        messageArea.setPreferredSize(new Dimension(400, 300)); // Set the preferred size of the messageArea
        sendArea.setPreferredSize(new Dimension(400, 50)); // Set the preferred size of the sendArea
        panel.add(new JScrollPane(messageArea)); // Wrap the messageArea in a JScrollPane
        panel.add(new JScrollPane(sendArea)); // Wrap the sendArea in a JScrollPane
        panel.add(buttonPanel); // Add the buttonPanel to the panel
        panel.add(privateChatPanel); // Add the privateChatButton to the panel
        // add the panel to the frame and display the frame
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    // main method that runs the client program
    public static void main(String[] args) {
        new ClientDriver();
    }
}