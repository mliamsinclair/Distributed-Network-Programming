import java.rmi.*;

public interface Client extends Remote {
    public void receive(String s) throws RemoteException;

}
