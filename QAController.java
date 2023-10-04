import java.util.ArrayList;

public class QAController {
    private static ArrayList<String> questions = new ArrayList<String>();
    private static boolean questionChange = false;

    public synchronized void putQuestion(String question) {
        while (questionChange) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        questionChange = true;
        questions.add(question);
        System.out.println("Question added: " + question);
        questionChange = false;
        notifyAll();
    }

    public synchronized String getQuestion() {
        while (questions.size() == 0) {
            System.out.println("No questions available.");
            return null;
        }
        String[] question = questions.get(0).split("###");
        return question[0];
    }

    public synchronized int answerQuestion(String answer) {
        while (questionChange) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        questionChange = true;
        if (questions.size() == 0) {
            questionChange = false;
            notifyAll();
            return 0;
        }
        String[] questionArray = questions.get(0).split("###");
        System.out.println("Correct answer: " + questionArray[1]);
        if (questionArray[1].equalsIgnoreCase(answer)) {
            System.out.println("Client answer: " + answer);
            questions.remove(0);
            questionChange = false;
            notifyAll();
            return 1;
        } else {
            questionChange = false;
            notifyAll();
            return -1;
        }
    }
}
