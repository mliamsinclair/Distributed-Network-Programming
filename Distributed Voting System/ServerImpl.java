import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;

public class ServerImpl extends UnicastRemoteObject
        implements Server {

    private int numClients;
    private PrimaryServer primaryServer;
    private ArrayList<Voter> voterList;

    public ServerImpl(PrimaryServer primaryServer) throws RemoteException {
        super();
        this.primaryServer = primaryServer;
        numClients = 0;
        voterList = new ArrayList<>();
        System.out.println("Server started.");
    }

    // return the current number of clients
    // client will automatically connect to the server with the least amount of
    // clients
    public int getNumClients() throws RemoteException {
        return numClients;
    }

    // increments the number of clients
    public void connect() throws RemoteException {
        System.out.println("Client connected.");
        numClients++;
    }

    // decrements the number of clients
    public void disconnect() throws RemoteException {
        System.out.println("Client disconnected.");
        numClients--;
    }

    // close the server
    public void close() throws RemoteException {
        System.out.println("Closing server...");
        try {
            // remove server from primary server list
            // and wait for 5 seconds before closing
            primaryServer.removeServer(this);
            Thread.sleep(2000);
            System.out.println("Server closed.");
        } catch (Exception e) {
            System.out.println("Server closed.");
            try {
                Thread.sleep(2000);
            } catch (Exception e2) {
                System.out.println("Error closing server." + e2);
            }
        }
        System.exit(0);
    }

    // add a voter to the voter list
    // also adds to the primary server's voter list
    public void addVoter(Voter voter) throws RemoteException {
        voterList.add(voter);
        primaryServer.addVoter(voter);
        System.out.println("Voter " + voter.getName() + " added.");
        // check if any voters need more neighbors
        for (Voter v : voterList) {
            if (v.getNeighbors().length < 5) {
                v.addNeighbor(voter);
            }
        }
    }

    // remove a voter from the voter list
    // also removes from the primary server's voter list
    public void removeVoter(Voter voter) throws RemoteException {
        voterList.remove(voter);
        primaryServer.removeVoter(voter);
        System.out.println("Voter " + voter.getName() + " removed.");
        // remove voter from all neighbors
        for (Voter v : voterList) {
            v.removeNeighbor(voter);
            v.broadcast("Voter " + voter.getName() + " removed.");
        }
    }

    // return list of all voters
    public ArrayList<Voter> getVoterList() throws RemoteException {
        return voterList;
    }

    // return a list of 5 random voters
    public ArrayList<Voter> getRandomVoters() throws RemoteException {
        ArrayList<Voter> randomVoters = new ArrayList<>();
        if (voterList.size() < 5) {
            return voterList;
        }
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
