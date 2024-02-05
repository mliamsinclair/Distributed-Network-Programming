import java.rmi.*;
import java.util.ArrayList;

/*
 * /Users/liamsinclair/Documents/Documents/jdk1.8.0_202.jdk/Contents/Home/bin/java -Djava.security.policy=all.policy  ServerDriver
 */

// this is the driver class for the server
// system is fault tolerant since clients connect to directly
// to one another, only primary server is needed to always be running
public class ServerDriver {
       public static void main(String[] args) throws Exception {
              // Create and install a security manager
              if (System.getSecurityManager() == null) {
                     System.setSecurityManager(new RMISecurityManager());
              }
              // check for primary server
              System.out.println("Checking for primary server...");
              boolean isPrimary = false;
              try {
                     // looking up primary server
                     PrimaryServer primaryServer = (PrimaryServer) Naming.lookup("rmi://127.0.0.1/primaryserver");
                     // if no exception is thrown, primary server is found
                     System.out.println("Primary server found.");
              } catch (Exception e) {
                     // if exception is thrown, primary server is found not found
                     System.out.println("Primary server not found.");
                     isPrimary = true;
              }
              // Start primary server
              if (isPrimary) {
                     System.out.println("Starting primary server...");
                     // create registry
                     java.rmi.registry.LocateRegistry.createRegistry(1099);
                     PrimaryServer primaryServer = new PrimaryServerImpl();
                     Naming.rebind("primaryserver", primaryServer);
                     System.out.println("Primary server started.");
                     // listing for close command
                     CloseThread closeThread = new CloseThread(primaryServer, null, true);
                     closeThread.start();
                     try {
                            // wait for close command
                            closeThread.join();
                            Naming.unbind("primaryserver");
                            System.exit(0);
                     } catch (Exception e) {
                            System.out.println("Error closing server.\n" + e);
                     }
              } else {
                     // Start regular server
                     System.out.println("Starting regular server...");
                     // get primary server
                     PrimaryServer primaryServer = (PrimaryServer) Naming.lookup("rmi://127.0.0.1/primaryserver");
                     Server server = new ServerImpl(primaryServer);
                     // add server to primary server
                     primaryServer.addServer(server);
                     // listen for close command
                     CloseThread closeThread = new CloseThread(primaryServer, server, false);
                     closeThread.start();
                     try {
                            // wait for close command
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
                            // listen for close command
                            while (true) {
                                   String input = System.console().readLine();
                                   if (input.equals("close")) {
                                          break;
                                   }
                            }
                            // shutdown command - close all servers
                            if (isPrimary) {
                                   shutdown(primaryServer);
                            } else {
                                   // not primary - remove itself from server list
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
                     // remove all servers from server list
                     ArrayList<Server> serverList = new ArrayList<>(primaryServer.getServerList());
                     System.out.println(serverList.size() + " servers connected.");
                     // wait for all servers to close
                     for (int i = 0; i < serverList.size(); i++) {
                            // start threads to close servers
                            ShutdownThread shutdownThread = new ShutdownThread(primaryServer, i);
                            shutdownThread.start();
                     }
              } catch (Exception e) {
                     System.out.println("Connections closed." + e);
              }
       }

       // thread class to shut down servers without exiting
       // this way each server can safely be closed without calling
       // System.exit(0) which would close primary server before
       // all other servers are closed
       public static class ShutdownThread extends Thread {
              private PrimaryServer primaryServer;
              private int index;

              public ShutdownThread(PrimaryServer primaryServer, int index) {
                     this.primaryServer = primaryServer;
                     this.index = index;
              }

              public void run() {
                     try {
                            // remove server from server list and shut it down
                            ArrayList<Server> serverList = new ArrayList<>(primaryServer.getServerList());
                            System.out.println("Shutting down server " + (index + 1) + "...");
                            serverList.get(index).close();
                     } catch (Exception e) {
                            // exception will be raised but no action is needed
                     }
              }
       }
}
