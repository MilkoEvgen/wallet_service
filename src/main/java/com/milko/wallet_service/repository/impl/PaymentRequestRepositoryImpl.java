package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.PaymentRequest;
import com.milko.wallet_service.repository.PaymentRequestRepository;
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
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentRequestRepositoryImpl implements PaymentRequestRepository {

    @Override
    public PaymentRequest create(PaymentRequest request, DataSource dataSource) {
        log.info("IN create, request = {}", request);
        Map<String, Object> parameters = getParametersFromPaymentRequest(request);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("payment_requests");
        simpleJdbcInsert.execute(parameters);
        return findById(request.getId(), dataSource).get();
    }

    @Override
    public Optional<PaymentRequest> findById(UUID requestId, DataSource dataSource) {
        log.info("IN findById, requestId = {}", requestId);
        String sql = "SELECT * FROM payment_requests WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PaymentRequest request = jdbcTemplate.queryForObject(sql, new PaymentRequestRowMapper(), requestId);
        return Optional.of(request);
    }

    @Override
    public List<PaymentRequest> findAllByUserId(UUID profileId, DataSource dataSource) {
        log.info("IN findAllByUserId, UUID = {}", profileId);
        String sql = "SELECT * FROM payment_requests WHERE profile_uid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new PaymentRequestRowMapper(), profileId);
    }

    private Map<String, Object> getParametersFromPaymentRequest(PaymentRequest request) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", request.getId());
        parameters.put("created_at", request.getCreatedAt());
        parameters.put("expired_at", request.getExpiredAt());
        parameters.put("profile_uid", request.getProfileUid());
        parameters.put("owner_wallet_uid", request.getOwnerWalletUid());
        parameters.put("amount", request.getAmount());
        parameters.put("fee", request.getFee());
        parameters.put("type", request.getType().toString());
        parameters.put("status", request.getStatus());
        return parameters;
    }

    private static final class PaymentRequestRowMapper implements RowMapper<PaymentRequest> {
        @Override
        public PaymentRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            return PaymentRequest.builder()
                    .id(UUID.fromString(rs.getString(("uuid"))))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .expiredAt(rs.getTimestamp("expired_at").toLocalDateTime())
                    .profileUid(UUID.fromString(rs.getString("profile_uid")))
                    .ownerWalletUid(UUID.fromString(rs.getString("owner_wallet_uid")))
                    .amount(rs.getBigDecimal("amount"))
                    .fee(rs.getBigDecimal("fee"))
                    .type(RequestType.valueOf(rs.getString("type")))
                    .status(Status.valueOf(rs.getString("status")))
                    .build();
        }
    }
}
