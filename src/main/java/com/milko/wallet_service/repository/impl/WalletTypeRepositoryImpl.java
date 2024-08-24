package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.WalletType;
import com.milko.wallet_service.repository.WalletTypeRepository;
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
public class WalletTypeRepositoryImpl implements WalletTypeRepository {

    @Override
    public Boolean create(WalletType walletType, DataSource dataSource) {
        log.info("IN WalletTypeRepository create, walletType = {}", walletType);
        Map<String, Object> parameters = getParametersFromWalletType(walletType);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("wallet_types");

        int rowsAffected = simpleJdbcInsert.execute(parameters);
        return rowsAffected > 0;
    }

    @Override
    public void rollbackCreate(Integer id, DataSource dataSource) {
        log.info("IN WalletTypeRepository rollbackCreate, id = {}", id);
        String sql = "DELETE FROM wallet_types WHERE id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, id);
    }

    @Override
    public WalletType findById(Integer id, DataSource dataSource) {
        log.info("IN WalletTypeRepository findById, id = {}", id);
        String sql = "SELECT * FROM wallet_types WHERE id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForObject(sql, new WalletTypeRowMapper(), id);
    }

    @Override
    public List<WalletType> findAll(DataSource dataSource) {
        log.info("IN WalletTypeRepository findAll");
        String sql = "SELECT * FROM wallet_types";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new WalletTypeRowMapper());
    }

    @Override
    public Boolean updateStatus(Status status, Integer id, UUID changedByUserId, DataSource dataSource) {
        log.info("IN WalletTypeRepository update, status = {}, id = {}", status, id);
        String sql = "UPDATE wallet_types SET status = ?, modified_at = ?, modifier = ? WHERE id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int rowsAffected = jdbcTemplate.update(sql, status.name(), LocalDateTime.now(), changedByUserId.toString(), id);

        return rowsAffected > 0;
    }

    @Override
    public void rollbackUpdate(Status status, Integer id, LocalDateTime dateTime, String changedByUserId, DataSource dataSource) {
        log.info("IN WalletTypeRepository rollbackUpdate, status = {}, id = {}", status, id);
        String sql = "UPDATE wallet_types SET status = ?, modified_at = ?, modifier = ? WHERE id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, status.name(), dateTime, changedByUserId, id);
    }

    @Override
    public Boolean deleteById(Integer id, DataSource dataSource) {
        log.info("IN WalletTypeRepository deleteById, id = {}", id);
        String sql = "UPDATE wallet_types SET status = 'DELETED', archived_at = ? WHERE id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int rowsAffected = jdbcTemplate.update(sql, LocalDateTime.now(), id);
        return rowsAffected > 0;
    }

    @Override
    public void rollbackDeleteById(Integer id, DataSource dataSource) {
        log.info("IN WalletTypeRepository rollbackDeleteById, id = {}", id);
        String sql = "UPDATE wallet_types SET status = 'ACTIVE', archived_at = ? WHERE id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, null, id);
    }

    @Override
    public Integer getMaxId(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String SQL = "SELECT max(id) FROM wallet_types";
        return jdbcTemplate.queryForObject(SQL, Integer.class);
    }

    @Override
    public String getCurrentStatusByWalletId(Integer id, DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String SQL = "SELECT status FROM wallet_types WHERE id = ?";
        return jdbcTemplate.queryForObject(SQL, String.class);
    }

    private Map<String, Object> getParametersFromWalletType(WalletType walletType){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", walletType.getId());
        parameters.put("name", walletType.getName());
        parameters.put("created_at", LocalDateTime.now());
        parameters.put("currency_code", walletType.getCurrencyCode());
        parameters.put("status", walletType.getStatus());
        parameters.put("profile_type", walletType.getProfileType());
        parameters.put("creator", walletType.getCreator());
        return parameters;
    }

    private static final class WalletTypeRowMapper implements RowMapper<WalletType> {
        @Override
        public WalletType mapRow(ResultSet rs, int rowNum) throws SQLException {
            return WalletType.builder()
                    .id(rs.getInt("id"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .modifiedAt(rs.getTimestamp("modified_at") != null ? rs.getTimestamp("modified_at").toLocalDateTime() : null)
                    .name(rs.getString("name"))
                    .currencyCode(rs.getString("currency_code"))
                    .status(Status.valueOf(rs.getString("status")))
                    .archivedAt(rs.getTimestamp("archived_at") != null ? rs.getTimestamp("archived_at").toLocalDateTime() : null)
                    .profileType(rs.getString("profile_type"))
                    .creator(rs.getString("creator"))
                    .modifier(rs.getString("modifier"))
                    .build();
        }
    }

}
