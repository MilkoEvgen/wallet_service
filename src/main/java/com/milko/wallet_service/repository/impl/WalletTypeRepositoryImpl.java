package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.model.WalletType;
import com.milko.wallet_service.repository.WalletTypeRepository;
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
    public WalletType findById(UUID uuid, DataSource dataSource) {
        log.info("IN WalletTypeRepository findById, uuid = {}", uuid);
        String sql = "SELECT * FROM wallet_types WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            WalletType walletType = jdbcTemplate.queryForObject(sql, new WalletTypeRowMapper(), uuid);
            return walletType;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("WalletType with id " + uuid + " not found", LocalDateTime.now());
        }
    }

    @Override
    public List<WalletType> findAll(DataSource dataSource) {
        log.info("IN WalletTypeRepository findAll");
        String sql = "SELECT * FROM wallet_types";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new WalletTypeRowMapper());
    }

    @Override
    public Boolean updateStatus(ChangeWalletTypeInputDto dto, LocalDateTime modifyingTime, DataSource dataSource) {
        log.info("IN WalletTypeRepository update, status = {}, id = {}", dto.getToStatus().name(), dto.getWalletTypeId());
        String sql = "UPDATE wallet_types SET status = ?, modified_at = ?, modifier = ? WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int rowsAffected = jdbcTemplate.update(sql, dto.getToStatus().name(), modifyingTime, dto.getChangedByUserUid().toString(), dto.getWalletTypeId());

        return rowsAffected > 0;
    }

    @Override
    public Boolean deleteById(UUID uuid, DataSource dataSource) {
        log.info("IN WalletTypeRepository deleteById, uuid = {}", uuid);
        String sql = "UPDATE wallet_types SET status = 'DELETED', archived_at = ? WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int rowsAffected = jdbcTemplate.update(sql, LocalDateTime.now(), uuid);
        return rowsAffected > 0;
    }

    private Map<String, Object> getParametersFromWalletType(WalletType walletType){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", walletType.getUuid());
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
                    .uuid(UUID.fromString(rs.getString("uuid")))
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
