package worklist.ui;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.spi.SyncProviderException;
import javax.swing.table.AbstractTableModel;
import worklist.ParseRowSetInput;
import worklist.SqlConnection;

public class MyTableModel extends AbstractTableModel {

    private CachedRowSet crs;
    private ResultSetMetaData rsmd;
    private int numCols;
    private int numRows;
    private final int ID;

    public MyTableModel(CachedRowSet crs, int ID) throws SQLException {
        // Set local variables and calculate rows and column counts by
        // using RS metadata.
        this.crs = crs;
        this.crs.setShowDeleted(false);
        this.rsmd = crs.getMetaData();
        numCols = rsmd.getColumnCount();
        this.crs.beforeFirst();
        numRows = 0;
        while (this.crs.next()) {
            numRows++;
        }
        this.crs.beforeFirst();
        this.ID = ID;
    }

    @Override
    public int getRowCount() {
        return numRows;
    }

    @Override
    public int getColumnCount() {
        return numCols;
    }

    @Override
    public Class getColumnClass(int column) {
        // getColumnClass checks for class of col values to figure out how our
        // renderer is supposed to style the output. Booleans gets checkboxes, 
        // integers gets right-align etc.
        Class cls = null;
        for (int row = 0; row < numRows; row++) {
            Object value = getValueAt(row, column);
            if (value != null) {
                Class comparedCls = value.getClass();
                if (cls == null) {
                    cls = comparedCls;
                } else if (comparedCls.isAssignableFrom(cls)) {
                    cls = comparedCls;
                }
            }
            if (cls == null) {
                cls = Object.class;
            }

            return cls;
        }
        return cls;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            crs.absolute(rowIndex + 1);
            Object obj = crs.getObject(columnIndex + 1);
            if (obj == null) {
                return null;
            } else {
                return obj;
            }
        } catch (SQLException e) {
            return e.getLocalizedMessage();
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        try {
            if (value != null) {
                crs.absolute(rowIndex + 1);
                crs.updateObject(columnIndex + 1, value);
                crs.updateRow();
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "setValueAt() - SQL Error");
        }
    }

    @Override
    public String getColumnName(int column) {
        try {
            // Ugly hack to give our time columns space.
            if (rsmd.getColumnLabel(column + 1).contains("Expected")) {
                return "Expected Time";
            } else if (rsmd.getColumnLabel(column + 1).contains("Actual")) {
                return "Actual Time";
            }
            return rsmd.getColumnLabel(column + 1);
        } catch (SQLException e) {
            return e.getLocalizedMessage();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Fields containing *ID* will not be editable.
        // ID 0 = Workplace, no editable fields.
        if (this.ID == 0) {
            return false;
        }
        String str = getColumnName(columnIndex);
        return !(str.contains("ID"));
    }

    public void addRow() {
        // Creates an object from ParseRowSetInput.java and runs it.
        if(new ParseRowSetInput(crs, this, ID).parseInput()) {
            numRows++;
        }
    }

    public void removeRow(int row) {
        try {
            // Move to entered row, delete it, decrease total, reset the cursor
            // and fire dataChanged to repopulate the table.
            crs.absolute(row + 1);
            crs.deleteRow();
            numRows--;
            crs.beforeFirst();
            if (ID == 0) {
                updateChanges();
            } else {
                fireTableDataChanged();
            }
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "removeRow() - SQL Error");
        }

    }

    public void updateChanges() {
        try {
            crs.acceptChanges();
            // Calculates progress unless we're on the main tab.
            if (ID > 0) {
                calculateProgress();
            }
            fireTableDataChanged();
        } catch (SyncProviderException ex) {
            System.err.println(ex.toString());
        }
    }

    // Moves cursor to front, discards our changes and calls fire-function
    // to repopulate table with original data.
    public void discardChanges() {
        try {
            crs.beforeFirst();
            crs.restoreOriginal();
            fireTableDataChanged();
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "discardChanges() - SQL Error");
        }

    }

    // Calculates progress and updates Workplace with value every time we 
    // choose to commit our changes.
    private void calculateProgress() {
        // We can't calculate 0 rows and our main tab (ID=0) can't progress 
        if (numRows < 1 || ID == 0) {
            return;
        }
        try {
            crs.beforeFirst();
            int finished = 0;
            // Iterates through our table and checks how many booleans are true.
            while (crs.next()) {
                if (crs.getBoolean("Finished") == true) {
                    finished++;
                }
            }
            double i = ((double) finished / numRows) * 100.0;
            // Update our database with the progress.
            SqlConnection.sqlExecuteUpdate("UPDATE Workplace SET Progress = " + (int) i + " WHERE ID = " + ID);
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "SQL Error");
        }
    }
}
