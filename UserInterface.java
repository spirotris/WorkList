package worklist;

import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class UserInterface implements Runnable {

    private TableModel worklistTableModel;
    private TableModel tasklistTableModel;

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
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Worklists", listWorkplaces());
        tabbedPane.addTab("Tasklist", listTasklist());
        return tabbedPane;
    }

    private JScrollPane listWorkplaces() {
        Object[] colNames = SqlConnection.sqlGetColumnNames("SELECT * FROM Workplace");
        Object[][] tableData = SqlConnection.sqlGetTableData("SELECT * FROM Workplace");
        worklistTableModel = new DefaultTableModel(tableData, colNames);
        worklistTableModel.addTableModelListener(new MyTableModelListener("Workplace"));
        JTable table = new JTable(worklistTableModel);
        // Hide the ID column
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.setPreferredScrollableViewportSize(new Dimension(700, 500));
        table.setFillsViewportHeight(true);
        return new JScrollPane(table);
    }

    private JScrollPane listTasklist() {
        int testID = 1; // FRAGANCIA TEST
        Object[] colNames = SqlConnection.sqlGetColumnNames("SELECT * FROM Tasklist WHERE ID = " + testID);
        Object[][] tableData = SqlConnection.sqlGetTableData("SELECT * FROM Tasklist WHERE ID = " + testID);
        tasklistTableModel = new DefaultTableModel(tableData, colNames);
        tasklistTableModel.addTableModelListener(new MyTableModelListener("Tasklist WHERE ID = " + testID));
        JTable table = new JTable(tasklistTableModel);
        // Hide the ID column
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.setPreferredScrollableViewportSize(new Dimension(700, 500));
        table.setFillsViewportHeight(true);
        return new JScrollPane(table);
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(0, 40));
        JButton addButton = new JButton("Add Field");
        JButton deleteButton = new JButton("Delete Field");
        deleteButton.setEnabled(false);

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createRigidArea(new Dimension(10, 10)));
        panel.add(addButton);
        panel.add(Box.createRigidArea(new Dimension(10, 10)));
        panel.add(deleteButton);

        return panel;
    }
}
