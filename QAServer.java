import java.net.*;
import java.util.Scanner;
import java.io.*;

public class QAServer {
    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java QAServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        QAController controller = new QAController();
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            Thread close = new Thread(new closeServer());
            close.start();
            System.out.println("Server started on port " + portNumber + ".");
            System.out.println("Type 'Bye.' to close the server.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread handle = new Thread(new ClientHandler(clientSocket, controller));
                handle.start();
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        // The connection with the client
        private Socket connection;
        private QAController controller;

        public ClientHandler(Socket s, QAController controller) {
            connection = s;
            this.controller = controller;
        }

        public void run() {
            try {
                PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                System.out.println("Client Connected.");
                String inputLine, name, answer, question;
                int output;
                inputLine = in.readLine();
                System.out.println("Client: " + inputLine);
                if (inputLine.equals("Q")) {
                    name = in.readLine();
                    System.out.println("Client: " + name);
                    Question questioner = new Question(name);
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.equalsIgnoreCase("Bye.")) {
                            System.out.println(name + " Disconnected.");
                            System.out.println("Questions asked: " + questioner.getQuestionNumber());
                            out.println("Questions asked: " + questioner.getQuestionNumber() + "."
                                    + " Thank you for playing " + name + "!");
                            connection.close();
                            out.close();
                            in.close();
                            break;
                        }
                        questioner.addQuestion(inputLine);
                        controller.putQuestion(inputLine);
                        out.println("Done.");
                    }
                } else if (inputLine.equals("A")) {
                    int questionNumber = 1;
                    name = in.readLine();
                    System.out.println("Client: " + name);
                    Answer answerer = new Answer(name);
                    boolean answering = true;
                    while (answering) {
                        question = controller.getQuestion();
                        if (question == null) {
                            out.println("No questions available. Please wait for the next question.");
                        }
                        while ((question = controller.getQuestion()) == null) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                        }
                        while ((question = controller.getQuestion()) != null) {
                            System.out.println("Question: " + question);
                            out.println(question);
                            answer = in.readLine();
                            if (answer.equalsIgnoreCase("Bye.")) {
                                System.out.println(name + " Disconnected.");
                                System.out.println("Score: " + answerer.getPoints());
                                out.println("Final Score: " + answerer.getPoints() + ". Thank you for playing " + name
                                        + "!");
                                connection.close();
                                out.close();
                                in.close();
                                answering = false;
                                break;
                            }
                            output = controller.answerQuestion(answer, questionNumber);
                            if (output == 1) {
                                answerer.correctAnswer();
                                out.println("Won! Your score is " + answerer.getPoints() + ".");
                                questionNumber++;
                            } else if (output == 0) {
                                out.println("Faster next time! Your score is " + answerer.getPoints() + ".");
                                questionNumber++;
                            } else {
                                answerer.wrongAnswer();
                                out.println("Wrong! Your score is " + answerer.getPoints() + ".");
                                questionNumber++;
                            }
                        }
                    }
                } else {
                    // error
                }
                ;
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on the port or listening for a connection");
                System.out.println(e.getMessage());
            }

        }

    }

    private static class closeServer implements Runnable {
        public void run() {
            Scanner sc = new Scanner(System.in);
            String input;
            while ((input = sc.nextLine()) != null) {
                if (input.equalsIgnoreCase("Bye.")) {
                    System.out.println("Server closed.");
                    sc.close();
                    System.exit(0);
                }
            }
        }
    }
}
