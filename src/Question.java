public class Question {

    private String series;
    private int questionNumber;
    private String question;
    private String a,b,c,d;
    private String answer = "";
    private String explanation = "";
    private String type = "";


    public Question(String series, int questionNumber, String question, String a, String b, String c, String d) {
        this.series = series;
        this.questionNumber = questionNumber;
        this.question = question;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public String getSeries() {
        return series;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public String getQuestion() {
        return question;
    }

    public String getA() {
        return a;
    }

    public String getB() {
        return b;
    }

    public String getC() {
        return c;
    }

    public String getD() {
        return d;
    }

    public String getAnswer() {
        return answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getType() {
        return type;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setA(String a) {
        this.a = a;
    }

    public void setB(String b) {
        this.b = b;
    }

    public void setC(String c) {
        this.c = c;
    }

    public void setD(String d) {
        this.d = d;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setType(String type) {
        this.type = type;
    }
}
