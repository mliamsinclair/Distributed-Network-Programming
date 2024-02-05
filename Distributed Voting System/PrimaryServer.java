import java.rmi.*;
import java.util.ArrayList;

public interface PrimaryServer extends Remote {
       // add a new server, when a regular server starts it calls this to add itself to
       // the list on the primary server
       void addServer(Server s) throws RemoteException;

       // remove a server from the server list
       void removeServer(Server s) throws RemoteException;

       // return list of all servers
       // this can also be called by the client, once the client gets the whole server
       // list it can choose one server to start a booking seat procedure
       ArrayList<Server> getServerList() throws RemoteException;

       // add a new voter to the voter list
       void addVoter(Voter v) throws RemoteException;

       // remove a voter from the voter list
       void removeVoter(Voter v) throws RemoteException;

       // return list of all voters
       ArrayList<Voter> getVoterList() throws RemoteException;

       // return a list of 5 random voters
       ArrayList<Voter> getRandomVoters() throws RemoteException;
}
