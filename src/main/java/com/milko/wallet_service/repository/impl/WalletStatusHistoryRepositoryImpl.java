package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.WalletStatusHistory;
import com.milko.wallet_service.repository.WalletStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
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

@Repository
@RequiredArgsConstructor
public class WalletStatusHistoryRepositoryImpl implements WalletStatusHistoryRepository {

    @Override
    public Boolean create(WalletStatusHistory walletStatusHistory, DataSource dataSource) {
        Map<String, Object> parameters = getParametersFromWalletStatusHistory(walletStatusHistory);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("wallet_status_history");

        int rowsAffected = simpleJdbcInsert.execute(parameters);
        return rowsAffected > 0;
    }

    @Override
    public List<WalletStatusHistory> findAllByWalletId(UUID walletUid, DataSource dataSource) {
        String SQL = "SELECT * FROM wallet_status_history WHERE wallet_uid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(SQL, new WalletStatusHistoryRowMapper(), walletUid);
    }

    private Map<String, Object> getParametersFromWalletStatusHistory(WalletStatusHistory walletStatusHistory){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", walletStatusHistory.getUuid());
        parameters.put("created_at", LocalDateTime.now());
        parameters.put("wallet_uid", walletStatusHistory.getWalletUid());
        parameters.put("changed_by_user_uid", walletStatusHistory.getChangedByUserUid());
        parameters.put("changed_by_profile_type", walletStatusHistory.getChangedByProfileType());
        parameters.put("reason", walletStatusHistory.getReason());
        parameters.put("from_status", walletStatusHistory.getFromStatus());
        parameters.put("comment", walletStatusHistory.getComment());
        parameters.put("to_status", walletStatusHistory.getToStatus());
        return parameters;
    }

    private static final class WalletStatusHistoryRowMapper implements RowMapper<WalletStatusHistory> {
        @Override
        public WalletStatusHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            return WalletStatusHistory.builder()
                    .uuid(UUID.fromString(rs.getString("uuid")))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .walletUid(UUID.fromString(rs.getString("wallet_uid")))
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
