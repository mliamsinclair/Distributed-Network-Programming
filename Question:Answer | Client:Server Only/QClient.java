import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

// Client class
public class QClient {

    public static void main(String[] args) throws IOException {
        // Check for correct number of command line arguments
        if (args.length != 2) {
            System.err.println(
                    "Usage: java QClient <host name> <port number>");
            System.exit(1);
        }
        // Get host name and port number from command line arguments
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        try ( // Create socket and I/O streams
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(
                        new InputStreamReader(System.in))) {
            // Create thread to process user input
            Thread cli = new Thread(new ProcessCLI(stdIn, out, in, echoSocket));
            cli.start();
            String inputLine;
            // Read from server until server closes connection
            while ((inputLine = in.readLine()) != null) {
                // If server sends "Close" message, close connection
                if (inputLine.equalsIgnoreCase("Close")) {
                    System.out.println("Server closed connection.");
                    // Read closing message from server
                    System.out.println(in.readLine());
                    // All streams and socket are automatically closed by the try-with-resources
                    System.exit(0);
                }
                // Otherwise, print message from server
                System.out.println(inputLine);
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }

    // Thread to process user input
    private static class ProcessCLI implements Runnable {
        // I/O streams and socket
        private BufferedReader stdIn;
        private PrintWriter out;
        private BufferedReader in;
        private Socket echoSocket;

        // Constructor
        public ProcessCLI(BufferedReader stdIn, PrintWriter out, BufferedReader in, Socket echoSocket) {
            this.stdIn = stdIn;
            this.out = out;
            this.in = in;
            this.echoSocket = echoSocket;
        }

        // Run method
        public void run() {
            String currentInput;
            // Read from user input until user types "Bye."
            try {
                while ((currentInput = stdIn.readLine()) != null) {
                    // Send user input to server
                    out.println(currentInput);
                    // If user types "Bye.", close connection
                    if (currentInput.equalsIgnoreCase("Bye.")) {
                        // Give main method time to read server response
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // Close all streams and socket
                        closeAll();
                        // Exit program
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                System.out.println("Server closed connection unexpectedly.");
            }
        }

        // Close all streams and socket
        private void closeAll() throws IOException {
            stdIn.close();
            out.close();
            in.close();
            echoSocket.close();
        }
    }
}
