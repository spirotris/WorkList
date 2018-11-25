package worklist.ui;

import java.sql.SQLException;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import worklist.SqlConnection;
import static worklist.ui.UserInterface.setColWidth;

public class MyNewTab extends JPanel {

    public MyNewTab(int ID, String name, JTabbedPane tabPane) {
        try {
            MyTableModel tblModel = new MyTableModel(
                    SqlConnection.sqlGetCachedRowSet(
                            "SELECT * FROM Tasklist WHERE ID = " + ID), ID);
            JTable table = new JTable(tblModel);
            setColWidth(table, 3, 90);
            setColWidth(table, 4, 90);
            setColWidth(table, 5, 50);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setReorderingAllowed(false);
            UserInterface.hideIdCols(table);
            super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            super.setName(name);
            super.add(new JScrollPane(table));
            super.add(new MyTabPaneButtons(table, tabPane));
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "NewTab() - SQL Error");
        }
    }
}
