package com.milko.wallet_service.transaction;

import com.milko.wallet_service.exceptions.TransactionFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TransactionManager {

    public void beginTransaction(DataSource dataSource) throws SQLException {
        TransactionContext context = TransactionContext.get();
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        context.registerTransaction(dataSource, connection);
    }

    public void addDataSourceToTransaction(DataSource dataSource) {
        TransactionContext context = TransactionContext.get();
        try {
            context.addDataSource(dataSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T executeInTransaction(List<DataSource> initialDataSources, TransactionalTask<T> task) {
        TransactionContext context = TransactionContext.get();
        T result;

        try {
            for (DataSource dataSource : initialDataSources) {
                beginTransaction(dataSource);
            }
            result = task.execute(context);

            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
            throw new TransactionFailedException("Transaction failed", e.getCause(), LocalDateTime.now());
        }

        return result;
    }

    public void commitTransaction() throws SQLException {
        TransactionContext context = TransactionContext.get();
        for (Connection connection : context.getAllConnections()) {
            connection.commit();
            connection.close();
        }
        TransactionContext.clear();
    }

    public void rollbackTransaction() {
        TransactionContext context = TransactionContext.get();
        for (Connection connection : context.getAllConnections()) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                // Логирование ошибки
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // Логирование ошибки
                }
            }
        }
        TransactionContext.clear();
    }
}
