
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;

public class ServerImpl extends UnicastRemoteObject implements Server {

    // list of clients
    private ArrayList<Object[]> clientList;

    // constructor
    public ServerImpl() throws RemoteException {
        clientList = new ArrayList<>();
    }

    // save reference to client in client list with name, reference, private chat
    // status, and private chat name
    public boolean login(String name, Client c) throws RemoteException {
        System.out.println(name + " is logging in...");
        Object[] client = new Object[4];
        client[0] = name;
        client[1] = c;
        client[2] = false;
        client[3] = "none";
        clientList.add(client);
        return true;
    }

    // remove reference to client
    public void logout(Client c) throws RemoteException {
        System.out.println("Logging out...");
        for (int i = 0; i < clientList.size(); i++) {
            Object[] client = clientList.get(i);
            if (client[1].equals(c)) {
                clientList.remove(i);
                break;
            }
        }
    }

    // send message to all clients using their reference
    // in the client list
    public void send(String s) throws RemoteException {
        System.out.println("Sending message...");
        for (int i = 0; i < clientList.size(); i++) {
            Object[] client = clientList.get(i);
            if (!(boolean) client[2]) {
                ((Client) client[1]).receive(s);
            }
        }
    }

    // send private message to another user
    public void sendPrivate(String s, Client c, String name) throws RemoteException {
        System.out.println("Sending private message...");
        boolean found = false;
        for (int i = 0; i < clientList.size(); i++) {
            Object[] client = clientList.get(i);
            if (client[0].equals(name)) {
                ((Client) client[1]).receive(s);
                found = true;
            }
        }
        if (!found) {
            c.receive("User not found.");
        }
    }

    // check if a signed in user exists
    public boolean userExists(String name) throws RemoteException {
        System.out.println("Checking if user exists...");
        for (int i = 0; i < clientList.size(); i++) {
            Object[] client = clientList.get(i);
            if (client[0].equals(name)) {
                return true;
            }
        }
        return false;
    }

    // set private chat with another user
    public void setPrivate(Client c, boolean b, String name) throws RemoteException {
        System.out.println("Setting private chat...");
        for (int i = 0; i < clientList.size(); i++) {
            Object[] client = clientList.get(i);
            if (client[1].equals(c)) {
                client[2] = b;
                client[3] = name;
                break;
            }
        }
    }

    // check if private chat is set
    // returns name of person in private chat
    // or 'none' if private chat is not set
    public String checkPrivate(String name) throws RemoteException {
        System.out.println("Checking if private chat is set...");
        for (int i = 0; i < clientList.size(); i++) {
            Object[] client = clientList.get(i);
            if (client[0].equals(name)) {
                return (String) client[3];
            }
        }
        return "none";
    }
}
