import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class AddStudent extends JDialog {

    private JTable table;
    private Connection conn;
    private HashMap<String, JTextField> columnData;

    public AddStudent(JTable table, Connection conn) {
        this.conn = conn;

        setTitle("Add Student");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Name");
        c.gridx = 0;
        c.gridy = 0;
        add(nameLabel, c);

        JTextField nameField = new JTextField(15);
        c.gridx = 1;
        add(nameField, c);

        JLabel idLabel = new JLabel("ID");
        c.gridx = 0;
        c.gridy = 1;
        add(idLabel, c);

        JTextField idField = new JTextField(15);
        c.gridy = 1;
        c.gridx = 1;
        add(idField, c);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String sql = "INSERT INTO Students (Name, ID) VALUES (?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, nameField.getText());
                    pstmt.setString(2, idField.getText());
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(AddStudent.this, "Student added successfully");
                    dispose();
                } catch (SQLException ex) {
                    System.out.println("Error while adding student: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        c.gridy = 2;
        add(addButton, c);
    }
}

