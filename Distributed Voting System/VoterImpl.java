import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Scanner;

public class VoterImpl extends UnicastRemoteObject implements Voter {
    private ArrayList<Voter> neighbors;
    private String name;
    private ArrayList<Boolean> votes;
    private String topic;
    private Scanner scanner;
    private boolean electionRunning;
    private Voter electionRunner;
    private boolean voted;

    public VoterImpl(String name, Scanner scanner) throws RemoteException {
        super();
        this.name = name;
        this.scanner = scanner;
        neighbors = new ArrayList<>();
        votes = new ArrayList<>();

    }

    public void setName(String name) throws RemoteException {
        this.name = name;
    }

    public String getName() throws RemoteException {
        return name;
    }

    public synchronized boolean election(String topic) throws RemoteException {
        String menu = "\n===== Menu =====\n1. Start a new election\n2. Get a list of all neighboring voters\n3. Exit\n\nEnter the number of your choice: ";
        if (electionRunning) {
            System.out.println("Election already running.");
            return false;
        }
        electionRunning = true;
        setAllElectionRunning(electionRunning, this);
        System.out.println("Starting election for " + topic);
        this.topic = topic;
        if (neighbors.size() > 0) {
            for (Voter voter : neighbors) {
                voter.broadcast("\n\n===========\nNew election started!\nTopic: " + topic
                        + "\n===========\n\nEnter your vote (1=yes/2=no): ");
            }
            // get current voter's vote
            int choice = scanner.nextInt();
            while (choice != 1 && choice != 2) {
                System.out.println("Invalid choice.");
                System.out.println("Enter the vote for your topic (1=yes/2=no): ");
                choice = scanner.nextInt();
            }
            if (choice == 1) {
                votes.add(true);
                System.out.println("Vote submitted.");
            } else if (choice == 2) {
                votes.add(false);
                System.out.println("Vote submitted.");
            } else {
                System.out.println("Invalid choice.");
            }
            // wait for all votes
            System.out.println("Waiting for other voters to vote...");
            while (votes.size() < neighbors.size()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
            // count votes
            int yes = 0;
            int no = 0;
            for (boolean vote : votes) {
                if (vote) {
                    yes++;
                } else {
                    no++;
                }
            }
            boolean result = yes > no;
            if (result) {
                for (Voter voter : neighbors) {
                    voter.broadcast("\n\n=== Election result: Yes ===\n\n" + menu);
                    voter.setVoted(false);
                }
            } else {
                for (Voter voter : neighbors) {
                    voter.broadcast("\n=== Election result: No ===\n" + menu);
                    voter.setVoted(false);
                }
            }
            votes.clear();
            topic = null;
            electionRunning = false;
            setAllElectionRunning(electionRunning, this);
            return result;
        }
        electionRunning = false;
        setAllElectionRunning(electionRunning, this);
        System.out.println("Not enough voters to start election.");
        return false;
    }

    public String getTopic() throws RemoteException {
        if (topic == null) {
            return "No election is running.";
        }
        return topic;
    }

    public void broadcast(String message) throws RemoteException {
        System.out.println(message);
    }

    public void addNeighbor(Voter voter) throws RemoteException {
        if (neighbors.contains(voter)) {
            return;
        }
        neighbors.add(voter);
    }

    public Voter[] getNeighbors() throws RemoteException {
        return neighbors.toArray(new Voter[neighbors.size()]);
    }

    public void removeNeighbor(Voter voter) throws RemoteException {
        neighbors.remove(voter);
    }

    public int getNumNeighbors() throws RemoteException {
        return neighbors.size();
    }

    public void vote(boolean vote) throws RemoteException {
        votes.add(vote);
        voted = true;
    }

    public void setElectionRunning(boolean electionRunning, Voter v) throws RemoteException {
        this.electionRunning = electionRunning;
        if (electionRunning) {
            electionRunner = v;
        } else {
            electionRunner = null;
        }
    }

    public boolean getElectionRunning() throws RemoteException {
        return electionRunning;
    }

    public void setAllElectionRunning(boolean electionRunning, Voter v) throws RemoteException {
        for (Voter voter : neighbors) {
            voter.setElectionRunning(electionRunning, v);
        }
    }

    public Voter getElectionRunner() throws RemoteException {
        return electionRunner;
    }

    public boolean getVoted() throws RemoteException {
        return voted;
    }

    public void setVoted(boolean voted) throws RemoteException {
        this.voted = voted;
    }
}