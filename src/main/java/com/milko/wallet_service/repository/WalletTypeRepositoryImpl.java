package com.milko.wallet_service.repository;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.WalletType;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class WalletTypeRepositoryImpl implements WalletTypeRepository{
    private final ShardService shardService;

    @Override
    @Transactional
    public Integer create(WalletType walletType) {
        Map<String, Object> parameters_0 = getParametersFromWalletType(walletType);
        Map<String, Object> parameters_1 = getParametersFromWalletType(walletType);

        SimpleJdbcInsert simpleJdbcInsert0 = new SimpleJdbcInsert(shardService.getFirstTransactionalDataSource())
                .withTableName("wallet_types")
                .usingGeneratedKeyColumns("id");
        SimpleJdbcInsert simpleJdbcInsert1 = new SimpleJdbcInsert(shardService.getSecondTransactionalDataSource())
                .withTableName("wallet_types")
                .usingGeneratedKeyColumns("id");

        Number generatedId_0 = simpleJdbcInsert0.executeAndReturnKey(parameters_0);
        parameters_1.put("id", generatedId_0);
        simpleJdbcInsert1.execute(parameters_1);
        return generatedId_0.intValue();
    }

    @Override
    public WalletType findById(Integer id) {
        String sql = "SELECT * FROM wallet_types WHERE id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(shardService.getRandomDataSource());
        return jdbcTemplate.queryForObject(sql, new WalletTypeRowMapper(), id);
    }

    @Override
    public List<WalletType> findAll() {
        String sql = "SELECT * FROM wallet_types";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(shardService.getRandomDataSource());
        return jdbcTemplate.query(sql, new WalletTypeRowMapper());
    }

    @Override
    @Transactional
    public WalletType update(WalletType walletType) {
        StringBuilder sql = new StringBuilder("UPDATE wallet_types SET ");
        Map<String, Object> parameters = new HashMap<>();

        if (walletType.getName() != null) {
            sql.append("name = :name, ");
            parameters.put("name", walletType.getName());
        }
        if (walletType.getCurrencyCode() != null) {
            sql.append("currency_code = :currency_code, ");
            parameters.put("currency_code", walletType.getCurrencyCode());
        }
        if (walletType.getStatus() != null) {
            sql.append("status = :status, ");
            parameters.put("status", walletType.getStatus().toString());
        }
        if (walletType.getProfileType() != null) {
            sql.append("profile_type = :profile_type, ");
            parameters.put("profile_type", walletType.getProfileType());
        }
        if (walletType.getModifier() != null) {
            sql.append("modifier = :modifier, ");
            parameters.put("modifier", walletType.getModifier());
        }
        sql.append("modified_at = :modified_at ");
        parameters.put("modified_at", LocalDateTime.now());
        sql.append("WHERE id = :id");
        parameters.put("id", walletType.getId());

        NamedParameterJdbcTemplate namedParameterJdbcTemplate_0 = new NamedParameterJdbcTemplate(shardService.getFirstTransactionalDataSource());
        NamedParameterJdbcTemplate namedParameterJdbcTemplate_1 = new NamedParameterJdbcTemplate(shardService.getSecondTransactionalDataSource());
        int rowsAffectedInDs_0 = namedParameterJdbcTemplate_0.update(sql.toString(), parameters);
        int rowsAffectedInDs_1 = namedParameterJdbcTemplate_1.update(sql.toString(), parameters);

        if (rowsAffectedInDs_0 != rowsAffectedInDs_1) {
            throw new RuntimeException();
        }

        String selectSql = "SELECT * FROM wallet_types WHERE id = :id";
        return namedParameterJdbcTemplate_0.queryForObject(selectSql, parameters, new WalletTypeRowMapper());
    }

    @Override
    @Transactional
    public Boolean deleteById(Integer id) {
        String sql = "UPDATE wallet_types SET status = 'DELETED' WHERE id = ?";
        JdbcTemplate jdbcTemplate_0 = new JdbcTemplate(shardService.getFirstTransactionalDataSource());
        JdbcTemplate jdbcTemplate_1 = new JdbcTemplate(shardService.getSecondTransactionalDataSource());
        int rowsAffectedInDs_0 = jdbcTemplate_0.update(sql, id);
        int rowsAffectedInDs_1 = jdbcTemplate_1.update(sql, id);
        if (rowsAffectedInDs_0 != rowsAffectedInDs_1){
            throw new RuntimeException();
        }
        return rowsAffectedInDs_0 > 0;
    }

    private Map<String, Object> getParametersFromWalletType(WalletType walletType){
        Map<String, Object> parameters = new HashMap<>();
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
