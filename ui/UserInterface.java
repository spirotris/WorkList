package worklist.ui;

import worklist.ui.MyTabPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import worklist.SqlConnection;

public class UserInterface implements Runnable {

    @Override
    public void run() {
        JFrame frame = new JFrame("Workplace");
        ImageIcon img = new ImageIcon("src/worklist/graphics/icons8-timesheet-40.png");
        frame.setIconImage(img.getImage());
        // Checks if we can fetch SQL data, otherwise we quit.
        // Could be user/pass error or 'Workplace' table missing in database.
        if (!SqlConnection.sqlCheckIfExists()) {
            errorPopup("Unable to fetch data from database!\nSetup MySQL server.", "SQL Error");
            System.exit(0);
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
        frame.add(new MyTabPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Static errorPane that we can call from other classes.
    public static void errorPopup(String str, String title) {
        JOptionPane.showMessageDialog(null, str, title, JOptionPane.ERROR_MESSAGE);
    }
    
    // Same, but info icon.
    public static void infoPopup(String str, String title) {
        JOptionPane.showMessageDialog(null, str, title, JOptionPane.INFORMATION_MESSAGE);
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
    
    public static void setColWidth(JTable table, int colNum, int width) {
        table.getColumnModel().getColumn(colNum).setMinWidth(width);
        table.getColumnModel().getColumn(colNum).setMaxWidth(width);
    }
}
