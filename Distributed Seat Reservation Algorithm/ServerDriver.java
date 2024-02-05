import java.rmi.*;
import java.util.ArrayList;

/*
 * /Users/liamsinclair/Documents/Documents/jdk1.8.0_202.jdk/Contents/Home/bin/java -Djava.security.policy=all.policy  ServerDriver
 */

public class ServerDriver {
       public static void main(String[] args) throws Exception {
              // Create and install a security manager
              if (System.getSecurityManager() == null) {
                     System.setSecurityManager(new RMISecurityManager());
              }
              System.out.println("Checking for primary server...");
              boolean isPrimary = false;
              try {
                     PrimaryServer primaryServer = (PrimaryServer) Naming.lookup("rmi://127.0.0.1/primaryserver");
                     primaryServer.getSeatMap();
                     System.out.println("Primary server found.");
              } catch (Exception e) {
                     System.out.println("Primary server not found.");
                     isPrimary = true;
              }
              // Start primary server
              if (isPrimary) {
                     System.out.println("Starting primary server...");
                     java.rmi.registry.LocateRegistry.createRegistry(1099);
                     PrimaryServer primaryServer = new PrimaryServerImpl();
                     Naming.rebind("primaryserver", primaryServer);
                     System.out.println("Primary server started.");
                     CloseThread closeThread = new CloseThread(primaryServer, null, true);
                     closeThread.start();
                     try {
                            closeThread.join();
                            System.exit(0);
                     } catch (Exception e) {
                            System.out.println("Error closing server.\n" + e);
                     }
              }
              // Start regular server
              else {
                     System.out.println("Starting regular server...");
                     PrimaryServer primaryServer = (PrimaryServer) Naming.lookup("rmi://127.0.0.1/primaryserver");
                     Server server = new ServerImpl(primaryServer);
                     primaryServer.addServer(server);
                     CloseThread closeThread = new CloseThread(primaryServer, server, false);
                     closeThread.start();
                     try {
                            closeThread.join();
                            System.exit(0);
                     } catch (Exception e) {
                            System.out.println("Error closing server.\n" + e);
                     }
              }
       }

       // thread class to listen for close command
       // if the server is primary, remove all servers from the server list
       // if the server is regular, remove itself from the server list
       // then exit
       public static class CloseThread extends Thread {
              private PrimaryServer primaryServer;
              private Server server;
              private boolean isPrimary;

              public CloseThread(PrimaryServer primaryServer, Server server, boolean isPrimary) {
                     this.primaryServer = primaryServer;
                     this.server = server;
                     this.isPrimary = isPrimary;
              }

              public void run() {
                     try {
                            System.out.println("Enter 'close' at any time to close the server.");
                            while (true) {
                                   String input = System.console().readLine();
                                   if (input.equals("close")) {
                                          break;
                                   }
                            }
                            if (isPrimary) {
                                   shutdown(primaryServer);
                            } else {
                                   server.close();
                            }
                     } catch (Exception e) {
                            System.out.println("Error closing server." + e);
                     }
              }
       }

       // primary server shutdown method
       public static void shutdown(PrimaryServer primaryServer) {
              try {
                     ArrayList<Server> serverList = primaryServer.getServerList();
                     for (Server s : serverList) {
                            s.close();
                     }
                     // wait for all servers to close
                     while (!primaryServer.getServerList().isEmpty()) {
                            Thread.sleep(1000);
                     }
                     Naming.unbind("primaryserver");
              } catch (Exception e) {
                     System.out.println("Connections closed.");
              }
       }
}
