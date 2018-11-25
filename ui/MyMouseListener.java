package worklist.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

public class MyMouseListener extends MouseAdapter {

    private JTabbedPane tabPane;

    public MyMouseListener(JTabbedPane tabPane) {
        this.tabPane = tabPane;
    }

    /*
        Custom mouse listener, listens for doubleclicks, gets focused row
        and creates a new tab with a JPanel containing TableModel and buttons.
        Tied to ID which we get from the first column of source tableModel.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        JTable table = (JTable) e.getSource();
        // Checks if we have a doubleclick and if it's in one of our rows.
        if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
            int row = table.rowAtPoint(e.getPoint());
            MyTableModel tblModel = (MyTableModel) table.getModel();
            String name = (String) tblModel.getValueAt(row, 1);
            int ID = (int) tblModel.getValueAt(row, 0);

            // Check to see if we've tried to open the current TAB already.
            // If it is we just focus it instead of creating a second tab.
            int tabs = tabPane.getComponentCount();
            if (tabs > 1) {
                for (int i = 0; i < tabs; i++) {
                    System.out.println("i: " + i + " tabs: " + tabs + " tabAt(" + i + "): " + tabPane.getComponentAt(i).getName());
                    if (tabPane.getComponentAt(i).getName().contains(name)) {
                        tabPane.setSelectedIndex(i);
                        return;
                    }
                }
            }
            tabPane.addTab(name, new MyNewTab(ID, name, tabPane));
            tabPane.setSelectedIndex(tabPane.getComponentCount() - 1);
        }
    }
}
