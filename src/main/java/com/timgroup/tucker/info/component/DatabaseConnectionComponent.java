package com.timgroup.tucker.info.component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
        try (Connection dbConnection = connectionProvider.getConnection()) {
            long before = System.currentTimeMillis();
            try (Statement statement = dbConnection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("select 1;")) {
                    long after = System.currentTimeMillis();
                    return new Report(Status.OK, (after - before) + "ms" );
                }
            }
        } catch (SQLException e) {
            return new Report(Status.CRITICAL, e.getMessage());
        }
    }
}
