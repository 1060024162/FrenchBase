import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.*;
import java.util.List;

public class DataBaseView extends JFrame {


    public DataBaseView() throws SQLException, ClassNotFoundException{
        Class.forName("org.sqlite.JDBC");
        String projectDir = System.getProperty("user.dir");
        String url = "jdbc:sqlite:" + projectDir + "/identifier.sqlite";
        Connection connection = DriverManager.getConnection(url);

        setTitle("Questions Table");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);


        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);
        table.setFont(new Font(table.getFont().getName(),table.getFont().getStyle(),12));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tableModel.addColumn("Test Series");
        tableModel.addColumn("Question Number");
        tableModel.addColumn("Question");
        tableModel.addColumn("A");
        tableModel.addColumn("B");
        tableModel.addColumn("C");
        tableModel.addColumn("D");
        tableModel.addColumn("Answer");
        tableModel.addColumn("Type");
        tableModel.addColumn("Notes");



        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        loadDataToTable(connection,tableModel);


        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        sorter.toggleSortOrder(1);

        Comparator<Integer> questionNumberComparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        };

        sorter.setComparator(1, questionNumberComparator);
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
                    // 创建一个RowFilter列表
                    List<RowFilter<TableModel, Object>> filters = new ArrayList<>();

                    // 添加Test series过滤器
                    RowFilter<TableModel, Object> testSeriesFilter = RowFilter.regexFilter(searchText, 0);
                    filters.add(testSeriesFilter);

                    // 添加Question Number过滤器
                    RowFilter<TableModel, Object> questionNumberFilter = RowFilter.regexFilter(searchText, 1);
                    filters.add(questionNumberFilter);

                    // 添加Type过滤器
                    RowFilter<TableModel, Object> typeFilter = RowFilter.regexFilter(searchText, 8);
                    filters.add(typeFilter);

                    // 将所有的过滤器使用RowFilter.orFilter组合起来
                    sorter.setRowFilter(RowFilter.orFilter(filters));
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
                            ModifyDialog modifyDialog = new ModifyDialog(table, selectedRow,connection);
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
                AddDialog addDialog = new AddDialog(table, connection);
                addDialog.setVisible(true);
            }
        });


          /*
             刷新列表
        */


        JButton refresh = new JButton("Refresh");
        buttonPanel.add(refresh);
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setRowCount(0);
                loadDataToTable(connection,tableModel);
            }
        });

          /*
             删除列表
        */

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
                                String deleteSQL = "DELETE FROM Questions WHERE \"Question Number\" = ? AND \"Test Series\" = ? AND \"Answer\" IS NULL ;";
                                PreparedStatement pstmt = connection.prepareStatement(deleteSQL);
                                pstmt.setInt(1, Integer.parseInt(table.getValueAt(selectedRows[i], 1).toString()));
                                pstmt.setString(2, table.getValueAt(selectedRows[i], 0).toString());
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


        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

    }
    public static void loadDataToTable(Connection conn, DefaultTableModel tableModel) {

        try {
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Questions");

            while (resultSet.next()) {
                Object[] rowData = {
                        resultSet.getString("Test Series"),
                        resultSet.getInt("Question Number"),
                        resultSet.getString("Question"),
                        resultSet.getString("A"),
                        resultSet.getString("B"),
                        resultSet.getString("C"),
                        resultSet.getString("D"),
                        resultSet.getString("Answer"),
                        resultSet.getString("Type"),
                        resultSet.getString("Notes"),


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
