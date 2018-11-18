package worklist;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.stream.Stream;
import javax.swing.JTable;

public class ExportToPdf {

    private JTable inputTbl;
    private MyTableModel tblModel;
    private String filename;

    public ExportToPdf(JTable inputTable, String filename) {
        this.inputTbl = inputTable;
        this.filename = filename;
        tblModel = (MyTableModel) inputTbl.getModel();
    }

    public boolean writePdf() {
        try {

            int cols = tblModel.getColumnCount();
            String[] columnNames = new String[cols];
            for (int i = 0; i < cols; i++) {
                columnNames[i] = tblModel.getColumnName(i);
            }

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("src/" + filename + ".pdf"));

            document.open();

            PdfPTable table = new PdfPTable(cols);
            addTableHeader(table, columnNames);
            addRows(table);
            //addCustomRows(table);
            document.add(table);
            document.close();
            return true;
        } catch (FileNotFoundException | DocumentException e) {
            System.err.println(e.getLocalizedMessage());
            return false;
        }
    }

    private void addTableHeader(PdfPTable table, String[] columnNames) {
        Stream.of(columnNames)
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table) {
        int rows = tblModel.getRowCount();
        int cols = tblModel.getColumnCount();
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                Object obj = tblModel.getValueAt(j, i);
                table.addCell(obj + "");
            }
        }
    }

}
