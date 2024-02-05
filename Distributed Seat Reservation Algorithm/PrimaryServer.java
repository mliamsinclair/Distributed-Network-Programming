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

       // return current seat map
       String[] getSeatMap() throws RemoteException;

       // after a server is able to successfully book a seat, update the map on the
       // primary server
       void updateSeatMap(int seatnumber, String name) throws RemoteException;
}
