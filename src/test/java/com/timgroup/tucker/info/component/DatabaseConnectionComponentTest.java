package com.timgroup.tucker.info.component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.component.DatabaseConnectionComponent.ConnectionProvider;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class DatabaseConnectionComponentTest {

    private final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);

    @Test
    public void canBeGivenCustomisedIdAndLabel() throws Exception {
        DatabaseConnectionComponent component = new DatabaseConnectionComponent("id", "label", connectionProvider);
        assertEquals("id", component.getId());
        assertEquals("label", component.getLabel());
    }

    @Test
    public void reportsOkWithConnectionMetadataIfAllIsWell() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connectionProvider.getConnection()).thenReturn(connection);
        final Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        final DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metadata);
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(metadata.getURL()).thenReturn("jdbc:nowhere");
        when(metadata.getUserName()).thenReturn("some-user");

        Report report = new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        assertThat(report.getStatus(), equalTo(Status.OK));
        assertThat(String.valueOf(report.getValue()), stringContainsInOrder(Arrays.asList("some-user", "jdbc:nowhere")));
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
        verify(connection).getMetaData();
    }

    @Test
    public void omitsUsernameFromMetadataWhenNull() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connectionProvider.getConnection()).thenReturn(connection);
        final Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        final DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metadata);
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(metadata.getURL()).thenReturn("jdbc:nowhere");
        when(metadata.getUserName()).thenReturn(null);

        Report report = new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        assertThat(report.getStatus(), equalTo(Status.OK));
        assertThat(String.valueOf(report.getValue()), containsString("jdbc:nowhere"));
    }

    @Test
    public void omitsMetadataWhenNotConfigured() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connectionProvider.getConnection()).thenReturn(connection);
        final Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        final DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metadata);
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(metadata.getURL()).thenReturn("jdbc:nowhere");
        when(metadata.getUserName()).thenReturn("some-user");

        Report report = new DatabaseConnectionComponent("id", "label", connectionProvider, false).getReport();
        assertThat(report.getStatus(), equalTo(Status.OK));
        assertThat(String.valueOf(report.getValue()), not(either(containsString("jdbc:nowhere")).or(containsString("some-user"))));
        verify(connection, never()).getMetaData();
    }

    @Test
    public void reportsCriticalIfConnectionCannotBeObtained() throws Exception {
        when(connectionProvider.getConnection()).thenThrow(new SQLException("foo"));

        final Report report = new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        assertEquals(Status.CRITICAL, report.getStatus());
        assertEquals("foo", report.getValue());
    }

    @Test
    public void reportsCriticalIfStatementCannotBeObtained() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connectionProvider.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenThrow(new SQLException("bar"));

        final Report report = new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        assertEquals(Status.CRITICAL, report.getStatus());
        assertEquals("bar", report.getValue());
    }

    @Test
    public void reportsCriticalIfStatementCannotBeExecuted() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connectionProvider.getConnection()).thenReturn(connection);
        final Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(Mockito.anyString())).thenThrow(new SQLException("baz"));

        final Report report = new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        assertEquals(Status.CRITICAL, report.getStatus());
        assertEquals("baz", report.getValue());
    }

    @Test
    public void alwaysClosesItsConnections() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connectionProvider.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenThrow(new SQLException("foo"));

        new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        verify(connection).close();
    }

    @Test
    public void reportsCriticalIfConnectionCannotBeClosed() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connectionProvider.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenAnswer(RETURNS_MOCKS);
        when(connection.getMetaData()).thenAnswer(RETURNS_MOCKS);
        doThrow(new SQLException("boz")).when(connection).close();

        final Report report = new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        assertEquals(Status.CRITICAL, report.getStatus());
        assertEquals("boz", report.getValue());
    }

}
