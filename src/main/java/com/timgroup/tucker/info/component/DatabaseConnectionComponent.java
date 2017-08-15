package com.timgroup.tucker.info.component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
    private final boolean fetchMetadata;
    private final Status failureStatus;

    public interface ConnectionProvider {
        Connection getConnection() throws SQLException;
    }

    public DatabaseConnectionComponent(String id, String label, ConnectionProvider connectionProvider) {
        this(id, label, connectionProvider, true);
    }

    public DatabaseConnectionComponent(String id, String label, ConnectionProvider connectionProvider, boolean fetchMetadata) {
        super(id, label);
        this.connectionProvider = connectionProvider;
        this.fetchMetadata = fetchMetadata;
        this.failureStatus = Status.CRITICAL;
    }

    @Override
    public Report getReport() {
        try (Connection dbConnection = connectionProvider.getConnection()) {
            String durationString;
            long before = System.currentTimeMillis();
            try (Statement statement = dbConnection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("select 1;")) {
                    long after = System.currentTimeMillis();
                    durationString = (after - before) + "ms";
                }
            }

            if (fetchMetadata) {
                DatabaseMetaData metaData = dbConnection.getMetaData();
                String user = metaData.getUserName();
                String jdbcUrl = metaData.getURL();
                String prefix = user != null && !user.isEmpty() ? user + " @ " + jdbcUrl : jdbcUrl;
                return new Report(Status.OK, prefix + ": " + durationString);
            }
            else {
                return new Report(Status.OK, durationString);
            }
        } catch (SQLException e) {
            return new Report(failureStatus, e.getMessage());
        }
    }
}
