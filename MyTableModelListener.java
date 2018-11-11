package worklist;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class MyTableModelListener implements TableModelListener {

    private final String TABLE;

    public MyTableModelListener(String table) {
        this.TABLE = table;
    }

    /*
    *
    * DELETE -1
    * INSERT 1
    * UPDATE 0
    */
    
    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel) e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);
        
        int id = (int) model.getValueAt(row, 0);
        int sqlUpdate = SqlConnection.sqlExecuteUpdate("UPDATE " + TABLE + " SET " + columnName + " ='" + data + "' WHERE ID = '" + id + "'");
        System.out.println("Table value changed " + data + " col: " + column + " row: " + row + " ID: " + model.getValueAt(row, 0));
        System.out.println("Sql Update: " + sqlUpdate);
    }
    
}