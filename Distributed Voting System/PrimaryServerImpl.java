import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;

public class PrimaryServerImpl extends UnicastRemoteObject
              implements PrimaryServer {

       // list of all servers
       private ArrayList<Server> serverList;
       // list of all voters
       private ArrayList<Voter> voterList;

       public PrimaryServerImpl() throws RemoteException {
              super();
              serverList = new ArrayList<>();
              voterList = new ArrayList<>();
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

       // add a new voter to the voter list
       public void addVoter(Voter v) throws RemoteException {
              voterList.add(v);
              System.out.println("Voter " + (voterList.size()) + " added.");
              // add voter to all servers that don't already have it
              for (Server s : serverList) {
                     if (!s.getVoterList().contains(v)) {
                           s.addVoter(v);
                     }
              }
       }

       // remove a voter from the voter list
       public void removeVoter(Voter v) throws RemoteException {
              int index = voterList.indexOf(v);
              voterList.remove(v);
              System.out.println("Voter " + (index + 1) + " removed.");
              // remove voter from all servers
              for (Server s : serverList) {
                     if (s.getVoterList().contains(v)) {
                           s.removeVoter(v);
                     }
              }
       }

       // return list of all voters
       public ArrayList<Voter> getVoterList() throws RemoteException {
              return voterList;
       }

       // return a list of 5 random voters
       public ArrayList<Voter> getRandomVoters() throws RemoteException {
              ArrayList<Voter> randomVoters = new ArrayList<>();
              for (int i = 0; i < 5; i++) {
                     int index = (int) (Math.random() * voterList.size());
                     if (randomVoters.contains(voterList.get(index))) {
                           i--;
                           continue;
                     }
                     randomVoters.add(voterList.get(index));
              }
              return randomVoters;
       }

}
