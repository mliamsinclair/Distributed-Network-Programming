import java.rmi.*;

/*
 * java -Djava.security.policy=all.policy  ServerDriver
 */

public class ServerDriver {
    public static void main(String[] args) {
        try {
            // create registry and bind server to registry
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }
            Server server = new ServerImpl();
            Naming.rebind("server", server);
        } catch (Exception e) {
            System.out.println("ServerDriver error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
