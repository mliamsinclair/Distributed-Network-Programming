
public class QAController {
    private static String question = null;
    private static int counter = 0;

    public synchronized void putQuestion(String Q) {
        while (question != null) {
            try {
                System.out.println("Waiting for question to be answered.");
                wait();
            } catch (InterruptedException e) {
            }
        }
        question = Q;
        counter++;
        System.out.println("Question added: " + question);
        notifyAll();
    }

    public synchronized String getQuestion() {
        if (question == null) {
            try {
                wait(25);
            } catch (InterruptedException e) {
            }
            return null;
        }
        String[] returnQ = question.split("###");
        return returnQ[0];
    }

    public synchronized int answerQuestion(String answer, int num) {
        if (question == null) {
            notifyAll();
            return 0;
        }
        if (num != counter) {
            notifyAll();
            return 0;
        }
        String[] questionArray = question.split("###");
        if (questionArray[1].equalsIgnoreCase(answer)) {
            question = null;
            notifyAll();
            return 1;
        } else {
            notifyAll();
            return -1;
        }
    }
}
