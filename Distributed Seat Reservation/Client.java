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
              // menu to be displayed to the user
              String menu = "\n==== Menu ====\n1. Book a seat\n2. Leave a seat\n3. Print seats\n4.Exit\n\nEnter choice: ";
              int choice = 0;
              boolean reconnect = false;
              int seatnumber = 0;
              String name = "";
              // print all seats
              System.out.println(server.seatMapPrint());
              // loop until user exits
              while (choice != 4) {
                     try {
                            // if reconnect is true, skip the menu and go straight to the choice
                            // reconnect is true when the server disconnects and a new server is acquired
                            if (reconnect) {
                                   // same switch statement as below but without the print menu
                                   switch (choice) {
                                          // book a seat
                                          case 1:
                                                 if (server.book(seatnumber - 1, name)) {
                                                        System.out.println("\nBooking success.");
                                                 } else {
                                                        System.out.println("\nBooking failed.");
                                                 }
                                                 break;
                                          // leave a seat
                                          case 2:
                                                 if (server.leave(seatnumber - 1)) {
                                                        System.out.println("\nLeaving success.");
                                                 } else {
                                                        System.out.println("\nLeaving failed.");
                                                 }
                                                 break;
                                          // print seats
                                          case 3:
                                                 System.out.println(server.seatMapPrint());
                                                 break;
                                          // invalid choice
                                          default:
                                                 System.out.println("Invalid choice.");
                                                 break;
                                   }
                                   reconnect = false;
                                   // if reconnect is false, print the menu and get user input
                            } else {
                                   // print menu
                                   System.out.print(menu);
                                   // get user input
                                   choice = Integer.parseInt(System.console().readLine());
                                   // switch statement for the menu
                                   switch (choice) {
                                          // book a seat
                                          case 1:
                                                 System.out.print("Enter seat number: ");
                                                 seatnumber = Integer.parseInt(System.console().readLine());
                                                 System.out.print("Enter name: ");
                                                 name = System.console().readLine();
                                                 while (name.equals("") || name == null) {
                                                        System.out.print("Enter name: ");
                                                        name = System.console().readLine();
                                                 }
                                                 if (server.book(seatnumber - 1, name)) {
                                                        System.out.println("\nBooking success.");
                                                 } else {
                                                        System.out.println("\nBooking failed. Seat is not available.");
                                                 }
                                                 break;
                                          // leave a seat
                                          case 2:
                                                 System.out.print("Enter seat number: ");
                                                 seatnumber = Integer.parseInt(System.console().readLine());
                                                 if (server.leave(seatnumber - 1)) {
                                                        System.out.println("\nLeaving success.");
                                                 } else {
                                                        // if the seat is not occupied, it cannot be left
                                                        System.out.println("\nLeaving failed. Seat is not occupied.");
                                                 }
                                                 break;
                                          // print seats
                                          case 3:
                                                 System.out.println(server.seatMapPrint());
                                                 break;
                                          // exit
                                          case 4:
                                                 System.out.println("Exiting...");
                                                 server.disconnect();
                                                 break;
                                          // invalid choice
                                          default:
                                                 System.out.println("Invalid choice.");
                                                 break;
                                   }
                            }
                     } catch (NumberFormatException e) {
                            // if the user enters a non-integer value
                            System.out.println("Invalid input.");
                     } catch (Exception e) {
                            // if the server disconnects
                            // this print state is only for visibility purposes
                            System.out.println(
                                          "\n(Printing for visibility purposes, would not be visible to client)\nServer disconnected. Attempting to reconnect...");
                            // remove the server from the server list
                            try {
                                   primaryServer.removeServer(server);
                                   // get new server list
                                   serverList = primaryServer.getServerList();
                                   min = serverList.get(0).getNumClients();
                                   serverIndex = 0;
                                   // choose the server with the least number of clients
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
                                   // if the primary server disconnects
                                   System.out.println("Primary server closed the connection. Exiting...");
                                   System.exit(0);
                            }
                     }
              }

       }
}