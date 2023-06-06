import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileChooserApp extends JFrame {

    public FileChooserApp(int n) throws SQLException, ClassNotFoundException {

        Class.forName("org.sqlite.JDBC");
        String projectDir = System.getProperty("user.dir");
        String url = "jdbc:sqlite:" + projectDir + "/identifier.sqlite";
        Connection connection = DriverManager.getConnection(url);

        setTitle("File Chooser App");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Drag and drop or select a file"));

        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton browse = new JButton("Browse");
        panel.add(browse, BorderLayout.SOUTH);

        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
                int returnValue = fileChooser.showOpenDialog(FileChooserApp.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        if (n == 0){
                            processFile(selectedFile, textArea,connection);
                        }else {
                            processAnswerFile(selectedFile,textArea,connection);
                        }

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        new FileDrop(textArea, new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] files) throws IOException {
               if (n == 0){
                   processFile(files[0], textArea,connection);
               }else {
                    processAnswerFile(files[0],textArea,connection);
               }

            }
        });

        add(panel);
    }

    private static void processFile(File file, JTextArea textArea, Connection conn) throws IOException {
        textArea.setText("Selected file: " + file.getAbsolutePath());

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            Pattern testPattern = Pattern.compile("(?i)test\\s+(\\d+)");
            Matcher testMatcher = testPattern.matcher(line);

            if (testMatcher.find()) {
                String series = "Test " + testMatcher.group(1);
                System.out.println("Series: " + line.trim());
                lines.add(series);
            } else {
                lines.add(line);
            }
        }
        textArea.setText(parseQuestions(lines,conn));
    }


    private static String parseQuestions(List<String> lines,Connection conn){

        Pattern questionPattern = Pattern.compile("(\\d+),\\s*(.+)");
        Pattern optionPattern = Pattern.compile("([A-D])\\)\\s+(.+?)(?=\\s+[A-D]\\)|$)");

        String currentQuestion = "";
        StringBuilder questionBuilder = new StringBuilder();
        String test = "Test 1",message = "";
        Map<String, String> optionsMap = new TreeMap<>();

        for (String line : lines) {

            if (line.trim().toLowerCase().startsWith("test")){
                test = line;
            }else {
                Matcher questionMatcher = questionPattern.matcher(line);
                if (questionMatcher.find()) {
                    if (!currentQuestion.isEmpty()) {
                        System.out.println("Question: " + currentQuestion);
                        optionsMap.forEach((key, value) -> System.out.println(key + ") " + value));
                        if (optionsMap.size() == 4){
                            String[] questionState = currentQuestion.split(",");
                            Question question = new Question(test,Integer.parseInt(questionState[0]),questionState[1],
                                    optionsMap.get("A"),
                                    optionsMap.get("B"),
                                    optionsMap.get("C"),
                                    optionsMap.get("D")
                            );
                            message = insertQuestion(conn,question);
                            if (message.startsWith("Error")){
                                return currentQuestion + message;
                            }
                            optionsMap.clear();
                        }
                    }
                    currentQuestion = questionMatcher.group(1) + ", " + questionMatcher.group(2);
                    questionBuilder.setLength(0);
                    questionBuilder.append(questionMatcher.group(2));
                } else {
                    questionBuilder.append(" ").append(line);
                    Matcher optionMatcher = optionPattern.matcher(line);
                    while (optionMatcher.find()) {
                        optionsMap.put(optionMatcher.group(1), optionMatcher.group(2));
                    }
                }
            }
        }

        // 输出最后一个问题及其选项
        if (!currentQuestion.isEmpty()) {
            System.out.println("Question: " + currentQuestion);
            optionsMap.forEach((key, value) -> System.out.println(key + ") " + value));
        }
        return message;
    }

    private static String insertQuestion(Connection conn, Question question) {
        if (!isQuestionExists(conn,question)){
            String sql = "INSERT INTO Questions (\"Test Series\", \"Question Number\", Question, A, B, C, D, Notes,Type,Answer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, question.getSeries());
                pstmt.setInt(2, question.getQuestionNumber());
                pstmt.setString(3, question.getQuestion());
                pstmt.setString(4, question.getA());
                pstmt.setString(5, question.getB());
                pstmt.setString(6, question.getC());
                pstmt.setString(7, question.getD());
                pstmt.setString(8, null); // answer
                pstmt.setString(9, null); // Explanation
                pstmt.setString(10, null); // type
                pstmt.executeUpdate();
                return "Successful";
            } catch (SQLException e) {
                System.out.println("Error while inserting the data: " + e.getMessage());
                e.printStackTrace();
                return "Error while inserting the data: " + e.getMessage();
            }
        }else {
            return "Repeated question";
        }
    }

    private static boolean isQuestionExists(Connection conn, Question question) {
        String sql = "SELECT COUNT(*) FROM Questions WHERE \"Test Series\" = ? AND \"Question Number\" = ? AND Question = ? AND A = ? AND B = ? AND C = ? AND D = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, question.getSeries());
            pstmt.setInt(2, question.getQuestionNumber());
            pstmt.setString(3, question.getQuestion());
            pstmt.setString(4, question.getA());
            pstmt.setString(5, question.getB());
            pstmt.setString(6, question.getC());
            pstmt.setString(7, question.getD());

            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error while checking for duplicate data: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static void processAnswerFile(File file, JTextArea textArea, Connection conn) throws IOException {
        textArea.setText("Selected file: " + file.getAbsolutePath());

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            Pattern testPattern = Pattern.compile("(?i)test\\s+(\\d+)");
            Matcher testMatcher = testPattern.matcher(line);

            if (testMatcher.find()) {
                String series = "Test " + testMatcher.group(1);
                System.out.println("Series: " + line.trim());
                lines.add(series);
            } else {
                lines.add(line);
            }
        }
        textArea.setText(parseAnswers(lines,conn));
    }

    private static String parseAnswers(List<String> lines,Connection conn){

        Pattern answerPattern = Pattern.compile("(\\d+),([A-D])\\.(.+)");

        String currentTest = "Test 1", message = "";
        for (String line : lines) {

            if (line.trim().toLowerCase().startsWith("test")){
                currentTest = line;
            }else {
                Matcher answerMatcher = answerPattern.matcher(line);
                if (answerMatcher.find()) {
                    int questionNumber = Integer.parseInt(answerMatcher.group(1));
                    String answer = answerMatcher.group(2);
                    String notes = answerMatcher.group(3);
                    message = updateAnswer(conn, currentTest, questionNumber, answer, notes);
                    if (message.startsWith("Error")){
                        return "Test Series: " + currentTest + ", Question Number: " + questionNumber + ", " + message;
                    }
                }
            }
        }
        return message;
    }

    private static String updateAnswer(Connection conn, String testSeries, int questionNumber, String answer, String notes) {
        String sql = "UPDATE Questions SET Answer = ?, Notes = ? WHERE \"Test Series\" = ? AND \"Question Number\" = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, answer);
            pstmt.setString(2, notes);
            pstmt.setString(3, testSeries);
            pstmt.setInt(4, questionNumber);
            pstmt.executeUpdate();
            return "Successful";
        } catch (SQLException e) {
            System.out.println("Error while updating the data: " + e.getMessage());
            e.printStackTrace();
            return "Error while updating the data: " + e.getMessage();
        }
    }
}