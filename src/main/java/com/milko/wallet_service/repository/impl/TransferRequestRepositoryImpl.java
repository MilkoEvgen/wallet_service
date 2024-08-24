package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.model.TransferRequest;
import com.milko.wallet_service.repository.TransferRequestRepository;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TransferRequestRepositoryImpl implements TransferRequestRepository {

    @Override
    public TransferRequest create(TransferRequest request, DataSource dataSource) {
        log.info("IN create, request = {}", request);
        Map<String, Object> parameters = getParametersFromTransferRequest(request);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("transfer_requests");
        simpleJdbcInsert.execute(parameters);
        return findById(request.getUuid(), dataSource).get();
    }

    @Override
    public Optional<TransferRequest> findById(UUID uuid, DataSource dataSource) {
        log.info("IN findById, request id = {}", uuid);
        String sql = "SELECT * FROM transfer_requests WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        TransferRequest transferRequest = jdbcTemplate.queryForObject(sql, new TransferRequestRowMapper(), uuid);
        return Optional.of(transferRequest);
    }

    @Override
    public Optional<TransferRequest> findByPaymentRequestId(UUID paymentRequestId, DataSource dataSource) {
        log.info("IN findById, paymentRequestId = {}", paymentRequestId);
        String sql = "SELECT * FROM transfer_requests WHERE payment_request_uid_from = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        TransferRequest transferRequest = jdbcTemplate.queryForObject(sql, new TransferRequestRowMapper(), paymentRequestId);
        return Optional.of(transferRequest);
    }

    private Map<String, Object> getParametersFromTransferRequest(TransferRequest request) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", request.getUuid());
        parameters.put("created_at", LocalDateTime.now());
        parameters.put("payment_request_uid_from", request.getPaymentRequestUid());
        parameters.put("recipient_uid", request.getRecipientUid());
        parameters.put("wallet_uid_to", request.getWalletUidTo());
        return parameters;
    }

    private static final class TransferRequestRowMapper implements RowMapper<TransferRequest> {
        @Override
        public TransferRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TransferRequest.builder()
                    .uuid(UUID.fromString(rs.getString(("uuid"))))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .paymentRequestUid(UUID.fromString(rs.getString("payment_request_uid_from")))
                    .recipientUid(UUID.fromString(rs.getString("recipient_uid")))
                    .walletUidTo(UUID.fromString(rs.getString("wallet_uid_to")))
                    .build();
        }
    }
}
