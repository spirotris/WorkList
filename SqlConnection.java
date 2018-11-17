package worklist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

public class SqlConnection {

    private static final String USER = "WorkUser";
    private static final String PASS = "WorkPass";
    private static final String URL = "jdbc:mysql://192.168.1.101/WorkList?relaxAutoCommit=true";

    public static boolean sqlCheckIfExists() {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                Statement stmt = con.createStatement()) {
            return stmt.execute("SELECT * FROM Workplace");
        } catch (SQLException e) {
            System.out.println(e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * MySQL Connection settings in private strings.
     *
     * @return Connection
     * @throws SQLException
     */
    public static Connection sqlGetConnection() throws SQLException {
        Connection con = DriverManager.getConnection(URL, USER, PASS);
        return con;
    }

    /*
    * Setup a CachedRowSet via factory and execute query.
    * URL and login hard-coded in file.
     */
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
            System.out.println("Unable to cache rowset: " + e.getLocalizedMessage());
            return null;
        }
    }
}
