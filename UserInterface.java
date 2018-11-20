package worklist;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.TableColumn;

public class UserInterface implements Runnable {

    // TabPane is local variable in order to access and close tabs from
    // different methods.
    private static JTabbedPane tabPane;

    @Override
    public void run() {
        JFrame frame = new JFrame("Worktime manager");
        ImageIcon img = new ImageIcon("graphics/icons8-timesheet-40.png");
        frame.setIconImage(img.getImage());
        // Checks if we can fetch SQL data, otherwise we quit.
        // Could be user/pass error or lack of SQL structure.
        if (!SqlConnection.sqlCheckIfExists()) {
            errorPopup("Unable to fetch data from database!\nSetup MySQL server.", "SQL Error");
            return;
        }
        // Avoid exiting when you close the window. Instead we implement a new
        // windowlistener and launch a prompt to confirm exit.
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                    String ObjButtons[] = {"Yes", "No"};
                    int PromptResult
                            = JOptionPane.showOptionDialog(null,
                                    "Are you sure you want to exit?",
                                    "Unsaved changes?",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.WARNING_MESSAGE,
                                    null, ObjButtons, ObjButtons[1]);
                    if (PromptResult == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
            }
        });
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(createTabPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JTabbedPane createTabPane() {
        tabPane = new JTabbedPane();
        try {
            JPanel worklistPanel = new JPanel();
            // Populate our tablemodel through custom SQL function that returns
            // a CachedRowSet object. 0 indicates this is our main tab.
            MyTableModel tblModel = new MyTableModel(
                    SqlConnection.sqlGetCachedRowSet("SELECT * FROM Workplace"), 0);
            JTable table = new JTable(tblModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            // Custom mouse listener that listens for dblclicks to open new tabs.
            table.addMouseListener(new MyMouseListener(tabPane));
            // hideIdCols simply sets width of columns with "ID" to 1
            hideIdCols(table);
            // Custom cell renderer in order to facilitate JProgressBars rendering
            table.getColumn("Progress").setCellRenderer(new ProgressBarCellRenderer());
            worklistPanel.setLayout(new BoxLayout(worklistPanel, BoxLayout.Y_AXIS));
            // Add our table to a scrollpane and create a button panel below.
            worklistPanel.add(new JScrollPane(table));
            worklistPanel.add(createButtons(table));
            tabPane.addTab("Workplace", worklistPanel);
        } catch (SQLException e) {
            errorPopup(e.getLocalizedMessage(), "createTabPane() - SQL Error");
        }
        return tabPane;
    }

    public static JPanel createButtons(JTable table) {
        JPanel panel = new JPanel();
        // Get the tablemodel so we can fetch cells and add function to our buttons.
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
        JButton setupButton = new JButton("Export PDF");
        setupButton.addActionListener((ActionEvent e) -> {
            ExportToPdf pdf = new ExportToPdf(table, "Test"); // TODO: FILENAME!
            if(pdf.writePdf()) {
                System.out.println("Success!");
            } else {
                System.err.println("FAIL!");
            }
        });
        
        // Layout sections for our button panel. 2 rows and 3 cols with 5px gaps.
        GridLayout layout = new GridLayout(2, 3);
        layout.setHgap(5);
        layout.setVgap(5);
        panel.setLayout(layout);
        
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(updateButton);
        panel.add(discardButton);
        panel.add(closeButton);
        panel.add(setupButton);

        return panel;
    }

    // Static errorPane that we can call from other classes.
    public static void errorPopup(String str, String title) {
        JOptionPane.showMessageDialog(null, str, title, JOptionPane.ERROR_MESSAGE);
    }

    // Checks headerValues and sets ID column widths to 1 to "hide" them
    public static void hideIdCols(JTable table) {
        if (table == null) {
            return;
        }
        int numCols = table.getColumnCount();
        for (int i = 0; i < numCols; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            if (col.getHeaderValue().toString().contains("ID")) {
                col.setMaxWidth(1);
                col.setMinWidth(1);
                col.setWidth(1);
                col.setPreferredWidth(1);
                table.doLayout();
            }
        }
    }
}
