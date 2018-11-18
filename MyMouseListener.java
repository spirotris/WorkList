package worklist;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class MyMouseListener extends MouseAdapter {

    private JTabbedPane tabPane;

    public MyMouseListener(JTabbedPane tabPane) {
        this.tabPane = tabPane;
    }

    /*
        Custom mouse listener, listens for doubleclicks, gets focused row
        and creates a new tab with a JPanel containing TableModel and buttons.
        Tied to ID.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        JTable table = (JTable) e.getSource();
        if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
            int row = table.rowAtPoint(e.getPoint());

            MyTableModel tblModel = (MyTableModel) table.getModel();
            String name = (String) tblModel.getValueAt(row, 1);
            int ID = (int) tblModel.getValueAt(row, 0);

            try {
                // This entire section is similar to createTabPane() in UserInterface
                // except here we call Tasklist and use the proper ID.
                JPanel panel = new JPanel();
                tblModel = new MyTableModel(
                        SqlConnection.sqlGetCachedRowSet(
                                "SELECT * FROM Tasklist WHERE ID = " + ID), ID);
                JTable paneTable = new JTable(tblModel);
                paneTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                UserInterface.hideIdCols(paneTable);
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(new JScrollPane(paneTable));
                panel.add(UserInterface.createButtons(paneTable));
                // Add our new tab and open it.
                tabPane.addTab(name, panel);
                tabPane.setSelectedIndex(tabPane.getComponentCount() - 1);
            } catch (SQLException ex) {
                ex.getLocalizedMessage();
            }

        }
    }
}
