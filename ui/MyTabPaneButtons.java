package worklist.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import worklist.ExportToPdf;

public class MyTabPaneButtons extends JPanel {
    
    public MyTabPaneButtons(JTable table, JTabbedPane tabPane) {
        // Get the tablemodel so we can fetch cells and add function to our buttons.
        // Each button calls methods from the MyTableModel class
        MyTableModel tblModel = (MyTableModel) table.getModel();
        
        JButton addButton = new JButton("Add");
        addButton.addActionListener((ActionEvent e) -> {
            tblModel.addRow();
        });
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener((ActionEvent e) -> {
            if (table.getSelectedRow() != -1) {
                tblModel.removeRow(table.getSelectedRow());
            }
        });
        
        JButton updateButton = new JButton("Commit");
        updateButton.addActionListener((ActionEvent e) -> {
            tblModel.updateChanges();
        });
        
        JButton discardButton = new JButton("Discard");
        discardButton.addActionListener((ActionEvent e) -> {
            tblModel.discardChanges();
        });
        
        JButton closeButton = new JButton("Close Tab");
        closeButton.addActionListener((ActionEvent e) -> {
            // Make sure we don't close the "Main" tab.
            if (tabPane.getSelectedIndex() != 0) {
                tabPane.removeTabAt(tabPane.getSelectedIndex());
            }
        });
        JButton exportButton = new JButton("Export PDF");
        exportButton.addActionListener((ActionEvent e) -> {
            int curTab = tabPane.getSelectedIndex();
            if (curTab != 0) {
                String name = tabPane.getTitleAt(curTab);
                // Update changes to make sure we get what's displayed.
                tblModel.updateChanges();
                ExportToPdf pdf = new ExportToPdf(table, name);
                if (pdf.writePdf()) {
                    UserInterface.infoPopup("PDF Export Success:\nFilename: " + name + ".pdf", "PDF Writer");
                } else {
                    UserInterface.errorPopup("Failed to write PDF file.", "PDF Writer");
                }
            }
        });

        // Layout sections for our button panel. 2 rows and 3 cols with 5px gaps.
        GridLayout layout = new GridLayout(2, 3);
        layout.setHgap(5);
        layout.setVgap(5);
        super.setLayout(layout);
        
        super.add(addButton);
        super.add(deleteButton);
        super.add(updateButton);
        super.add(discardButton);
        super.add(closeButton);
        super.add(exportButton);
    }
}
