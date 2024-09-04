package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.Wallet;
import com.milko.wallet_service.repository.WalletRepository;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {

    @Override
    public Wallet create(Wallet wallet, DataSource dataSource) {
        log.info("IN WalletRepository create, wallet = {}", wallet);
        Map<String, Object> parameters = getParametersFromWallet(wallet);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("wallets");
        simpleJdbcInsert.execute(parameters);
        return findById(wallet.getUuid(), dataSource).get();
    }

    @Override
    public Optional<Wallet> findById(UUID walletId, DataSource dataSource) {
        log.info("IN WalletRepository findById, walletId = {}", walletId);
        String sql = "SELECT * FROM wallets WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Wallet wallet = jdbcTemplate.queryForObject(sql, new WalletRepositoryImpl.WalletRowMapper(), walletId);
        return Optional.of(wallet);
    }

    @Override
    public List<Wallet> findAllByUserId(UUID uuid, DataSource dataSource) {
        log.info("IN WalletRepository findAllByUserId, UUID = {}", uuid);
        String sql = "SELECT * FROM wallets WHERE profile_uid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new WalletRepositoryImpl.WalletRowMapper(), uuid);
    }

    @Override
    public Optional<Wallet> update(Wallet wallet, DataSource dataSource) {
        log.info("IN WalletRepository update, wallet = {}", wallet);
        StringBuilder sql = new StringBuilder("UPDATE wallets SET ");
        Map<String, Object> parameters = new HashMap<>();

        if (wallet.getName() != null) {
            sql.append("name = :name, ");
            parameters.put("name", wallet.getName());
        }
        if (wallet.getWalletTypeId() != null) {
            sql.append("wallet_type_id = :wallet_type_id, ");
            parameters.put("wallet_type_id", wallet.getWalletTypeId());
        }
        if (wallet.getStatus() != null) {
            sql.append("status = :status, ");
            parameters.put("status", wallet.getStatus());
        }
        if (wallet.getBalance() != null) {
            sql.append("balance = :balance, ");
            parameters.put("balance", wallet.getBalance());
        }
        sql.append("modified_at = :modified_at ");
        parameters.put("modified_at", LocalDateTime.now());
        sql.append("WHERE uuid = :uuid");
        parameters.put("uuid", wallet.getUuid());

        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcTemplate.update(sql.toString(), parameters);

        return findById(wallet.getUuid(), dataSource);
    }

    @Override
    public Boolean updateBalance(UUID walletId, BigDecimal newBalance, DataSource dataSource) {
        log.info("IN WalletRepository updateBalance, walletId = {}, newBalance = {}", walletId, newBalance);
        String sql = "UPDATE wallets SET balance = ? WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int rowsAffected = jdbcTemplate.update(sql, newBalance, walletId);
        return rowsAffected > 0;
    }

    @Override
    public Boolean updateStatus(UUID walletId, Status status, DataSource dataSource) {
        log.info("IN WalletRepository updateStatus, walletId = {}", walletId);
        String sql = "UPDATE wallets SET status = ?, modified_at = ? WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int rowsAffected = jdbcTemplate.update(sql, status.toString(), LocalDateTime.now(), walletId);
        return rowsAffected > 0;
    }

    @Override
    public Boolean deleteById(UUID walletId, DataSource dataSource) {
        log.info("IN WalletRepository deleteById, walletId = {}", walletId);
        String sql = "UPDATE wallets SET status = 'DELETED', archived_at = ? WHERE uuid = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int rowsAffected = jdbcTemplate.update(sql, LocalDateTime.now(), walletId);
        return rowsAffected > 0;
    }

    private Map<String, Object> getParametersFromWallet(Wallet wallet) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", wallet.getUuid());
        parameters.put("created_at", LocalDateTime.now());
        parameters.put("name", wallet.getName());
        parameters.put("wallet_type_id", wallet.getWalletTypeId());
        parameters.put("profile_uid", wallet.getProfileUid());
        parameters.put("status", wallet.getStatus());
        parameters.put("balance", wallet.getBalance());
        return parameters;
    }

    private static final class WalletRowMapper implements RowMapper<Wallet> {
        @Override
        public Wallet mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Wallet.builder()
                    .uuid(UUID.fromString(rs.getString(("uuid"))))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .modifiedAt(rs.getTimestamp("modified_at") != null ? rs.getTimestamp("modified_at").toLocalDateTime() : null)
                    .name(rs.getString("name"))
                    .walletTypeId(UUID.fromString(rs.getString("wallet_type_id")))
                    .profileUid(UUID.fromString(rs.getString("profile_uid")))
                    .status(Status.valueOf(rs.getString("status")))
                    .balance(rs.getBigDecimal("balance"))
                    .archivedAt(rs.getTimestamp("archived_at") != null ? rs.getTimestamp("archived_at").toLocalDateTime() : null)
                    .build();
        }
    }
}
