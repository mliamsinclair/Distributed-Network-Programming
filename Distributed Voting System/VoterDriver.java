import java.rmi.*;
import java.util.ArrayList;
import java.util.Scanner;

public class VoterDriver {
       public static void main(String[] args) throws Exception {
              // Create and install a security manager
              if (System.getSecurityManager() == null) {
                     System.setSecurityManager(new RMISecurityManager());
              }
              // Get primary server
              PrimaryServer primaryServer = (PrimaryServer) Naming.lookup("rmi://127.0.0.1/primaryserver");
              // Get server list
              System.out.println("Getting server list...");
              ArrayList<Server> serverList = primaryServer.getServerList();
              // Choose a server to start booking seat procedure
              // Choose the server with the least number of clients
              boolean connected = false;
              // make sure list of servers if valid
              for (Server s : serverList) {
                     try {
                            s.getNumClients();
                     } catch (Exception e) {
                            // if server is not connected to primary server, remove it from the list
                            primaryServer.removeServer(s);
                     }
              }
              // get server list again
              serverList = primaryServer.getServerList();
              int min = serverList.get(0).getNumClients();
              int serverIndex = 0;
              for (Server s : serverList) {
                     if (s.getNumClients() < min) {
                            min = s.getNumClients();
                            serverIndex = serverList.indexOf(s);
                     }
              }
              Server server = serverList.get(serverIndex);
              // Connect to the server
              try {
                     server.connect();
                     connected = true;
              } catch (Exception e) {
                     primaryServer.removeServer(server);

              }
              // if connection fails, try again until connected
              while (!connected) {
                     // Get server list
                     System.out.println("Getting server list...");
                     serverList = primaryServer.getServerList();
                     // Choose the server with the least number of clients
                     min = serverList.get(0).getNumClients();
                     serverIndex = 0;
                     for (Server s : serverList) {
                            if (s.getNumClients() < min) {
                                   min = s.getNumClients();
                                   serverIndex = serverList.indexOf(s);
                            }
                     }
                     server = serverList.get(serverIndex);
                     // Connect to the server
                     try {
                            server.connect();
                            connected = true;
                     } catch (Exception e) {
                            primaryServer.removeServer(server);
                     }
              }
              // get client name
              Scanner scan = new Scanner(System.in);
              System.out.print("Enter your name: ");
              String name = scan.nextLine();
              // create voter
              Voter voter = new VoterImpl(name, scan);
              // add voter to voter list
              try {
                     server.addVoter(voter);
              } catch (Exception e) {
                     primaryServer.removeServer(server);
                     serverList = primaryServer.getServerList();
                     // Choose a server to with the least number of clients
                     min = serverList.get(0).getNumClients();
                     serverIndex = 0;
                     for (Server s : serverList) {
                            if (s.getNumClients() < min) {
                                   min = s.getNumClients();
                                   serverIndex = serverList.indexOf(s);
                            }
                     }
                     server = serverList.get(serverIndex);
                     // Connect to the server
                     server.connect();
              }
              System.out.println("Voter " + name + " added.");
              // get voter list
              ArrayList<Voter> voterList = server.getRandomVoters();
              System.out.println("Voter list obtained:");
              for (Voter v : voterList) {
                     System.out.println("- " + v.getName());
                     voter.addNeighbor(v);
              }
              // print number of voters
              System.out.println("Number of voters: " + voter.getNumNeighbors());
              // create menu
              String menu = "\n===== Menu =====\n1. Start a new election\n2. Get a list of all neighboring voters\n3. Exit\n\nEnter the number of your choice: ";
              // print menu
              boolean skip = false;
              System.out.println(menu);
              while (true) {
                     int choice = scan.nextInt();
                     while (voter.getElectionRunning() && !voter.getVoted()) {
                            Voter electionRunner = voter.getElectionRunner();
                            if (choice == 1) {
                                   electionRunner.vote(true);
                                   System.out.println("Vote submitted for " + electionRunner.getName() + ".");
                            } else if (choice == 2) {
                                   electionRunner.vote(false);
                                   System.out.println("Vote submitted for " + electionRunner.getName() + ".");
                            } else {
                                   System.out.println("Invalid choice.");
                            }
                            // print menu
                            System.out.println(menu);
                            choice = scan.nextInt();
                     }

                     if (choice == 1) {
                            // check if election is already in progress
                            if (voter.getElectionRunning()) {
                                   System.out.println("An election is already in progress.");
                                   continue;
                            }
                            // start election
                            System.out.println("Enter topic (Must be a yes or no question): ");
                            scan.nextLine();
                            String topic = scan.nextLine();
                            voter.election(topic);
                            skip = true;
                     } else if (choice == 2) {
                            // get list of all neighboring voters
                            Voter[] neighbors = voter.getNeighbors();
                            System.out.println("Neighboring voters:");
                            for (Voter v : neighbors) {
                                   System.out.println("- " + v.getName());
                            }
                     } else if (choice == 3) {
                            // exit
                            System.out.println("Exiting...");
                            // disconnect from server
                            server.disconnect();
                            // remove voter from voter list
                            server.removeVoter(voter);
                            System.exit(0);
                     } else {
                            // invalid choice
                            System.out.println("Invalid choice.");
                     }
                     // print menu
                     if (!skip) {
                            System.out.println(menu);
                            skip = false;
                     }
              }
       }

}
