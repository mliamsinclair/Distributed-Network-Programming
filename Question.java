import java.util.ArrayList;

public class Question {
    String name;
    ArrayList<String> questions = new ArrayList<String>();
    int questionNumber = 0;
    public Question (String name) {
        this.name = name;
    }
    public void addQuestion (String question) {
        questions.add(question);
        questionNumber++;
    }
    public String getQuestion () {
        return questions.get(questionNumber);
    }
    public int getQuestionNumber () {
        return questionNumber;
    }
    public String getName () {
        return name;
    }
    public String getQuestionByNumber(int number) {
        return questions.get(number);
    }
}
