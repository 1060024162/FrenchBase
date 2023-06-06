import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Quiz {
    private ArrayList<Question> questions;
    private int score;

    private String testSeries;
    private boolean submitted;
    private Connection connection;

    private List<String> userAnswers;

    private List<Boolean> results;
    private String id;


    public Quiz(String id) throws ClassNotFoundException, SQLException {
        questions = new ArrayList<>();
        userAnswers = new ArrayList<>(40);
        for (int i = 0; i < 40; i++){
            userAnswers.add("No Answer");
        }
        score = 0;
        this.id = id;
        Class.forName("org.sqlite.JDBC");
        String projectDir = System.getProperty("user.dir");
        String url = "jdbc:sqlite:" + projectDir + "/identifier.sqlite";
        this.connection = DriverManager.getConnection(url);
    }

    public void generateQuiz(int numOfQuestions, String testSeries) {
        setTestSeries(testSeries);
        Statement statement = null;
        try {
            statement = connection.createStatement();
            String query = "SELECT * FROM Questions WHERE `Test Series` = '" + testSeries + "' ORDER BY RANDOM() LIMIT " + numOfQuestions;
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String series = resultSet.getString("Test Series");
                int questionNumber = resultSet.getInt("Question Number");
                String question = resultSet.getString("Question");
                String a = resultSet.getString("A");
                String b = resultSet.getString("B");
                String c = resultSet.getString("C");
                String d = resultSet.getString("D");
                String answer = resultSet.getString("Answer");
                String explanation = resultSet.getString("Notes");
                String type = resultSet.getString("Type");

                Question q = new Question(series, questionNumber, question, a, b, c, d);
                q.setAnswer(answer);
                q.setExplanation(explanation);
                q.setType(type);

                questions.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        questions.sort((q1, q2) -> Integer.compare(q1.getQuestionNumber(), q2.getQuestionNumber()));
    }

    public void displayResults() {
        System.out.println("你的得分是：" + score + "/" + questions.size());

        JOptionPane.showMessageDialog(null, "Your score is：" + score + "/" + questions.size());
        for (Question question : questions) {
            System.out.println("Questions：" + question.getQuestion());
            System.out.println("Answers：" + question.getAnswer());
            System.out.println("Explanations：" + question.getExplanation());
            System.out.println();
        }
    }

    public void updateScore(String studentID, String testSeries, int score) throws Exception {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE Students SET \"" + testSeries + "\" = ? WHERE ID = ?");
            stmt.setInt(1, score);
            stmt.setString(2, studentID);
            stmt.executeUpdate();
            System.out.println("成功录入");
        } catch (SQLException e) {
            System.out.println("Error while updating score: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public int getScore() {
        return score;
    }

    public Connection getConnection() {
        return connection;
    }

    public void incrementScore() {
        score++;
    }

    public void recordAnswer(int questionIndex, String answer) {

        userAnswers.set(questionIndex, answer);
    }

    public String getUserAnswer(int questionIndex) {
        if (questionIndex < userAnswers.size()) {
            return userAnswers.get(questionIndex);
        } else {
            return null;
        }
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void submit() {
        this.submitted = true;
        results = new ArrayList<>(questions.size());
        for (int i = 0; i < questions.size(); i++) {
            String correctAnswer = questions.get(i).getAnswer();
            String userAnswer = userAnswers.get(i);
            results.add(correctAnswer.equals(userAnswer));
        }
    }

    public void setUserAnswer(int index, String selection) {
        userAnswers.set(index, selection);
    }

    public String getTestSeries() {
        return testSeries;
    }

    public void setTestSeries(String testSeries) {
        this.testSeries = testSeries;
    }

    public List<Boolean> getResults() {
        return results;
    }
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
