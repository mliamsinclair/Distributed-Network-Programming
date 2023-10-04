public class Answer {
    private int points = 0;
    private int questionNumber = 0;
    String name;
    public Answer (String name) {
        this.name = name;
    }
    public void correctAnswer () {
        points++;
        questionNumber++;
    }
    public void wrongAnswer () {
        questionNumber++;
    }
    public int getPoints () {
        return points;
    }
    public void setQuestionNumber (int questionNumber) {
        this.questionNumber = questionNumber;
    }
    public int getQuestionNumber () {
        return questionNumber;
    }
    public String getName () {
        return name;
    }
}
