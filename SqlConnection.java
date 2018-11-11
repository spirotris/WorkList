package worklist;

import java.sql.*;
import java.util.ArrayList;

public class SqlConnection {

    private static final String USER = "WorkUser";
    private static final String PASS = "WorkPass";
    private static final String URL = "jdbc:mysql://192.168.1.101/WorkList";

    /**
     *
     * @param query - SQL Query
     * @return
     */
    public static Object[] sqlGetColumnNames(String query) {
        ArrayList<String> strArr = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query.concat(" LIMIT 1"))) {
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                strArr.add(rsmd.getColumnName(i));
            }
        } catch (SQLException e) {
            System.out.println("SqlGetColumnNames() error: " + e.getLocalizedMessage() + "\nQuery: " + query);
            return strArr.toArray();
        }
        return strArr.toArray();
    }

    public static Object[][] sqlGetTableData(String query) {
        Object[][] tableData;
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();

            rs.last();
            int rowCount = rs.getRow();
            rs.beforeFirst();

            tableData = new Object[rowCount][colCount];
            while (rs.next()) {
                for (int i = 0; i < colCount; i++) {
                    tableData[rs.getRow() - 1][i] = rs.getObject(i + 1);
                }
            }
            return tableData;
        } catch (SQLException e) {
            System.out.println("SqlGetTableData() error: " + e.getLocalizedMessage() + "\nQuery: " + query);
            return null;
        }
    }

    public static int sqlExecuteUpdate(String query) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                Statement stmt = con.createStatement()) {
            return stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("SqlExecuteUpdate() error: " + e.getLocalizedMessage());
        }
        return 0;
    }

    /*
    public static boolean sqlExecuteQuery(String query) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
        } catch (SQLException e) {
            System.out.println("SqlExecuteQuery() error: " + e.getLocalizedMessage());
        }
        return false;
    }
     */
}
