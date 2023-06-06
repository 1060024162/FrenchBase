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

public class ModifyDialog extends JDialog{

    private JTable table;
    private int row;
    private Connection conn;
    private HashMap<String, JTextField> columnData;
    public ModifyDialog(JTable table, int selectedRow, Connection conn) {
        this.table = table;
        this.row = selectedRow;
        this.conn = conn;
        columnData = new HashMap<>();




        setTitle("Modify Row");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;


        for (int i = 0; i < table.getColumnCount(); i++) {
            JLabel label = new JLabel(table.getColumnName(i));
            // 设置标签的位置
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i;
            gridBagConstraints.weightx = 0.5;
            add(label, gridBagConstraints);

            Object cellValue = table.getValueAt(selectedRow, i);
            String cellText = cellValue == null ? "" : cellValue.toString();
            JTextField textField = new JTextField(cellText, 15);
            columnData.put(table.getColumnName(i), textField);
            // 设置文本字段的位置
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = i;
            gridBagConstraints.weightx = 0.5;
            add(textField, gridBagConstraints);
        }

        System.out.println("Selected row: " + row);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a SQL update statement with placeholders for values
                    StringBuilder updateSQL = new StringBuilder("UPDATE Questions SET ");
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        if (i != 0) {
                            updateSQL.append(", ");
                        }
                        updateSQL.append("\"").append(table.getColumnName(i)).append("\" = ?");
                    }
                    updateSQL.append(" WHERE \"Question Number\" = ? AND \"Test Series\" = ?");

                    System.out.println("Executing SQL: " + updateSQL.toString()); // 输出 SQL 语句


                    PreparedStatement pstmt = conn.prepareStatement(updateSQL.toString());

                    // Set the values in the PreparedStatement
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        JTextField textField = columnData.get(table.getColumnName(i));
                        pstmt.setString(i + 1, textField.getText());
                    }
                    // Set the value for the WHERE clause (Question Number)
                    pstmt.setInt(table.getColumnCount() + 1, Integer.parseInt(table.getValueAt(row, 1).toString()));
                    pstmt.setString(table.getColumnCount() + 2, table.getValueAt(row, 0).toString());
                    pstmt.executeUpdate();

                    // Update the JTable
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        JTextField textField = columnData.get(table.getColumnName(i));
                        table.setValueAt(textField.getText(), row, i);
                    }
                    // Repaint the table
                    table.repaint();

                    // Close the dialog
                    dispose();
                } catch (SQLException ex) {
                    System.out.println("Error while updating the data: " + ex.getMessage());
                    ex.printStackTrace();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        add(updateButton);
    }
}
