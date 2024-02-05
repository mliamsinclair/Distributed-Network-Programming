import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;

public class PrimaryServerImpl extends UnicastRemoteObject
              implements PrimaryServer {

       // list of all servers
       private ArrayList<Server> serverList;
       // seat map, 0 means available, 1 means occupied
       private String[] seatMap;

       public PrimaryServerImpl() throws RemoteException {
              super();
              serverList = new ArrayList<>();
              seatMap = new String[50];
       }

       // add a new server, when a regular server starts it calls this to add itself to
       // the list on the primary server
       public void addServer(Server s) throws RemoteException {
              serverList.add(s);
              System.out.println("Server " + (serverList.size()) + " added.");
       }

       // remove a server from the server list
       public void removeServer(Server s) throws RemoteException {
              int index = serverList.indexOf(s);
              serverList.remove(s);
              System.out.println("Server " + (index + 1) + " removed.");
       }

       // return list of all servers
       // this can also be called by the client, once the client gets the whole server
       // list it can choose one server to start a booking seat procedure
       public ArrayList<Server> getServerList() throws RemoteException {
              return serverList;
       }

       // return current seat map
       public String[] getSeatMap() throws RemoteException {
              return seatMap;
       }

       // after a server is able to successfully book a seat, update the map on the
       // primary server
       public void updateSeatMap(int seatnumber, String name) throws RemoteException {
              seatMap[seatnumber] = name;
              System.out.println("Seat map updated.");
              if (name == null)
                     System.out.println("Seat " + (seatnumber + 1) + " is now available.");
              else
                     System.out.println("Seat " + (seatnumber + 1) + " is now occupied by " + name);
       }
}
