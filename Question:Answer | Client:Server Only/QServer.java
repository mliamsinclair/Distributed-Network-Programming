import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

// Question/Answer Server
public class QServer {

    public static void main(String[] args) throws IOException {
        // Check for correct number of arguments
        if (args.length != 1) {
            System.err.println("Usage: java QAServer <port number>");
            System.exit(1);
        }
        // Check for valid port number
        int portNumber = Integer.parseInt(args[0]);
        // Initialize controller, passed to all client handlers
        QAController controller = new QAController();
        // Start server
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]))) {
            // Start thread that will close the server when the user types "Bye."
            Thread close = new Thread(new CloseServer());
            close.start();
            System.out.println("Server started on port " + portNumber + ".");
            System.out.println("Type 'Bye.' to close the server.");
            // Accept client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Start client handler thread
                ClientHandler handler = new ClientHandler(clientSocket, controller);
                handler.start();
            }
            // Catch exceptions
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    // Client handler thread
    private static class ClientHandler extends Thread {
        // Client socket
        private Socket connection;
        // Controller passed to all client handlers
        private QAController controller;
        // Flag to interrupt thread
        private boolean shouldInterrupt = false;

        // Constructor
        public ClientHandler(Socket s, QAController controller) {
            connection = s;
            this.controller = controller;
        }

        // Interrupt thread
        public boolean shouldInterrupt() {
            return shouldInterrupt;
        }

        // Run thread
        public void run() {
            String name = "", answer, question, mode = "";
            try {
                // Initialize input and output streams
                PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // Notify server that client has connected
                System.out.println("Client Connected.");
                // Initialize shared client data object
                ClientData input = new ClientData();
                // Start threads to listen for client input and close client connection
                Thread clientListen = new Thread(new FromClient(in, input, this, out));
                clientListen.start();
                // Start thread to close client connection via server input
                Thread close = new Thread(new CloseClient(out, input, in));
                close.start();
                // Get client name and mode
                out.println("Welcome to the Question/Answer Server!");
                out.println("Enter 'Q' if you are a Questioner or 'A' if you are an Answerer: ");
                while (!(mode = input.getUserInput()).equalsIgnoreCase("Q")
                        && !mode.equalsIgnoreCase("A") && !mode.equalsIgnoreCase("Bye.")) {
                    out.println("Enter 'Q' if you are a Questioner or 'A' if you are an Answerer: ");
                }
                // Check if client disconnected
                if (mode.equalsIgnoreCase("Bye.")) {
                    input.shouldInterrupt();
                    connection.close();
                    return;
                }
                // Set client mode
                input.setMode(mode.toUpperCase());
                // Get client name
                out.println("Enter your name: ");
                name = input.getUserInput();
                // Check if client disconnected
                if (name.equalsIgnoreCase("Bye.")) {
                    input.shouldInterrupt();
                    connection.close();
                    return;
                }
                // Set client name
                input.setName(name);
                // Start client in questioner mode
                if (mode.equalsIgnoreCase("Q")) {
                    // Get question and answer from client
                    while (!shouldInterrupt) {
                        out.println("Enter a question or 'Bye.' to quit: ");
                        question = input.getUserInput();
                        // Check if client disconnected
                        if (question.equalsIgnoreCase("Bye.")) {
                            input.shouldInterrupt();
                            connection.close();
                            return;
                        }
                        out.println("Enter the answer to your question: ");
                        answer = input.getUserInput();
                        // Check if client disconnected
                        if ((answer.equalsIgnoreCase("Bye."))) {
                            input.shouldInterrupt();
                            connection.close();
                            return;
                        }
                        // Add question to controller
                        controller.putQuestion(question + "###" + answer, out);
                        // Increment question counter
                        input.incrementCounter();
                    }
                    // Start client in answerer mode
                } else if (mode.equalsIgnoreCase("A")) {
                    int numQuestions;
                    int result;
                    // Get question from controller and send to client
                    while (!shouldInterrupt) {
                        out.println("Fetching question...");
                        // Get question from controller
                        question = controller.getQuestion(out);
                        // Get question number
                        numQuestions = controller.getCounter();
                        out.println("Question: " + question);
                        out.println("Enter your answer or 'Bye.' to quit: ");
                        // Get answer from client
                        answer = input.getUserInput();
                        // Check if client disconnected
                        if (answer.equalsIgnoreCase("Bye.")) {
                            input.shouldInterrupt();
                            connection.close();
                            return;
                        }
                        // Answer question
                        result = controller.answerQuestion(answer, numQuestions);
                        // Correct answer
                        if (result == 1) {
                            out.println("Correct!");
                            input.incrementCounter();
                            // Incorrect answer
                        } else if (result == -1) {
                            out.println("Incorrect!");
                            // Two second penalty
                            out.println("Two second penalty!");
                            Thread.sleep(2000);
                            // Question already answered
                        } else if (result == 0) {
                            out.println("Faster next time!");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Notify server that client has disconnected
            System.out.println(mode.toUpperCase() + ": " + name + " Disconnected.");
        }
    }

    // Close server thread
    private static class CloseServer implements Runnable {
        // Run thread
        public void run() {
            // Listen for server input via command line
            Scanner sc = new Scanner(System.in);
            String input;
            // Close server when user types "Bye."
            while ((input = sc.nextLine()) != null) {
                if (input.equalsIgnoreCase("Bye.")) {
                    System.out.println("Server closing...");
                    // Close all client connections and send closing message to clients
                    CloseClient.closeAll();
                    // Wait for ten seconds for all client connections to close
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Close server
                    System.out.println("Server closed.");
                    sc.close();
                    System.exit(0);
                }
            }
        }
    }

    // Client input thread
    private static class FromClient implements Runnable {
        private String currentInput = "-";
        private BufferedReader in;
        private ClientData data;
        private ClientHandler handler;
        private PrintWriter out;

        // Constructor
        public FromClient(BufferedReader in, ClientData data, ClientHandler handler, PrintWriter out) {
            this.in = in;
            currentInput = "-";
            this.data = data;
            this.handler = handler;
            this.out = out;
        }

        // Run thread
        public void run() {
            // Listen for client input
            try {
                while ((currentInput = in.readLine()) != null) {
                    // Set current input in shared client data object
                    data.setCurrentInput(currentInput);
                    // Check if client disconnected
                    if (currentInput.equalsIgnoreCase("Bye.")) {
                        // Interrupt client handler thread
                        handler.shouldInterrupt();
                        // Set shouldInterrupt flag in shared client data object
                        data.shouldInterrupt();
                        // Close client connection
                        // Check if client was in questioner or answerer mode
                        if (data.getMode().equalsIgnoreCase("Q")) {
                            // Notify server that client disconnected
                            System.out.println("Questioner " + data.getName() + " disconnected. Questions asked: "
                                    + data.getCounter());
                            // Send closing message to client
                            out.println("Questions asked: " + data.getCounter() + " Thanks for playing "
                                    + data.getName() + "!");
                        } else if (data.getMode().equalsIgnoreCase("A")) {
                            // Notify server that client disconnected
                            System.out.println("Answerer " + data.getName() + " disconnected." + " Final score: "
                                    + data.getCounter());
                            // Send closing message to client
                            out.println("Final score: " + data.getCounter() + " Thanks for playing " + data.getName()
                                    + "!");
                        } else {
                            // Notify server that client disconnected
                            System.out.println("Client disconnected.");
                        }
                        // Close I/O streams
                        in.close();
                        out.close();
                        return;
                    }
                }
                // Notify server that client disconnected
                System.out.println(data.getName() + " disconnected.");
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on the port or listening for a connection");
                System.out.println(e.getMessage());
            }
        }
    }

    // Controller class
    private static class QAController {
        private String question;
        private int counter;
        private boolean empty = true;

        // Add question to controller: Only one question can be added at a time
        // Question is stored as a string with the question and answer separated by
        // "###"
        public synchronized void putQuestion(String q, PrintWriter out) {
            // Check if question already exists
            if (!empty) {
                // Notify client that question already exists
                out.println("Waiting for question to be answered.");
            }
            // Wait for question to be answered
            while (!empty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                }
            }
            // Add question to controller
            empty = false;
            question = q;
            counter++;
            // Notify server that question was added
            System.out.println("Question added: " + question);
            // Notify all threads that question was added
            notifyAll();
        }

        // Get question from controller: Only one question can be retrieved at a time
        // Only the question is returned to the client
        public synchronized String getQuestion(PrintWriter out) {
            // Check if question exists
            if (empty) {
                out.println("No questions available. Waiting...");
            }
            // Wait for question to be added
            while (empty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                }
            }
            // Return question to client
            String[] returnQ = question.split("###");
            // Notify all threads that question was retrieved
            notifyAll();
            // Return question
            return returnQ[0];
        }

        public synchronized int answerQuestion(String answer, int num) {
            // If the question has already been answered, return 0
            if (empty || num != counter) {
                return 0;
            }
            // Get answer from question
            String[] questionArray = question.split("###");
            // Check if answer is correct
            if (questionArray[1].equalsIgnoreCase(answer)) {
                // Set question to empty
                empty = true;
                // Notify all threads that question was answered
                notifyAll();
                // Return 1 if answer is correct
                return 1;
            } else {
                // Return -1 if answer is incorrect
                return -1;
            }
        }

        // Get question counter
        public int getCounter() {
            return counter;
        }
    }

    // Shared client data object
    private static class ClientData {
        private boolean shouldInterrupt = false;
        private String currentInput = "-";
        private int counter = 0;
        private String mode = "";
        private String name = "";

        // Set shouldInterrupt flag
        public synchronized void shouldInterrupt() {
            shouldInterrupt = true;
        }

        // Get shouldInterrupt flag
        public synchronized boolean getShouldInterrupt() {
            return shouldInterrupt;
        }

        // Set current input
        public synchronized void setCurrentInput(String input) {
            currentInput = input;
            notifyAll();
        }

        // Reset current input
        public synchronized void resetCurrentInput() {
            currentInput = "-";
        }

        // Get current input
        /*
         * The reason for having it done in this way is so that a client disconnect
         * doesn't cause the server to wait for input that will never come and may
         * be handled at any time
         */
        public String getUserInput() {
            // Check if client disconnected
            if (shouldInterrupt) {
                return "Bye.";
            }
            // Otherwise, reset current input wait for client input
            resetCurrentInput();
            while (currentInput.equals("-")) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                }
            }
            // Return client input
            return currentInput;
        }

        // Increment question counter
        public void incrementCounter() {
            counter++;
        }

        // Get question counter
        public int getCounter() {
            return counter;
        }

        // Set client mode
        public void setMode(String mode) {
            this.mode = mode;
        }

        // Get client mode
        public String getMode() {
            return mode;
        }

        // Set client name
        public void setName(String name) {
            this.name = name;
        }

        // Get client name
        public String getName() {
            return name;
        }
    }

    // Close client connection thread
    private static class CloseClient implements Runnable {
        private static boolean shouldCloseClient = false;
        PrintWriter out;
        ClientData data;
        BufferedReader in;

        // Constructor
        public CloseClient(PrintWriter out, ClientData data, BufferedReader in) {
            this.out = out;
            this.data = data;
            this.in = in;
        }

        // Run thread
        public void run() {
            // Wait for server to close
            while (!shouldCloseClient && !data.getShouldInterrupt()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                }
            }
            // This is to prevent the server from sending the closing message twice
            if (data.getShouldInterrupt()) {
                return;
            }
            // Close client connection
            System.out.println("Closing client connection...");
            // Notify client that server is closing
            out.println("Close");
            // Check if client was in questioner or answerer mode
            if (data.getMode().equalsIgnoreCase("Q")) {
                // Notify server that client disconnected
                System.out.println("Questioner " + data.getName() + " disconnected. Questions asked: "
                        + data.getCounter());
                // Send closing message to client
                out.println("Questions asked: " + data.getCounter() + " Thanks for playing " + data.getName() + "!");
            } else if (data.getMode().equalsIgnoreCase("A")) {
                // Notify server that client disconnected
                System.out.println("Answerer " + data.getName() + " disconnected." + " Final score: "
                        + data.getCounter());
                // Send closing message to client
                out.println("Final score: " + data.getCounter() + " Thanks for playing " + data.getName() + "!");
            } else {
                // Notify server that client disconnected
                System.out.println("Client disconnected.");
            }
            try {
                // Close I/O streams
                in.close();
                out.close();
            } catch (IOException e) {
                System.out.println("Exception caught when trying to close client connection.");
                System.out.println(e.getMessage());
            }
        }

        // Close all client connections by setting shouldCloseClient flag
        public static void closeAll() {
            shouldCloseClient = true;
        }
    }
}