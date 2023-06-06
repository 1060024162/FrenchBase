import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
public class QuizGUI extends JFrame implements ActionListener {

    // 类的成员变量
    private Quiz quiz;

    private String id;
    private int currentQuestionIndex;

    private JButton nextButton, submitButton,previousButton;
    private JLabel questionLabel;
    private JRadioButton optionA, optionB, optionC, optionD;
    private ButtonGroup optionsGroup;

    // 构造方法
    public QuizGUI(Quiz quiz,String id) {
        this.quiz = quiz;
        this.id = id;

        initComponents();
        Question first = quiz.getQuestions().get(0);
        questionLabel.setText(first.getQuestionNumber() + first.getQuestion());
        optionA.setText(first.getA());
        optionB.setText(first.getB());
        optionC.setText(first.getC());
        optionD.setText(first.getD());
    }

    private void initComponents() {
        setTitle("Quiz");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        questionLabel = new JLabel();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        add(questionLabel, c);

        optionA = new JRadioButton();
        optionB = new JRadioButton();
        optionC = new JRadioButton();
        optionD = new JRadioButton();
        optionsGroup = new ButtonGroup();
        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        c.gridwidth = 1;
        c.gridy = 1;
        add(optionA, c);
        c.gridy = 2;
        add(optionB, c);
        c.gridy = 3;
        add(optionC, c);
        c.gridy = 4;
        add(optionD, c);
        previousButton = new JButton("Previous");
        previousButton.addActionListener(this);
        c.gridx = 1;
        c.gridy = 5;
        add(previousButton, c);


        nextButton = new JButton("Next");
        nextButton.addActionListener(this);
        c.gridx = 1;
        c.gridy = 6;
        add(nextButton, c);

        submitButton = new JButton("Submit");
        submitButton.addActionListener(this);
        c.gridy = 7;
        add(submitButton, c);

        currentQuestionIndex = 0;
        displayQuestion(currentQuestionIndex);
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == nextButton) {
            checkAnswer();
            if (currentQuestionIndex < quiz.getQuestions().size()){
                currentQuestionIndex++;
            }else if (currentQuestionIndex == quiz.getQuestions().size()){
                JOptionPane.showMessageDialog(this, "This is the last question, click the \"Submit\" button to end the quiz.", "Last question", JOptionPane.INFORMATION_MESSAGE);
            }
            displayQuestion(currentQuestionIndex);

        } else if (e.getSource() == submitButton) {
            checkAnswer();
            quiz.submit();
            showResults(quiz.getResults());
            quiz.displayResults();
            try {
                quiz.updateScore(id, quiz.getTestSeries(), quiz.getScore());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }finally {
                quiz.close();
            }
            displayQuestion(currentQuestionIndex);
            /*System.exit(0);*/
        }else if (e.getSource() == previousButton){

            checkAnswer();
            if (currentQuestionIndex > 0){
                currentQuestionIndex--;
            }else if (currentQuestionIndex == 0){
                JOptionPane.showMessageDialog(this, "There is no problem with the front.", "First question", JOptionPane.INFORMATION_MESSAGE);
            }
            displayQuestion(currentQuestionIndex);
        }
    }




    private void displayQuestion(int index) {
        Question question = quiz.getQuestions().get(index);
        questionLabel.setText(question.getQuestionNumber() +" "+ question.getQuestion());
        optionA.setText("A) " + question.getA());
        optionB.setText("B) " + question.getB());
        optionC.setText("C) " + question.getC());
        optionD.setText("D) " + question.getD());

        // get user answer and select the corresponding button
        String userAnswer = quiz.getUserAnswer(index);
        optionA.setSelected("A".equals(userAnswer));
        optionB.setSelected("B".equals(userAnswer));
        optionC.setSelected("C".equals(userAnswer));
        optionD.setSelected("D".equals(userAnswer));

        // if test is submitted, change color of options
        if (quiz.isSubmitted()) {
            String correctAnswer = question.getAnswer();
            optionA.setForeground("A".equals(correctAnswer) ? Color.GREEN : "A".equals(userAnswer) ? Color.RED : Color.BLACK);
            optionB.setForeground("B".equals(correctAnswer) ? Color.GREEN : "B".equals(userAnswer) ? Color.RED : Color.BLACK);
            optionC.setForeground("C".equals(correctAnswer) ? Color.GREEN : "C".equals(userAnswer) ? Color.RED : Color.BLACK);
            optionD.setForeground("D".equals(correctAnswer) ? Color.GREEN : "D".equals(userAnswer) ? Color.RED : Color.BLACK);

            optionA.setEnabled(false);
            optionB.setEnabled(false);
            optionC.setEnabled(false);
            optionD.setEnabled(false);
        } else {
            optionA.setEnabled(true);
            optionB.setEnabled(true);
            optionC.setEnabled(true);
            optionD.setEnabled(true);
        }

        optionsGroup.clearSelection();
    }

    private void checkAnswer() {
        String selectedOption = null;
        if (optionA.isSelected()) {
            selectedOption = "A";
        } else if (optionB.isSelected()) {
            selectedOption = "B";
        } else if (optionC.isSelected()) {
            selectedOption = "C";
        } else if (optionD.isSelected()) {
            selectedOption = "D";
        }
        if (selectedOption != null) {
            quiz.recordAnswer(currentQuestionIndex, selectedOption);
            if (currentQuestionIndex < quiz.getQuestions().size()){
                Question currentQuestion = quiz.getQuestions().get(currentQuestionIndex);
                if (selectedOption.equals(currentQuestion.getAnswer())) {
                    quiz.incrementScore();
                }
            }

        }

    }
    public void showResults(List<Boolean> results) {
        JFrame resultFrame = new JFrame("Quiz Results");
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columnNames = {"Question Number", "Correct", "Your Answer", "Correct Answer"};
        Object[][] data = new Object[results.size()][4];
        for (int i = 0; i < results.size(); i++) {
            data[i][0] = i + 1; // question number
            data[i][1] = results.get(i) ? "✔" : "✘";
            data[i][2] = quiz.getUserAnswer(i);
            data[i][3] = quiz.getQuestions().get(i).getAnswer();
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        resultFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        resultFrame.pack();
        resultFrame.setVisible(true);
    }
}