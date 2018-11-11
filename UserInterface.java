package worklist;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserInterface implements Runnable {

    private JTable Worklist;
    private JTable Tasklist;
    private JTabbedPane tabPane;

    @Override
    public void run() {
        JFrame frame = new JFrame("place holder");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        frame.add(tabPane());
        frame.add(createButtons());

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JTabbedPane tabPane() {
        tabPane = new JTabbedPane();
        Worklist = createTable("Workplace", 0, true);
        tabPane.addTab("Workplace", new JScrollPane(Worklist));
        Tasklist = createTable("Tasklist", 1, true);
        tabPane.addTab("Tasklist", new JScrollPane(Tasklist));
        tabPane.setEnabledAt(1, false);
        return tabPane;
    }

    private JTable createTable(String inputTable, int ID, boolean editable) {
        DefaultTableModel tableModel = populateTableModel(inputTable, ID);
        tableModel.addTableModelListener(new MyTableModelListener(inputTable));
        JTable table = new JTable(tableModel);
        table.setEnabled(editable);
        if (ID == 0) {
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    JTable table = (JTable) mouseEvent.getSource();
                    Point point = mouseEvent.getPoint();
                    if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                        int row = table.rowAtPoint(point);
                        int rowID = (int) Worklist.getValueAt(row, 0);
                        tabPane.setEnabledAt(1, true);
                        Tasklist = createTable("Tasklist", rowID, true);
                        tabPane.setComponentAt(1, Tasklist);
                        tabPane.setSelectedIndex(1);
                    }
                }

            });
        }
        // Hide the ID column
        //table.removeColumn(table.getColumnModel().getColumn(0));
        table.getColumnModel().getColumn(0).setMaxWidth(25);
        table.setPreferredScrollableViewportSize(new Dimension(700, 500));
        table.setFillsViewportHeight(true);
        return table;
    }

    private DefaultTableModel populateTableModel(String inputTable, int ID) {
        String sqlQuery;
        if (ID > 0) {
            sqlQuery = "SELECT * FROM "
                    .concat(inputTable)
                    .concat(" WHERE ID = ")
                    .concat(ID + "");
        } else {
            sqlQuery = "SELECT * FROM ".concat(inputTable);
        }
        Object[] colNames = SqlConnection.sqlGetColumnNames(sqlQuery);
        Object[][] tableData = SqlConnection.sqlGetTableData(sqlQuery);
        DefaultTableModel tableModel = new DefaultTableModel(tableData, colNames);
        return tableModel;
    }

    private void refreshTable(JTable table) {
        table.setModel(populateTableModel(tabPane.getTitleAt(tabPane.getSelectedIndex()), 0));
        table.getColumnModel().getColumn(0).setMaxWidth(25);
    }

    private String currentTab() {
        return tabPane.getTitleAt(tabPane.getSelectedIndex());
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(0, 40));

        JButton addButton = new JButton("Add Field");
        addButton.addActionListener((ActionEvent e) -> {
            String inputName = JOptionPane.showInputDialog("Name of new field:");
            if (!inputName.isEmpty()) {
                if (currentTab().contains("Workplace")) {
                    int update = SqlConnection.sqlExecuteUpdate("INSERT INTO Workplace VALUES (null, '" + inputName.trim() + "', 0)");
                    System.out.println("Query result: " + update);
                }
                if (currentTab().contains("Tasklist")) {
                    int ID = (int) Tasklist.getValueAt(0, 0);
                    int update = SqlConnection.sqlExecuteUpdate("INSERT INTO Tasklist VALUES (" + ID + ", '" + inputName.trim() + "', 0)");
                    System.out.println("Query result: " + update);
                }

                refreshTable(returnFocusedTable());
            }
        });

        JButton deleteButton = new JButton("Delete Row");
        deleteButton.addActionListener((ActionEvent e) -> {
            if (returnFocusedTable() == null) {
                return;
            }
            JTable curTable = returnFocusedTable();
            int row = curTable.getSelectedRow();
            int ID = (int) curTable.getValueAt(row, 0);
            int update = SqlConnection.sqlExecuteUpdate("DELETE FROM Workplace WHERE ID = " + ID);
            System.out.println("name: " + ID + " sql: " + update);
            refreshTable(curTable);
        });

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createRigidArea(new Dimension(10, 10)));
        panel.add(addButton);
        panel.add(Box.createRigidArea(new Dimension(10, 10)));
        panel.add(deleteButton);

        return panel;
    }

    private JTable returnFocusedTable() {
        if (Worklist.isShowing()) {
            return Worklist;
        }
        if (Tasklist.isShowing()) {
            return Tasklist;
        }
        return null;
    }
}
