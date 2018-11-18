package worklist;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.spi.SyncProviderException;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class MyTableModel extends AbstractTableModel {

    private CachedRowSet crs;
    private ResultSetMetaData rsmd;
    private int numCols;
    private int numRows;
    private int ID;

    public MyTableModel(CachedRowSet crs, int ID) throws SQLException {
        // Set local variables and calculate rows and column counts.
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
            UserInterface.errorPopup(e.getLocalizedMessage(), "SQL Error");
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
        try {
            crs.moveToInsertRow();
            // Our first row is the Primary which autoincrements, so we insert
            // null and let the database do the rest.
            crs.updateNull(1);
            for (int i = 1; i < numCols; i++) {
                String colName = getColumnName(i);

                // ID is never chosen, so we inut it ourselves.
                if (colName.contains("ID")) {
                    crs.updateInt(i + 1, ID);

                    // Finished is a boolean, so we convert a YES/NO option.
                } else if (colName.contains("Finished")) {
                    int j = JOptionPane.showConfirmDialog(null, "Finished?", "Test", JOptionPane.YES_NO_OPTION);
                    if (j == 0) {
                        crs.updateBoolean(i + 1, true);
                    } else {
                        crs.updateBoolean(i + 1, false);
                    }

                    // Check for integer, if not we return 0 as default
                } else if (colName.contains("Time")) {
                    String str = JOptionPane.showInputDialog("Value of: " + getColumnName(i));
                    int j = 0;
                    if (str != null) {
                        try {
                            j = Integer.valueOf(str);
                        } catch (NumberFormatException e) {
                            UserInterface.errorPopup("Invalid number! Defaults to 0", "Input Error");
                        }
                    }
                    crs.updateInt(i + 1, j);

                    // Progress always starts at 0
                } else if (colName.contains("Progress")) {
                    crs.updateInt(i + 1, 0);

                } else {
                    String str = JOptionPane.showInputDialog("Value of: " + getColumnName(i));
                    crs.updateObject(i + 1, str);
                }
            }

            crs.insertRow();
            numRows++;
            crs.moveToCurrentRow();
            fireTableDataChanged();
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "SQL Error");
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
            fireTableDataChanged();
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "SQL Error");
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

    public void discardChanges() {
        try {
            crs.beforeFirst();
            crs.restoreOriginal();
            fireTableDataChanged();
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "SQL Error");
        }

    }

    // Calculates progress and updates Workplace with value every time we 
    // choose to commit our changes.
    private void calculateProgress() {
        if (numRows < 1 || ID == 0) {
            return;
        }
        try {
            crs.beforeFirst();
            int finished = 0;
            while (crs.next()) {
                if (crs.getBoolean("Finished") == true) {
                    finished++;
                }
            }
            double i = ((double) finished / numRows) * 100.0;
            SqlConnection.sqlExecuteUpdate("UPDATE Workplace SET Progress = " + (int) i + " WHERE ID = " + ID);
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "SQL Error");
        }
    }
}
