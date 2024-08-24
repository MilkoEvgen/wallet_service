package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.model.TopUpRequest;
import com.milko.wallet_service.repository.TopUpRequestRepository;
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
public class TopUpRequestRepositoryImpl implements TopUpRequestRepository {

    @Override
    public TopUpRequest create(TopUpRequest request, DataSource dataSource) {
        log.info("IN create, request = {}", request);
        Map<String, Object> parameters = getParametersFromTopUpRequest(request);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("top_up_requests");
        simpleJdbcInsert.execute(parameters);
        return findById(request.getUuid(), dataSource).get();
    }

    @Override
    public Optional<TopUpRequest> findById(UUID id, DataSource dataSource) {
        log.info("IN findById, request id = {}", id);
        String sql = "SELECT * FROM top_up_requests WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        TopUpRequest topUpRequest = jdbcTemplate.queryForObject(sql, new TopUpRequestRepositoryImpl.TopUpRequestRowMapper(), id);
        return Optional.of(topUpRequest);
    }

    @Override
    public Optional<TopUpRequest> findByPaymentRequestId(UUID paymentRequestId, DataSource dataSource) {
        log.info("IN findById, request id = {}", paymentRequestId);
        String sql = "SELECT * FROM top_up_requests WHERE payment_request_uid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        TopUpRequest topUpRequest = jdbcTemplate.queryForObject(sql, new TopUpRequestRepositoryImpl.TopUpRequestRowMapper(), paymentRequestId);
        return Optional.of(topUpRequest);
    }

    private Map<String, Object> getParametersFromTopUpRequest(TopUpRequest request) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", request.getUuid());
        parameters.put("created_at", LocalDateTime.now());
        parameters.put("payment_request_uid", request.getPaymentRequestUid());
        return parameters;
    }

    private static final class TopUpRequestRowMapper implements RowMapper<TopUpRequest> {
        @Override
        public TopUpRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TopUpRequest.builder()
                    .uuid(UUID.fromString(rs.getString(("uuid"))))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .paymentRequestUid(UUID.fromString(rs.getString("payment_request_uid")))
                    .build();
        }
    }
}
