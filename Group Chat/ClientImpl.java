import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JTextArea;

public class ClientImpl extends UnicastRemoteObject implements Client {
    private JTextArea messageArea;
    private String name;
    private Server server;

    public ClientImpl(String name, Server server, JTextArea messageArea) throws RemoteException {
        this.name = name;
        this.server = server;
        this.messageArea = messageArea;
    }

    // receive message from server
    // display message in message area and set caret position to end of message area
    // to ensure that the most recent message is always visible
    // as well as print message to console
    public void receive(String s) throws RemoteException {
        System.out.println(s);
        messageArea.append(s + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    // logout of server
    public void logout() throws RemoteException {
        server.logout(this);
    }
}
