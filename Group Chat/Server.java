import java.rmi.*;

public interface Server extends Remote {
    public boolean login(String name, Client c) throws RemoteException;

    public void logout(Client c) throws RemoteException;

    public void send(String s) throws RemoteException;

    public void sendPrivate(String s, Client c, String name) throws RemoteException;

    public boolean userExists(String name) throws RemoteException;

    public void setPrivate(Client c, boolean b, String name) throws RemoteException;

    public String checkPrivate(String name) throws RemoteException;
}
