import java.rmi.*;

public interface Voter extends Remote {
    // save the voter's name
    void setName(String name) throws RemoteException;

    // return the voter's name
    String getName() throws RemoteException;

    // start a new election
    // must have at least more than one topic
    // returns result of the election
    boolean election(String topic) throws RemoteException;

    // return the current election topic
    String getTopic() throws RemoteException;

    // broadcast the topic/result of the election or a message
    void broadcast(String message) throws RemoteException;

    // add neighboring voters
    void addNeighbor(Voter voter) throws RemoteException;

    // return the neighboring voters
    Voter[] getNeighbors() throws RemoteException;

    // remove neighboring voter
    void removeNeighbor(Voter voter) throws RemoteException;

    // get number of neighboring voters
    int getNumNeighbors() throws RemoteException;

    // vote in an election
    void vote(boolean vote) throws RemoteException;

    // set election running
    void setElectionRunning(boolean electionRunning, Voter v) throws RemoteException;

    // get election running
    boolean getElectionRunning() throws RemoteException;

    // set all election running
    void setAllElectionRunning(boolean electionRunning, Voter v) throws RemoteException;

    // get election runner
    Voter getElectionRunner() throws RemoteException;

    // get voted
    boolean getVoted() throws RemoteException;

    // set voted
    void setVoted(boolean voted) throws RemoteException;
}