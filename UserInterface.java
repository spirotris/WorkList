package worklist;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        // Checks if we can fetch SQL data, otherwise we quit.
        if (!SqlConnection.sqlCheckIfExists()) {
            errorPopup("Unable to fetch data from database!\nSetup MySQL server.", "SQL Error");
            return;
        }
        // Avoid closing when you click the X. Instead we implement a new
        // windowlistener and launches a prompt to confirm exit.
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
            errorPopup(e.getLocalizedMessage(), "SQL Error");
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
                System.out.println("success!");
            } else {
                System.out.println("FAIL!");
            }
        });
        
        GridLayout layout = new GridLayout(2, 4);
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

    public static void errorPopup(String str, String title) {
        JOptionPane.showMessageDialog(null, str, title, JOptionPane.ERROR_MESSAGE);
    }

    /*
    Checks headerValues of each column and sets width to 1 for every
    header containing ID.
     */
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
