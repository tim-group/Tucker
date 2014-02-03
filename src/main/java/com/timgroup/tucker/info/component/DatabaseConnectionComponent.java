package com.timgroup.tucker.info.component;

import java.sql.Connection;
import java.sql.SQLException;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

/**
 * Reports whether the application can connect to the database.
 */
public final class DatabaseConnectionComponent extends Component {
    private final ConnectionProvider connectionProvider;

    public interface ConnectionProvider {
        Connection getConnection() throws SQLException;
    }

    public DatabaseConnectionComponent(String id, String label, ConnectionProvider connectionProvider) {
        super(id, label);
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Report getReport() {
        Connection dbConnection = null;
        try {
            dbConnection = connectionProvider.getConnection();
            dbConnection.createStatement().executeQuery("select 1;");
            return new Report(Status.OK);
        } catch (SQLException e) {
            return new Report(Status.CRITICAL, e.getMessage());
        } finally {
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (SQLException e) {
                    return new Report(Status.CRITICAL, e.getMessage());
                }
            }
        }
    }
}
