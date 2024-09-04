package com.milko.wallet_service.transaction;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TransactionContext {
    private static final ThreadLocal<TransactionContext> threadLocalContext = ThreadLocal.withInitial(TransactionContext::new);
    private final Map<DataSource, Connection> dataSourceConnections = new HashMap<>();

    public static TransactionContext get() {
        return threadLocalContext.get();
    }

    public void registerTransaction(DataSource dataSource, Connection connection) {
        dataSourceConnections.put(dataSource, connection);
    }

    public Connection getConnection(DataSource dataSource) {
        return dataSourceConnections.get(dataSource);
    }

    public void addDataSource(DataSource dataSource) throws SQLException {
        if (!dataSourceConnections.containsKey(dataSource)) {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            dataSourceConnections.put(dataSource, connection);
        }
    }

    public Collection<Connection> getAllConnections() {
        return dataSourceConnections.values();
    }

    public static void clear() {
        threadLocalContext.remove();
    }

    public boolean hasActiveTransaction() {
        return !dataSourceConnections.isEmpty();
    }
}
