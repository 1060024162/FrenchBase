import jdk.internal.access.JavaIOFileDescriptorAccess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class ModifyStudent extends JDialog{
    private JTextField nameField, idField;
    private Connection conn;
    private int selectedRow;
    private JTable table;

    public ModifyStudent(JTable table, int selectedRow, Connection conn) {
        this.table = table;
        this.selectedRow = selectedRow;
        this.conn = conn;

        setTitle("Modify Student");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Name");
        c.gridx = 0;
        c.gridy = 0;
        add(nameLabel, c);

        nameField = new JTextField(table.getValueAt(selectedRow, 0).toString(), 15);
        c.gridx = 1;
        add(nameField, c);

        JLabel idLabel = new JLabel("ID");
        c.gridy = 1;
        add(idLabel, c);

        idField = new JTextField(table.getValueAt(selectedRow, 1).toString(), 15);
        c.gridx = 1;
        add(idField, c);

        JButton modifyButton = new JButton("Modify");
        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String sql = "UPDATE Students SET Name = ?, ID = ? WHERE Name = ? AND ID = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, nameField.getText());
                    pstmt.setString(2, idField.getText());
                    pstmt.setString(3, table.getValueAt(selectedRow, 0).toString());
                    pstmt.setString(4, table.getValueAt(selectedRow, 1).toString());
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(ModifyStudent.this, "Student modified successfully");
                    dispose();
                } catch (SQLException ex) {
                    System.out.println("Error while modifying student: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        c.gridy = 2;
        add(modifyButton, c);
    }
}

