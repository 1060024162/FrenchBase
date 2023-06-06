import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class Students extends JFrame {


    public Students() throws SQLException, ClassNotFoundException{
        Class.forName("org.sqlite.JDBC");
        String projectDir = System.getProperty("user.dir");
        String url = "jdbc:sqlite:" + projectDir + "/identifier.sqlite";
        Connection connection = DriverManager.getConnection(url);

        setTitle("Students Table");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);


        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);
        table.setFont(new Font(table.getFont().getName(),table.getFont().getStyle(),12));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tableModel.addColumn("Name");
        tableModel.addColumn("ID");
        tableModel.addColumn("Test 1");
        tableModel.addColumn("Test 2");
        tableModel.addColumn("Test 3");
        tableModel.addColumn("Test 4");
        tableModel.addColumn("Test 5");
        tableModel.addColumn("Test 6");
        tableModel.addColumn("Test 7");
        tableModel.addColumn("Test 8");
        tableModel.addColumn("Test 9");
        tableModel.addColumn("Test 10");
        tableModel.addColumn("Test 11");
        tableModel.addColumn("Test 12");
        tableModel.addColumn("Test 13");
        tableModel.addColumn("Test 14");
        tableModel.addColumn("Test 15");
        tableModel.addColumn("Test 16");
        tableModel.addColumn("Test 17");
        tableModel.addColumn("Test 18");
        tableModel.addColumn("Test 19");
        tableModel.addColumn("Test 20");



        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        loadDataToTable(connection,tableModel);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        RowFilter<Object, Object> emptyValueFilter = new RowFilter<Object, Object>() {
            @Override
            public boolean include(Entry<? extends Object, ? extends Object> entry) {
                for (int i = 0; i < table.getColumnCount(); i++) {
                    Object value = entry.getValue(i);
                    if (value == null || value.toString().isEmpty()) {
                        return true;
                    }
                }
                return false;
            }
        };

        RowFilter<Object, Object> nonEmptyValueFilter = new RowFilter<Object, Object>() {
            @Override
            public boolean include(Entry<? extends Object, ? extends Object> entry) {
                for (int i = 0; i < table.getColumnCount(); i++) {
                    Object value = entry.getValue(i);
                    if (value == null || value.toString().isEmpty()) {
                        return false;
                    }
                }
                return true;
            }
        };

        sorter.setRowFilter(RowFilter.orFilter(Arrays.asList(emptyValueFilter, nonEmptyValueFilter)));


         /*
             实现搜索功能
        */
        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton);
        getContentPane().add(searchPanel, BorderLayout.NORTH);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText();
                if (searchText.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    // 在这里根据需要修改搜索逻辑
                    RowFilter<TableModel, Object> testSeriesFilter = RowFilter.regexFilter(searchText, 0);
                    RowFilter<TableModel, Object> questionNumberFilter = RowFilter.regexFilter(searchText, 1);
                    sorter.setRowFilter(RowFilter.orFilter(Arrays.asList(testSeriesFilter, questionNumberFilter)));
                }
            }
        });




        /*
             实现单行修改功能
        */
        JPanel buttonPanel = new JPanel();
        JButton modifyButton = new JButton("Modify Selected Row");
        buttonPanel.add(modifyButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    ModifyStudent modifyDialog = new ModifyStudent(table, selectedRow,connection);
                    modifyDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a row to modify.");
                }
            }

        });


         /*
             显示尚未完成编辑的题目
        */
        JButton addButton = new JButton(" Add row");
        buttonPanel.add(addButton);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddStudent addDialog = new AddStudent(table, connection);
                addDialog.setVisible(true);
            }
        });

        JButton refresh = new JButton("Refresh");
        buttonPanel.add(refresh);
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setRowCount(0);
                loadDataToTable(connection,tableModel);
            }
        });

        JButton deleteButton = new JButton("Delete rows");
        buttonPanel.add(deleteButton);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = table.getSelectedRows();
                if (selectedRows.length > 0) {
                    // Ask for confirmation before deleting the rows
                    int confirm = JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to delete these " + selectedRows.length + " rows?",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Sort the array in descending order to handle row indexes changing after deletion
                        Arrays.sort(selectedRows);
                        for (int i = selectedRows.length - 1; i >= 0; i--) {
                            try{
                                String deleteSQL = "DELETE FROM Students WHERE \"ID\" = ?";
                                PreparedStatement pstmt = connection.prepareStatement(deleteSQL);
                                pstmt.setString(1, table.getValueAt(selectedRows[i], 1).toString());
                                pstmt.executeUpdate();

                                // Remove row from the JTable
                                ((DefaultTableModel)table.getModel()).removeRow(selectedRows[i]);
                            } catch (SQLException ex) {
                                System.out.println("Error while deleting the data: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a row to delete.");
                }
            }
        });

        /*
        * 实现添加新列功能
        * */
        JPanel addColumnPanel = new JPanel();
        JTextField newColumnNameField = new JTextField(20);
        addColumnPanel.add(new JLabel("New column name:"));
        addColumnPanel.add(newColumnNameField);
        JButton addColumnButton = new JButton("Add column");
        addColumnPanel.add(addColumnButton);
        getContentPane().add(addColumnPanel, BorderLayout.NORTH);


        addColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newColumnName = newColumnNameField.getText();
                if (!newColumnName.isEmpty()) {
                    try {
                        // 更新你的数据库来添加新列
                        Statement stmt = connection.createStatement();
                        stmt.executeUpdate("ALTER TABLE Students ADD COLUMN `" + newColumnName + "` INT");

                        // 更新你的 DefaultTableModel 来添加新列
                        tableModel.addColumn(newColumnName);

                        //清空输入框
                        newColumnNameField.setText("");
                    } catch (SQLException ex) {
                        System.out.println("Error while adding new column: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });

    }
    public static void loadDataToTable(Connection conn, DefaultTableModel tableModel) {

        try {
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Students");

            while (resultSet.next()) {
                Object[] rowData = {
                        resultSet.getString("Name"),
                        resultSet.getInt("ID"),
                        resultSet.getString("Test 1"),
                        resultSet.getString("Test 2"),
                        resultSet.getString("Test 3"),
                        resultSet.getString("Test 4"),
                        resultSet.getString("Test 5"),
                        resultSet.getString("Test 6"),
                        resultSet.getString("Test 7"),
                        resultSet.getString("Test 8"),
                        resultSet.getString("Test 9"),
                        resultSet.getString("Test 10"),
                        resultSet.getString("Test 11"),
                        resultSet.getString("Test 12"),
                        resultSet.getString("Test 13"),
                        resultSet.getString("Test 14"),
                        resultSet.getString("Test 15"),
                        resultSet.getString("Test 16"),
                        resultSet.getString("Test 17"),
                        resultSet.getString("Test 18"),
                        resultSet.getString("Test 19"),
                        resultSet.getString("Test 20"),

                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            System.out.println("Error while loading data to table: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public class EmptyValueComparator implements Comparator<Object> {

        private JTable table;


        public EmptyValueComparator(JTable table) {
            this.table = table;
        }

        @Override
        public int compare(Object rowIndex1, Object rowIndex2) {
            boolean row1HasEmpty = hasEmptyValue((int)rowIndex1);
            boolean row2HasEmpty = hasEmptyValue((int)rowIndex2);

            if (row1HasEmpty && !row2HasEmpty) {
                return -1;
            } else if (!row1HasEmpty && row2HasEmpty) {
                return 1;
            } else {
                return 0;
            }
        }

        private boolean hasEmptyValue(int row) {
            for (int i = 0; i < table.getColumnCount(); i++) {
                Object value = table.getValueAt(row, i);
                if (value == null || value.toString().isEmpty()) {
                    return true;
                }
            }
            return false;
        }

    }
}
