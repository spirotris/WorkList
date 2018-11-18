package worklist;

import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressBarCellRenderer extends JProgressBar implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        MyTableModel tblModel = (MyTableModel) table.getModel();
        int progress = (int) tblModel.getValueAt(row, 2);
        setValue(progress);
        setString(progress+"%");
        setStringPainted(true);
        return this;
    }
}
