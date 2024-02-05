import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;

public class ServerImpl extends UnicastRemoteObject
        implements Server {

    private String[] seatMap;
    private int[] lockMap;
    private int numClients;
    private PrimaryServer primaryServer;

    public ServerImpl(PrimaryServer primaryServer) throws RemoteException {
        super();
        this.primaryServer = primaryServer;
        seatMap = primaryServer.getSeatMap();
        lockMap = new int[50];
        numClients = 0;
        System.out.println("Server started.");
    }

    // return current seat map
    public String[] seatMap() throws RemoteException {
        return seatMap;
    }

    // return printable seat map
    public String seatMapPrint() throws RemoteException {
        String map = "\n====Seats====\n";
        for (int i = 0; i < seatMap.length; i++) {
            if (seatMap[i] == null)
                map += "Seat " + (i + 1) + ": Available\n";
            else
                map += "Seat " + (i + 1) + ": " + seatMap[i] + "\n";
        }
        return map;
    }

    // lock seat on the current server, it is called by regular servers
    // return true only when the seat is available and not already locked
    public synchronized boolean lockSeat(int seatnumber) throws RemoteException {
        System.out.println("Locking seat " + (seatnumber + 1));
        if (seatMap[seatnumber] == null && lockMap[seatnumber] == 0) {
            lockMap[seatnumber] = 1;
            System.out.println("Seat " + (seatnumber + 1) + " locked.");
            return true;
        }
        System.out.println("Seat " + (seatnumber + 1) + " is not available.");
        return false;
    }

    // try to book a seat, a server can only be able to process a booking
    // successfully
    // after it sends lockseat requests to all servers and be able to lock the seat
    // successfully on all servers
    // if any server returns false (lockseat method) booking is failed
    public boolean book(int seatnumber, String name) throws RemoteException {
        // lock seat on all servers
        ArrayList<Server> serverList = primaryServer.getServerList();
        System.out.println("Booking seat " + (seatnumber + 1) + " for " + name);
        for (Server s : serverList) {
            if (!s.lockSeat(seatnumber)) {
                // if any server returns false, unlock all servers and return false
                for (Server s2 : serverList) {
                    s2.unlockFailed(seatnumber);
                }
                System.out.println("Booking failed for seat " + (seatnumber + 1));
                return false;
            }
        }
        // if all servers return true, update seat map on all servers
        for (Server s : serverList) {
            s.unlockSuccess(seatnumber, name);
        }
        System.out.println("Booking success for seat " + (seatnumber + 1) + " for " + name);
        primaryServer.updateSeatMap(seatnumber, name);
        return true;
    }

    // leave a seat, a server can only be able to process a leaving
    // successfully
    // after it sends lockseat requests to all servers and be able to lock the seat
    // successfully on all servers
    // if any server returns false (lockseat method) leaving is failed
    public boolean leave(int seatnumber) throws RemoteException {
        // lock seat on all servers
        ArrayList<Server> serverList = primaryServer.getServerList();
        System.out.println("Leaving seat " + (seatnumber + 1));
        for (Server s : serverList) {
            if (!s.lockSeat(seatnumber)) {
                // if any server returns false, unlock all servers and return false
                for (Server s2 : serverList) {
                    s2.unlockFailed(seatnumber);
                }
                System.out.println("Leaving failed for seat " + (seatnumber + 1));
                return false;
            }
        }
        // if all servers return true, update seat map on all servers
        for (Server s : serverList) {
            s.unlockSuccess(seatnumber, null);
        }
        System.out.println("Leaving success for seat " + (seatnumber + 1));
        primaryServer.updateSeatMap(seatnumber, null);
        return true;
    }

    // unlock seat on the current server, this is called after a booking is done
    // successfully and the seat is now occupied
    public void unlockSuccess(int seatnumber, String name) throws RemoteException {
        System.out.println("Unlocking seat " + (seatnumber + 1) + " for " + name + " (Success)");
        seatMap[seatnumber] = name;
        lockMap[seatnumber] = 0;
    }

    // unlock due to conflict, this is called after booking is failed and seat
    // becomes available again
    public void unlockFailed(int seatnumber) throws RemoteException {
        System.out.println("Unlocking seat " + (seatnumber + 1) + "(Failed)");
        lockMap[seatnumber] = 0;
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
        primaryServer.removeServer(this);
        System.out.println("Server closed.");
        System.exit(0);
    }
}
