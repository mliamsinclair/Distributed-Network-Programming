import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class QServer {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java QAServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        QAController controller = new QAController();
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            Thread close = new Thread(new CloseServer());
            close.start();
            System.out.println("Server started on port " + portNumber + ".");
            System.out.println("Type 'Bye.' to close the server.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, controller);
                handler.start();
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket connection;
        private QAController controller;
        private boolean shouldInterrupt = false;

        public ClientHandler(Socket s, QAController controller) {
            connection = s;
            this.controller = controller;
        }

        public boolean shouldInterrupt() {
            return shouldInterrupt;
        }

        public void run() {
            String name = "", answer, question, mode = "";
            try {
                PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                System.out.println("Client Connected.");
                ClientData input = new ClientData();
                Thread clientListen = new Thread(new FromClient(in, input, this, out));
                clientListen.start();
                Thread close = new Thread(new CloseClient(out, input, in, this));
                close.start();
                out.println("Welcome to the Question/Answer Server!");
                out.println("Enter 'Q' if you are a Questioner or 'A' if you are an Answerer: ");
                while ((mode = input.getUserInput()).equalsIgnoreCase("Q") == false
                        && mode.equalsIgnoreCase("A") == false && mode.equalsIgnoreCase("Bye.") == false) {
                    out.println("Enter 'Q' if you are a Questioner or 'A' if you are an Answerer: ");
                }
                if (mode.equalsIgnoreCase("Bye.")) {
                    connection.close();
                    return;
                }
                input.setMode(mode.toUpperCase());
                out.println("Enter your name: ");
                name = input.getUserInput();
                if (name.equalsIgnoreCase("Bye.")) {
                    connection.close();
                    return;
                }
                input.setName(name);
                if (mode.equalsIgnoreCase("Q")) {
                    while (!shouldInterrupt) {
                        out.println("Enter a question or 'Bye.' to quit: ");
                        question = input.getUserInput();
                        if (question.equalsIgnoreCase("Bye.")) {
                            connection.close();
                            return;
                        }
                        out.println("Enter the answer to your question: ");
                        answer = input.getUserInput();
                        if ((answer.equalsIgnoreCase("Bye."))) {
                            connection.close();
                            return;
                        }
                        controller.putQuestion(question + "###" + answer, out);
                        input.incrementCounter();
                    }
                } else if (mode.equalsIgnoreCase("A")) {
                    int numQuestions = controller.getCounter();
                    int result;
                    while (!shouldInterrupt) {
                        out.println("Fetching question...");
                        question = controller.getQuestion(out);
                        numQuestions = controller.getCounter();
                        out.println("Question: " + question);
                        out.println("Enter your answer or 'Bye.' to quit: ");
                        answer = input.getUserInput();
                        if (answer.equalsIgnoreCase("Bye.")) {
                            connection.close();
                            return;
                        }
                        result = controller.answerQuestion(answer, numQuestions);
                        if (result == 1) {
                            out.println("Correct!");
                            input.incrementCounter();
                        } else if (result == -1) {
                            out.println("Incorrect!");
                            out.println("Two second penalty!");
                            Thread.sleep(2000);
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
            System.out.println(mode.toUpperCase() + ": " + name + " Disconnected.");
        }
    }

    private static class CloseServer implements Runnable {
        public void run() {
            Scanner sc = new Scanner(System.in);
            String input;
            while ((input = sc.nextLine()) != null) {
                if (input.equalsIgnoreCase("Bye.")) {
                    System.out.println("Server closing...");
                    CloseClient.closeAll();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Server closed.");
                    sc.close();
                    System.exit(0);
                }
            }
        }
    }

    private static class FromClient implements Runnable {
        private String currentInput = "-";
        private BufferedReader in;
        private ClientData data;
        private ClientHandler handler;
        private PrintWriter out;

        public FromClient(BufferedReader in, ClientData data, ClientHandler handler, PrintWriter out) {
            this.in = in;
            currentInput = "-";
            this.data = data;
            this.handler = handler;
            this.out = out;
        }

        public void run() {
            try {
                while ((currentInput = in.readLine()) != null) {
                    data.setCurrentInput(currentInput);
                    if (currentInput.equalsIgnoreCase("Bye.")) {
                        handler.shouldInterrupt();
                        data.shouldInterrupt();
                        if (data.getMode().equalsIgnoreCase("Q")) {
                            System.out.println("Questioner " + data.getName() + " disconnected. Questions asked: "
                                    + data.getCounter());
                            out.println("Questions asked: " + data.getCounter() + " Thanks for playing "
                                    + data.getName() + "!");
                        } else if (data.getMode().equalsIgnoreCase("A")) {
                            System.out.println("Answerer " + data.getName() + " disconnected." + " Final score: "
                                    + data.getCounter());
                            out.println("Final score: " + data.getCounter() + " Thanks for playing " + data.getName()
                                    + "!");
                        } else {
                            System.out.println("Client disconnected.");
                        }
                        in.close();
                        out.close();
                        return;
                    }
                }
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on the port or listening for a connection");
                System.out.println(e.getMessage());
            }
        }
    }

    private static class QAController {
        private String question;
        private int counter;
        private boolean empty = true;

        public synchronized void putQuestion(String Q, PrintWriter out) {
            if (!empty) {
                out.println("Waiting for question to be answered.");
            }
            while (!empty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                }
            }
            empty = false;
            question = Q;
            counter++;
            System.out.println("Question added: " + question);
            notifyAll();
        }

        public synchronized String getQuestion(PrintWriter out) {
            if (empty) {
                out.println("No questions available. Waiting...");
            }
            while (empty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                }
            }
            String[] returnQ = question.split("###");
            notifyAll();
            return returnQ[0];
        }

        public synchronized int answerQuestion(String answer, int num) {
            if (empty || num != counter) {
                return 0;
            }
            String[] questionArray = question.split("###");
            if (questionArray[1].equalsIgnoreCase(answer)) {
                empty = true;
                notifyAll();
                return 1;
            } else {
                return -1;
            }
        }

        public int getCounter() {
            return counter;
        }
    }

    private static class ClientData {
        private boolean shouldInterrupt = false;
        private String currentInput = "-";
        private int counter = 0;
        private String mode = "";
        private String name = "";

        public synchronized void shouldInterrupt() {
            shouldInterrupt = true;
        }

        public synchronized void setCurrentInput(String input) {
            currentInput = input;
            notifyAll();
        }

        public synchronized void resetCurrentInput() {
            currentInput = "-";
        }

        public String getUserInput() {
            if (shouldInterrupt) {
                return "Bye.";
            }
            resetCurrentInput();
            while (currentInput.equals("-")) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                }
            }
            return currentInput;
        }

        public void incrementCounter() {
            counter++;
        }

        public int getCounter() {
            return counter;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getMode() {
            return mode;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static class CloseClient implements Runnable {
        private static boolean closeClient = false;
        PrintWriter out;
        ClientData data;
        BufferedReader in;

        public CloseClient(PrintWriter out, ClientData data, BufferedReader in, ClientHandler handler) {
            this.out = out;
            this.data = data;
            this.in = in;
        }

        public void run() {
            while (!closeClient) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                }
            }
            System.out.println("Closing client connection...");
            out.println("Close");
            if (data.getMode().equalsIgnoreCase("Q")) {
                System.out.println("Questioner " + data.getName() + " disconnected. Questions asked: "
                        + data.getCounter());
                out.println("Questions asked: " + data.getCounter() + " Thanks for playing " + data.getName() + "!");
            } else if (data.getMode().equalsIgnoreCase("A")) {
                System.out.println("Answerer " + data.getName() + " disconnected." + " Final score: "
                        + data.getCounter());
                out.println("Final score: " + data.getCounter() + " Thanks for playing " + data.getName() + "!");
            } else {
                System.out.println("Client disconnected.");
            }
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                System.out.println("Exception caught when trying to close client connection.");
                System.out.println(e.getMessage());
            }
        }
        public static void closeAll() {
            closeClient = true;
        }
    }
}