package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.WalletTypeStatusHistory;
import com.milko.wallet_service.repository.WalletTypeStatusHistoryRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Repository
public class WalletTypeStatusHistoryRepositoryImpl implements WalletTypeStatusHistoryRepository {

    @Override
    public Boolean create(WalletTypeStatusHistory walletTypeStatusHistory, DataSource dataSource) {
        Map<String, Object> parameters = getParametersFromWalletTypeStatusHistory(walletTypeStatusHistory);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("wallet_types_status_history");

        int rowsAffected = simpleJdbcInsert.execute(parameters);
        return rowsAffected > 0;
    }

    @Override
    public void rollbackCreate(Long id, DataSource dataSource) {
        String SQL = "DELETE FROM wallet_types_status_history WHERE id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(SQL, id);
    }

    @Override
    public List<WalletTypeStatusHistory> findAllByWalletTypeId(UUID walletTypeId, DataSource dataSource) {
        String SQL = "SELECT * FROM wallet_types_status_history WHERE wallet_type_id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(SQL, new WalletTypeStatusHistoryRowMapper(), walletTypeId);
    }

    @Override
    public Long getMaxId(DataSource dataSource) {
        String SQL = "SELECT max(id) FROM wallet_types_status_history";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForObject(SQL, Long.class);
    }

    private Map<String, Object> getParametersFromWalletTypeStatusHistory(WalletTypeStatusHistory walletTypeStatusHistory){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", walletTypeStatusHistory.getUuid());
        parameters.put("created_at", LocalDateTime.now());
        parameters.put("wallet_type_id", walletTypeStatusHistory.getWalletTypeId());
        parameters.put("changed_by_user_uid", walletTypeStatusHistory.getChangedByUserUid());
        parameters.put("changed_by_profile_type", walletTypeStatusHistory.getChangedByProfileType());
        parameters.put("reason", walletTypeStatusHistory.getReason());
        parameters.put("from_status", walletTypeStatusHistory.getFromStatus());
        parameters.put("comment", walletTypeStatusHistory.getComment());
        parameters.put("to_status", walletTypeStatusHistory.getToStatus());
        return parameters;
    }

    private static final class WalletTypeStatusHistoryRowMapper implements RowMapper<WalletTypeStatusHistory> {
        @Override
        public WalletTypeStatusHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            return WalletTypeStatusHistory.builder()
                    .uuid(UUID.fromString(rs.getString("uuid")))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .walletTypeId(UUID.fromString(rs.getString("wallet_type_id")))
                    .changedByUserUid(UUID.fromString(rs.getString("changed_by_user_uid")))
                    .changedByProfileType(rs.getString("changed_by_profile_type"))
                    .reason(rs.getString("reason"))
                    .fromStatus(Status.valueOf(rs.getString("from_status")))
                    .comment(rs.getString("comment"))
                    .toStatus(Status.valueOf(rs.getString("to_status")))
                    .build();
        }
    }
}
