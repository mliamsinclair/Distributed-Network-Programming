import java.io.*;
import java.net.*;

public class QAClient {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java QAClient <host name> <port number>");
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
            String userInput, question, response;
            System.out.print("Enter 'Q' if you are a Questioner or 'A' if you are an Answerer: ");
            userInput = stdIn.readLine();
            while (userInput.equalsIgnoreCase("Q") == false && userInput.equalsIgnoreCase("A") == false) {
                System.out.println("Invalid input.");
                System.out.print("Enter 'Q' if you are a Questioner or 'A' if you are an Answerer: ");
                userInput = stdIn.readLine();
            }
            out.println(userInput.toUpperCase());
            System.out.print("Enter your name: ");
            out.println(stdIn.readLine());

            // Questioner
            if (userInput.equalsIgnoreCase("Q")) {
                System.out.println("Enter a question or 'Quit.' to quit: ");
                while ((userInput = stdIn.readLine()) != null) {
                    if (userInput.equalsIgnoreCase("Quit.")) {
                        out.println(userInput);
                        echoSocket.close();
                        out.close();
                        in.close();
                        break;
                    }
                    question = userInput;
                    System.out.println("Enter the answer to your question: ");
                    userInput = stdIn.readLine();
                    out.println(question + "###" + userInput);
                    System.out.println(in.readLine());
                    System.out.println("Enter a question or 'Quit.' to quit: ");
                }

                // Answerer
            } else if (userInput.equalsIgnoreCase("A")) {
                System.out.println("Welcome! You may respond to questions as they come in, or enter 'Quit.' to quit.");
                System.out.println("Fetching question...");
                while ((question = in.readLine()) != null) {
                    if (question.equalsIgnoreCase("No questions available. Please wait for the next question.")) {
                        System.out.println(question);
                        System.out.println("Fetching question...");
                    } else {
                        System.out.println(question);
                        System.out.print("Enter your answer or enter 'Quit.' to quit: ");
                        userInput = stdIn.readLine();
                        if (userInput.equalsIgnoreCase("Quit.")) {
                            out.println(userInput);
                            echoSocket.close();
                            out.close();
                            in.close();
                            break;
                        }
                        out.println(userInput);
                        response = in.readLine();
                        System.out.println(response);
                        if (response.equalsIgnoreCase("Wrong!")) {
                            try {
                                System.out.println("Two second penalty!");
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                            }
                        }
                        System.out.println("Fetching question...");
                    }
                }
            } else {
                System.out.println("Invalid input.");
                System.exit(1);
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
}
