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
            crs.updateNull(1);
            for (int i = 1; i < numCols; i++) {
                if (getColumnName(i).contains("ID")) {
                    crs.updateInt(i + 1, ID);
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
            fireTableDataChanged();
        } catch (SyncProviderException ex) {
            System.out.println(ex.toString());
        }
    }

    public void discardChanges() {
        try {
            if (!crs.isFirst()) {
                crs.beforeFirst();
            }
            fireTableDataChanged();
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "SQL Error");
        }
    }
}
