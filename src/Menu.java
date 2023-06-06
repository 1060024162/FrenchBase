import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Menu {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("Main App");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel(new BorderLayout());
            frame.add(panel);

            // Create a panel for buttons
            JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
            panel.add(buttonPanel, BorderLayout.CENTER);



            JButton button2 = new JButton("Quiz");
            button2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String id = JOptionPane.showInputDialog(null, "Please enter your student number");
                    try {
                        getStudentID(id);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    if(id != null){
                        String testSeries = JOptionPane.showInputDialog(null, "Please enter the Test Series you want to take(Capitalization at the beginning)");
                        if (testSeries != null && !testSeries.trim().isEmpty()) {
                            try {
                                startQuiz(testSeries,id);
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            } catch (ClassNotFoundException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
            });

            JButton button3 = new JButton("Questions");
            button3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    DataBaseView dataBaseView = null;
                    try {
                        dataBaseView = new DataBaseView();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    dataBaseView.setVisible(true);
                }

        });

            JButton fileChooserButton = new JButton("Open File Chooser");
            fileChooserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Ask user if they want to add questions or answers
                    JOptionPane.showMessageDialog(null,"Note:Only UTF-8 encoded .txt files are supported");
                    Object[] options = {"Questions", "Answers"};
                    int n = JOptionPane.showOptionDialog(null,
                            "Would you like to import questions or answers?",
                            "Choose an Option",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);

                    // Open the File Chooser App
                    if (n != -1){
                        FileChooserApp fileChooserApp = null;
                        try {
                            fileChooserApp = new FileChooserApp(n);
                            System.out.println(n);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                        fileChooserApp.setVisible(true);
                    }

                }
            });
            JButton buttonStudents = new JButton("Students info");
            buttonStudents.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    Students students = null;
                    try {
                        students = new Students();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    students.setVisible(true);
                }
            });

            buttonPanel.add(buttonStudents);
            buttonPanel.add(button2);
            buttonPanel.add(button3);
            buttonPanel.add(fileChooserButton);

            frame.setVisible(true);
        });
    }


    private static void startQuiz(String testSeries,String id) throws SQLException, ClassNotFoundException {
        Quiz quiz = new Quiz(id);
        quiz.generateQuiz(40, testSeries);

        if (!quiz.getQuestions().isEmpty()) {
            QuizGUI quizGUI = new QuizGUI(quiz,id);
            quizGUI.setVisible(true);
        } else {
            // 可以在此处显示一条消息，告知用户问题列表为空
            JOptionPane.showMessageDialog(null, "No questions matching the selected Test Series were found, please try another Test Series.", "Questions not found", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static String getStudentID(String id) throws Exception {
        Class.forName("org.sqlite.JDBC");
        String projectDir = System.getProperty("user.dir");
        String url = "jdbc:sqlite:" + projectDir + "/identifier.sqlite";
        Connection connection = DriverManager.getConnection(url);
        if (id != null){
            try {
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Students WHERE ID = ?");
                stmt.setString(1, id);
                ResultSet resultSet = stmt.executeQuery();
                if (!resultSet.next()) {  // 如果没有找到学生记录
                    stmt = connection.prepareStatement("INSERT INTO Students (Name, ID) VALUES (?, ?)");
                    String studentName = JOptionPane.showInputDialog("Please enter your name");
                    stmt.setString(1, studentName);
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println("Error while checking student ID: " + e.getMessage());
                e.printStackTrace();
            }finally {
                if (connection != null){
                    connection.close();
                }
            }
            return id;
        }else {
            return null;
        }
    }



}
