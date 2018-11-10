package worklist;

public class WorkList {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UserInterface ui = new UserInterface();
        ui.run();
    }
}

/*
Simple login.. pre-filled? mysql-based? ip/user/pass?
Database creation with tables on first login if they don't exist already.
Editable tables, refresh fields
-- Separate class for each pane? Refresh-methods.
Adding columns to workplace/tasklist/task.
*/