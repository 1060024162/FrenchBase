import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class AddDialog extends JDialog {

    private JTable table;
    private Connection conn;
    private HashMap<String, JTextField> columnData;

    public AddDialog(JTable table, Connection conn) {
        this.table = table;
        this.conn = conn;
        columnData = new HashMap<>();

        setTitle("Add Row");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < table.getColumnCount(); i++) {
            JLabel label = new JLabel(table.getColumnName(i));
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i;
            gridBagConstraints.weightx = 0.5;
            add(label, gridBagConstraints);

            JTextField textField = new JTextField(15);
            columnData.put(table.getColumnName(i), textField);

            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = i;
            gridBagConstraints.weightx = 0.5;
            add(textField, gridBagConstraints);
        }

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    StringBuilder insertSQL = new StringBuilder("INSERT INTO Questions (");
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        if (i != 0) {
                            insertSQL.append(", ");
                        }
                        insertSQL.append("\"").append(table.getColumnName(i)).append("\"");
                    }
                    insertSQL.append(") VALUES (");
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        if (i != 0) {
                            insertSQL.append(", ");
                        }
                        insertSQL.append("?");
                    }
                    insertSQL.append(")");
                    System.out.println(insertSQL.toString());
                    PreparedStatement pstmt = conn.prepareStatement(insertSQL.toString());

                    // Set the values in the PreparedStatement
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        JTextField textField = columnData.get(table.getColumnName(i));
                        pstmt.setString(i + 1, textField.getText());
                    }

                    pstmt.executeUpdate();

                    // Close the dialog
                    dispose();
                } catch (SQLException ex) {
                    System.out.println("Error while inserting the data: " + ex.getMessage());
                    ex.printStackTrace();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        add(addButton);
    }
}

