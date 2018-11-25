package worklist;

import worklist.ui.UserInterface;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

public class SqlConnection {

    private static final String USER = "username";
    private static final String PASS = "password";
    private static final String DB = "WorkList";
    private static final String URL = "jdbc:mysql://127.0.0.1/" + DB + "?relaxAutoCommit=true";

    public static boolean sqlCheckIfExists() {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            DatabaseMetaData dbm = con.getMetaData();
            ResultSet rs = dbm.getTables("WorkList", null, null, null);
            boolean result = false;
            while (rs.next()) {
                if (rs.getString("TABLE_NAME").contains("Workplace")) {
                    result = true;
                }
            }
            return result;
        } catch (SQLException e) {
            System.err.println(e.getLocalizedMessage());
            return false;
        }
    }

    public static CachedRowSet sqlGetCachedRowSet(String query) {
        try {
            RowSetFactory factory = RowSetProvider.newFactory();
            CachedRowSet crs = factory.createCachedRowSet();
            crs.setUrl(URL);
            crs.setUsername(USER);
            crs.setPassword(PASS);
            crs.setReadOnly(false);
            crs.setCommand(query);
            crs.execute();
            return crs;
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "SQL Error");
            return null;
        }
    }

    public static void sqlExecuteUpdate(String query) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                Statement stmt = con.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            UserInterface.errorPopup(e.getLocalizedMessage(), "SQL Error");
        }
    }
}
