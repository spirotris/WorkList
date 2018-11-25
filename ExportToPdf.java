package worklist;

import worklist.ui.MyTableModel;
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
            // Below we create a document and use itextpdf to start a pdf
            // output filestream.
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("src/worklist/pdf/" + filename + ".pdf"));
            document.open();

            PdfPTable table = new PdfPTable(cols);
            addTableHeader(table, columnNames);
            addRows(table);
            document.add(table);
            document.addTitle(filename);
            document.close();
            return true;
        } catch (FileNotFoundException | DocumentException e) {
            System.err.println(e.getLocalizedMessage());
            return false;
        }
    }

    private void addTableHeader(PdfPTable table, String[] columnNames) {
        // Here we use a stream to run through each column name from our
        // array, create a cell, config it and add to header.
        Stream.of(columnNames)
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table) {
        int rows = tblModel.getRowCount();
        int cols = tblModel.getColumnCount();
        // Runs through our table and puts every single cell into the document.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Object obj = tblModel.getValueAt(i, j);
                table.addCell(obj + "");
            }
        }
    }
}
