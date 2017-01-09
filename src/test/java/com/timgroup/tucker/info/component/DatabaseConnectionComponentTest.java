package com.timgroup.tucker.info.component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.mockito.Mockito;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.component.DatabaseConnectionComponent.ConnectionProvider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
    public void reportsOkIfAllIsWell() throws Exception {
        when(connectionProvider.getConnection()).thenAnswer(RETURNS_MOCKS);

        Report report = new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        assertEquals(Status.OK, report.getStatus());
    }

    @Test
    public void reportsCriticalIfConnectionCannotBeObtained() throws Exception {
        when(connectionProvider.getConnection()).thenThrow(new SQLException("foo"));

        final Report report = new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        assertEquals(Status.CRITICAL, report.getStatus());
        assertEquals("foo", report.getValue());
    }

    @Test
    public void reportsWarningIfConnectionCannotBeObtainedAndComponentConfiguredForWarning() throws Exception {
        when(connectionProvider.getConnection()).thenThrow(new SQLException("foo"));

        final Report report = new DatabaseConnectionComponent("id", "label", connectionProvider, Status.WARNING).getReport();
        assertEquals(Status.WARNING, report.getStatus());
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
        doThrow(new SQLException("boz")).when(connection).close();

        final Report report = new DatabaseConnectionComponent("id", "label", connectionProvider).getReport();
        assertEquals(Status.CRITICAL, report.getStatus());
        assertEquals("boz", report.getValue());
    }

}
