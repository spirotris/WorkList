package worklist;

import worklist.ui.UserInterface;
import worklist.ui.MyTableModel;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.swing.JOptionPane;

public class ParseRowSetInput {

    private CachedRowSet crs;
    private MyTableModel tblModel;
    private int ID;

    public ParseRowSetInput(CachedRowSet crs, MyTableModel tblModel, int ID) {
        this.crs = crs;
        this.tblModel = tblModel;
        this.ID = ID;
    }

    public boolean parseInput() {
        try {
            crs.moveToInsertRow();
            // Our first row is the Primary which autoincrements, so we insert
            // null and let the database do the rest.
            crs.updateNull(1);

            // Loop through the number of columns and ask for input depending
            // on what the columnName is.
            for (int i = 1; i < tblModel.getColumnCount(); i++) {
                String colName = tblModel.getColumnName(i);

                // ID is never chosen, so we inut it ourselves.
                if (colName.contains("ID")) {
                    crs.updateInt(i + 1, ID);

                    // Assume user doesn't add completed tasks.
                } else if (colName.contains("Finished")) {
                    crs.updateBoolean(i + 1, false);

                    // Check for integer, if not we return 0 as default
                } else if (colName.contains("Expected")) {
                    String str = JOptionPane.showInputDialog(tblModel.getColumnName(i));
                    int j = 0;
                    if (str != null) {
                        try {
                            j = Integer.valueOf(str);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number! Defaults to 0");
                        }
                    }
                    crs.updateInt(i + 1, j);

                    // Progress and Actual Time always starts at 0
                } else if (colName.contains("Progress") || colName.contains("Actual")) {
                    crs.updateInt(i + 1, 0);
                } else {
                    if (ID == 0) {
                        String str = JOptionPane.showInputDialog(tblModel.getColumnName(i) + " of workplace:");
                        if(str==null) {
                            return false;
                        }
                        crs.updateObject(i + 1, str);
                    } else {
                        String str = JOptionPane.showInputDialog(tblModel.getColumnName(i) + " of task:");
                        crs.updateObject(i + 1, str);
                    }
                }
            }

            crs.insertRow();
            crs.moveToCurrentRow();
            if (ID == 0) {
                tblModel.updateChanges();
            } else {
                tblModel.fireTableDataChanged();
            }
            return true;
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "addRow() - SQL Error");
            return false;
        }
    }

}
