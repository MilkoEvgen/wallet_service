package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.model.WithdrawalRequest;
import com.milko.wallet_service.repository.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WithdrawalRequestRepositoryImpl implements WithdrawalRequestRepository {

    @Override
    public WithdrawalRequest create(WithdrawalRequest request, DataSource dataSource) {
        log.info("IN create, request = {}", request);
        Map<String, Object> parameters = getParametersFromWithdrawalRequest(request);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("withdrawal_requests");
        simpleJdbcInsert.execute(parameters);
        return findById(request.getUuid(), dataSource);
    }

    @Override
    public WithdrawalRequest findById(UUID requestId, DataSource dataSource) {
        log.info("IN findById, request id = {}", requestId);
        String sql = "SELECT * FROM withdrawal_requests WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            return jdbcTemplate.queryForObject(sql, new WithdrawalRequestRowMapper(), requestId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("WithdrawalRequest with id " + requestId + " not found", LocalDateTime.now());
        }
    }

    private Map<String, Object> getParametersFromWithdrawalRequest(WithdrawalRequest request) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", request.getUuid());
        parameters.put("created_at", LocalDateTime.now());
        parameters.put("payment_request_uid", request.getPaymentRequestUid());
        return parameters;
    }

    private static final class WithdrawalRequestRowMapper implements RowMapper<WithdrawalRequest> {
        @Override
        public WithdrawalRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            return WithdrawalRequest.builder()
                    .uuid(UUID.fromString(rs.getString(("uuid"))))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .paymentRequestUid(UUID.fromString(rs.getString("payment_request_uid")))
                    .build();
        }
    }
}
