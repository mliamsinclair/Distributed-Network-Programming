import java.rmi.*;
import java.util.ArrayList;

public class Client {
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
              server.connect();
              // Start booking seat procedure
              System.out.println("Connection successful.");
              String menu = "\n==== Menu ====\n1. Book a seat\n2. Leave a seat\n3. Print seats\n4.Exit\n\nEnter choice: ";
              int choice = 0;
              boolean reconnect = false;
              int seatnumber = 0;
              String name = "";
              System.out.println(server.seatMapPrint());
              while (choice != 4) {
                     try {
                            if (reconnect) {
                                   switch (choice) {
                                          case 1:
                                                 if (server.book(seatnumber - 1, name)) {
                                                        System.out.println("\nBooking success.");
                                                 } else {
                                                        System.out.println("\nBooking failed.");
                                                 }
                                                 break;
                                          case 2:
                                                 if (server.leave(seatnumber - 1)) {
                                                        System.out.println("\nLeaving success.");
                                                 } else {
                                                        System.out.println("\nLeaving failed.");
                                                 }
                                                 break;
                                          case 3:
                                                 System.out.println(server.seatMapPrint());
                                                 break;
                                          default:
                                                 System.out.println("Invalid choice.");
                                                 break;
                                   }
                            } else {
                                   System.out.print(menu);
                                   choice = Integer.parseInt(System.console().readLine());
                                   switch (choice) {
                                          case 1:
                                                 System.out.print("Enter seat number: ");
                                                 seatnumber = Integer.parseInt(System.console().readLine());
                                                 System.out.print("Enter name: ");
                                                 name = System.console().readLine();
                                                 if (server.book(seatnumber - 1, name)) {
                                                        System.out.println("\nBooking success.");
                                                 } else {
                                                        System.out.println("\nBooking failed.");
                                                 }
                                                 break;
                                          case 2:
                                                 System.out.print("Enter seat number: ");
                                                 seatnumber = Integer.parseInt(System.console().readLine());
                                                 if (server.leave(seatnumber - 1)) {
                                                        System.out.println("\nLeaving success.");
                                                 } else {
                                                        System.out.println("\nLeaving failed.");
                                                 }
                                                 break;
                                          case 3:
                                                 System.out.println(server.seatMapPrint());
                                                 break;
                                          case 4:
                                                 System.out.println("Exiting...");
                                                 server.disconnect();
                                                 break;
                                          default:
                                                 System.out.println("Invalid choice.");
                                                 break;
                                   }
                            }
                     } catch (NumberFormatException e) {
                            System.out.println("Invalid input.");
                     } catch (Exception e) {
                            System.out.println(
                                          "\n(Printing for visibility purposes, would not be visible to client)\nServer disconnected. Attempting to reconnect...");
                            try {
                                   primaryServer.removeServer(server);
                                   serverList = primaryServer.getServerList();
                                   min = serverList.get(0).getNumClients();
                                   serverIndex = 0;
                                   for (Server s : serverList) {
                                          if (s.getNumClients() < min) {
                                                 min = s.getNumClients();
                                                 serverIndex = serverList.indexOf(s);
                                          }
                                   }
                                   server = serverList.get(serverIndex);
                                   server.connect();
                                   System.out.println("Reconnected.");
                                   reconnect = true;
                            } catch (Exception e2) {
                                   System.out.println("Primary server closed the connection. Exiting...");
                                   System.exit(0);
                            }
                     }
              }

       }
}