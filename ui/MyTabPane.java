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

public class MyTabPane extends JTabbedPane {

    public MyTabPane() {
        try {
            JPanel panel = new JPanel();
            // Populate our tablemodel through custom SQL function that returns
            // a CachedRowSet object. 0 indicates this is our main tab.
            MyTableModel tblModel = new MyTableModel(
                    SqlConnection.sqlGetCachedRowSet(
                            "SELECT * FROM Workplace"), 0);
            JTable table = new JTable(tblModel);

            setColWidth(table, 2, 100);
            
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setReorderingAllowed(false);
            // Custom mouse listener that listens for dblclicks to open new tabs.
            table.addMouseListener(new MyMouseListener(this));

            // hideIdCols simply sets width of columns with "ID" to 1
            UserInterface.hideIdCols(table);

            // Custom cell renderer in order to facilitate JProgressBars rendering
            table.getColumn("Progress").setCellRenderer(new ProgressBarCellRenderer());
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            // Add our table to a scrollpane and create a button panel below.
            panel.add(new JScrollPane(table));
            panel.add(new MyTabPaneButtons(table, this));
            panel.setName("Workplace");
            super.addTab("Workplace", panel);
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "createTabPane() - SQL Error");
        }
    }
}
