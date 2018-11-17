package worklist;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import javax.swing.*;

public class UserInterface implements Runnable {

    private JTabbedPane tabPane;

    @Override
    public void run() {
        JFrame frame = new JFrame("place holder");
        if(!SqlConnection.sqlCheckIfExists()) {
            errorPopup("Unable to fetch data from database!\nSetup MySQL server.", "SQL Error");
            return;
        }
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(createTabPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JTabbedPane createTabPane() {
        tabPane = new JTabbedPane();
        try {
            JPanel worklistPanel = new JPanel();
            MyTableModel tblModel = new MyTableModel(SqlConnection.sqlGetCachedRowSet("SELECT * FROM Workplace"), 0);
            JTable table = new JTable(tblModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.addMouseListener(new MyMouseListener());
            worklistPanel.setLayout(new BoxLayout(worklistPanel, BoxLayout.Y_AXIS));
            worklistPanel.add(new JScrollPane(table));
            worklistPanel.add(createButtons(table));
            tabPane.addTab("Workplace", worklistPanel);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return tabPane;
    }

    private JPanel createButtons(JTable table) {
        JPanel panel = new JPanel();
        MyTableModel tblModel = (MyTableModel) table.getModel();

        JButton addButton = new JButton("Add");
        addButton.addActionListener((ActionEvent e) -> {
            tblModel.addRow();
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener((ActionEvent e) -> {
            if (table.getSelectedRow() != -1) {
                tblModel.removeRow(table.getSelectedRow());
            }
        });

        JButton updateButton = new JButton("Commit");
        updateButton.addActionListener((ActionEvent e) -> {
            tblModel.updateChanges();
        });

        JButton discardButton = new JButton("Discard");
        discardButton.addActionListener((ActionEvent e) -> {
            tblModel.discardChanges();
        });

        JButton closeButton = new JButton("Close Tab");
        closeButton.addActionListener((ActionEvent e) -> {
            if (tabPane.getSelectedIndex() != 0) {
                tabPane.removeTabAt(tabPane.getSelectedIndex());
            }
        });
        JButton setupButton = new JButton("Setup");
        GridLayout layout = new GridLayout(2, 4);
        layout.setHgap(5);
        layout.setVgap(5);
        panel.setLayout(layout);
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(updateButton);
        panel.add(discardButton);
        panel.add(closeButton);
        panel.add(setupButton);

        return panel;
    }

    public static void errorPopup(String str, String title) {
        JOptionPane.showMessageDialog(null, str, title, JOptionPane.ERROR_MESSAGE);
    }
    
    private class MyMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            JTable table = (JTable) e.getSource();
            if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                Point point = e.getPoint();
                int row = table.rowAtPoint(point);
                MyTableModel tblModel = (MyTableModel) table.getModel();
                String name = (String) tblModel.getValueAt(row, 1);
                int ID = (int) tblModel.getValueAt(row, 0);
                try {
                    JPanel panel = new JPanel();
                    tblModel = new MyTableModel(SqlConnection.sqlGetCachedRowSet("SELECT * FROM Tasklist WHERE ID = " + ID), ID);
                    JTable paneTbl = new JTable(tblModel);
                    paneTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.add(new JScrollPane(paneTbl));
                    panel.add(createButtons(paneTbl));
                    tabPane.addTab(name, panel);
                    tabPane.setSelectedIndex(tabPane.getComponentCount() - 1);
                } catch (SQLException ex) {
                    ex.getLocalizedMessage();
                }

            }
        }
    }
}
