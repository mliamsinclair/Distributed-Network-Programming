import java.rmi.*;

public interface Server extends Remote {
    // return current seat map
    String[] seatMap() throws RemoteException;

    // return printable seat map
    String seatMapPrint() throws RemoteException;

    // lock seat on the current server, it is called by regular servers
    // return true only when the seat is available and not already locked
    boolean lockSeat(int seatnumber) throws RemoteException;

    // try to book a seat, a server can only be able to process a booking
    // successfully
    // after it sends lockseat requests to all servers and be able to lock the seat
    // successfully on all servers
    // if any server returns false (lockseat method) booking is failed
    boolean book(int seatnumber, String name) throws RemoteException;

    // leave a seat, this is called by regular servers
    // return true only when the seat is occupied and not already locked
    boolean leave(int seatnumber) throws RemoteException;

    // unlock seat on the current server, this is called after a booking is done
    // successfully and the seat is now occupied
    void unlockSuccess(int seatnumber, String name) throws RemoteException;

    // unlock due to conflict, this is called after booking is failed and seat
    // becomes available again
    void unlockFailed(int seatnumber) throws RemoteException;

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
}
