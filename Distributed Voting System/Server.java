import java.rmi.*;
import java.util.ArrayList;

public interface Server extends Remote {
    // return the current number of clients
    // client will automatically connect to the server with the least amount of
    // clients
    int getNumClients() throws RemoteException;

    // increments the number of clients
    void connect() throws RemoteException;

    // decrements the number of clients
    void disconnect() throws RemoteException;

    // close the server
    void close() throws RemoteException;

    // add a voter to the voter list
    // also adds to the primary server's voter list
    void addVoter(Voter voter) throws RemoteException;

    // remove a voter from the voter list
    // also removes from the primary server's voter list
    void removeVoter(Voter voter) throws RemoteException;

    // return list of all voters
    ArrayList<Voter> getVoterList() throws RemoteException;

    // return a list of 5 random voters
    ArrayList<Voter> getRandomVoters() throws RemoteException;
}
