package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.Transaction;
import com.milko.wallet_service.model.TransactionStatus;
import com.milko.wallet_service.model.Wallet;
import com.milko.wallet_service.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    @Override
    public Transaction create(Transaction transaction, DataSource dataSource) {
        log.info("IN create, transaction = {}", transaction);
        Map<String, Object> parameters = getParametersFromTransaction(transaction);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("transactions");
        simpleJdbcInsert.execute(parameters);
        return findById(transaction.getUuid(), dataSource).get();
    }

    @Override
    public Transaction updateStatus(UUID transactionId, TransactionStatus status, DataSource dataSource) {
        log.info("IN updateStatus, transactionId = {}", transactionId);
        String sql = "UPDATE transactions SET status = ?, modified_at = ? WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, status.toString(), LocalDateTime.now(), transactionId);
        return findById(transactionId, dataSource).get();
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId, DataSource dataSource) {
        log.info("IN findById, transactionId = {}", transactionId);
        String sql = "SELECT * FROM transactions WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Transaction transaction = jdbcTemplate.queryForObject(sql, new TransactionRowMapper(), transactionId);
        return Optional.of(transaction);
    }

    private Map<String, Object> getParametersFromTransaction(Transaction transaction) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", transaction.getUuid());
        parameters.put("created_at", transaction.getCreatedAt());
        parameters.put("modified_at", transaction.getModifiedAt());
        parameters.put("linked_transaction", transaction.getLinkedTransaction());
        parameters.put("profile_uid", transaction.getProfileUid());
        parameters.put("wallet_uid", transaction.getWalletUid());
        parameters.put("wallet_name", transaction.getWalletName());
        parameters.put("balance_operation_amount", transaction.getBalanceOperationAmount());
        parameters.put("raw_amount", transaction.getRawAmount());
        parameters.put("fee", transaction.getFee());
        parameters.put("amount_in_usd", transaction.getAmountInUsd());
        parameters.put("type", transaction.getType());
        parameters.put("state", transaction.getState());
        parameters.put("payment_request_uid", transaction.getPaymentRequestUid());
        parameters.put("currency_code", transaction.getCurrencyCode());
        parameters.put("refund_fee", transaction.getRefundFee());
        parameters.put("status", transaction.getStatus().toString());
        return parameters;
    }

    private static final class TransactionRowMapper implements RowMapper<Transaction> {
        @Override
        public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Transaction.builder()
                    .uuid(UUID.fromString(rs.getString("uuid")))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .modifiedAt(rs.getTimestamp("modified_at") != null ? rs.getTimestamp("modified_at").toLocalDateTime() : null)
                    .linkedTransaction(rs.getString("linked_transaction") != null ? UUID.fromString(rs.getString("linked_transaction")) : null)
                    .profileUid(UUID.fromString(rs.getString("profile_uid")))
                    .walletUid(UUID.fromString(rs.getString("wallet_uid")))
                    .walletName(rs.getString("wallet_name"))
                    .balanceOperationAmount(rs.getBigDecimal("balance_operation_amount"))
                    .rawAmount(rs.getBigDecimal("raw_amount"))
                    .fee(rs.getBigDecimal("fee"))
                    .amountInUsd(rs.getBigDecimal("amount_in_usd"))
                    .type(rs.getString("type"))
                    .state(rs.getString("state"))
                    .paymentRequestUid(UUID.fromString(rs.getString("payment_request_uid")))
                    .currencyCode(rs.getString("currency_code"))
                    .refundFee(rs.getLong("refund_fee"))
                    .status(TransactionStatus.valueOf(rs.getString("status")))
                    .build();
        }
    }


}
