import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class QClient {

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java QClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(
                        new InputStreamReader(System.in))) {
            Thread cli = new Thread(new processCLI(stdIn, out, in, echoSocket));
            cli.start();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equalsIgnoreCase("Close")) {
                    System.out.println("Server closed connection.");
                    System.out.println(in.readLine());
                    stdIn.close();
                    in.close();
                    out.close();
                    echoSocket.close();
                    System.exit(0);
                }
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

    private static class processCLI implements Runnable {
        private static String currentInput;
        private static BufferedReader stdIn;
        private static PrintWriter out;
        private static BufferedReader in;
        private static Socket echoSocket;

        public processCLI(BufferedReader stdIn, PrintWriter out, BufferedReader in, Socket echoSocket) {
            QClient.processCLI.stdIn = stdIn;
            QClient.processCLI.out = out;
            QClient.processCLI.in = in;
            QClient.processCLI.echoSocket = echoSocket;
        }

        public void run() {
            try {
                while ((currentInput = stdIn.readLine()) != null) {
                    out.println(currentInput);
                    if (currentInput.equalsIgnoreCase("Bye.")) {
                        System.out.println(in.readLine());
                        stdIn.close();
                        out.close();
                        in.close();
                        echoSocket.close();
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                System.out.println("Server closed connection unexpectedly.");
            }
        }
    }
}
