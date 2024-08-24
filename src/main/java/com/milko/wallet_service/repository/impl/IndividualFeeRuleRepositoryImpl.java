package com.milko.wallet_service.repository.impl;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.IndividualFeeRule;
import com.milko.wallet_service.repository.IndividualFeeRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class IndividualFeeRuleRepositoryImpl implements IndividualFeeRuleRepository {

    @Override
    public IndividualFeeRule create(IndividualFeeRule feeRule, DataSource dataSource) {
        log.info("in create, feeRule = {}", feeRule);
        Map<String, Object> parameters = getParametersFromIndividualFeeRule(feeRule);
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("individual_fee_rules");
        jdbcInsert.execute(parameters);
        return getById(feeRule.getId(), dataSource);
    }

    @Override
    public Optional<IndividualFeeRule> getByTransactionType(RequestType transactionType, DataSource dataSource) {
        log.info("in getByTransactionType, transactionType = {}", transactionType);
        String SQL = "SELECT * FROM individual_fee_rules WHERE transaction_type = ? LIMIT 1";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        IndividualFeeRule feeRule = jdbcTemplate.queryForObject(SQL, new IndividualFeeRuleRowMapper(), transactionType.toString());
        return Optional.of(feeRule);
    }

    @Override
    public IndividualFeeRule getById(Long id, DataSource dataSource) {
        log.info("in getById, uuid = {}", id);
        String SQL = "SELECT * FROM individual_fee_rules WHERE id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForObject(SQL, new IndividualFeeRuleRowMapper(), id);
    }

    private Map<String, Object> getParametersFromIndividualFeeRule(IndividualFeeRule feeRule) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", feeRule.getId());
        parameters.put("created_at", feeRule.getCreatedAt());
        parameters.put("modified_at", feeRule.getModifiedAt());
        parameters.put("transaction_type", feeRule.getTransactionType().toString());
        parameters.put("percentage", feeRule.getPercentage());
        parameters.put("fixed_amount", feeRule.getFixedAmount());
        parameters.put("option", feeRule.getOption());
        parameters.put("archived_at", feeRule.getArchivedAt());
        parameters.put("wallet_uid", feeRule.getWalletUid());
        parameters.put("is_all_wallets", feeRule.getIsAllWallets());
        parameters.put("wallet_type_id", feeRule.getWalletTypeId());
        parameters.put("payment_method_id", feeRule.getPaymentMethodId());
        parameters.put("amount_from", feeRule.getAmountFrom());
        parameters.put("amount_to", feeRule.getAmountTo());
        parameters.put("highest_priority", feeRule.getHighestPriority());
        parameters.put("fee_currency", feeRule.getFeeCurrency());
        parameters.put("fee_rule_type", feeRule.getFeeRuleType());
        parameters.put("start_date", feeRule.getStartDate());
        parameters.put("end_date", feeRule.getEndDate());
        parameters.put("status", feeRule.getStatus());
        parameters.put("creator_profile_uid", feeRule.getCreatorProfileUid());
        parameters.put("modifier_profile_uid", feeRule.getModifierProfileUid());
        return parameters;
    }

    private static final class IndividualFeeRuleRowMapper implements RowMapper<IndividualFeeRule> {
        @Override
        public IndividualFeeRule mapRow(ResultSet rs, int rowNum) throws SQLException {
            return IndividualFeeRule.builder()
                    .id(rs.getLong("id"))
                    .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .modifiedAt(rs.getTimestamp("modified_at") != null ? rs.getTimestamp("modified_at").toLocalDateTime() : null)
                    .transactionType(RequestType.valueOf(rs.getString("transaction_type")))
                    .percentage(rs.getBigDecimal("percentage"))
                    .fixedAmount(rs.getBigDecimal("fixed_amount"))
                    .option(rs.getString("option"))
                    .archivedAt(rs.getTimestamp("archived_at") != null ? rs.getTimestamp("archived_at").toLocalDateTime() : null)
                    .walletUid(rs.getString("wallet_uid") != null ? UUID.fromString(rs.getString("wallet_uid")) : null)
                    .isAllWallets(rs.getBoolean("is_all_wallets"))
                    .walletTypeId(rs.getInt("wallet_type_id"))
                    .paymentMethodId(rs.getInt("payment_method_id"))
                    .amountFrom(rs.getInt("amount_from"))
                    .amountTo(rs.getInt("amount_to"))
                    .highestPriority(rs.getBoolean("highest_priority"))
                    .feeCurrency(rs.getString("fee_currency"))
                    .feeRuleType(rs.getString("fee_rule_type"))
                    .startDate(rs.getTimestamp("start_date") != null ? rs.getTimestamp("start_date").toLocalDateTime() : null)
                    .endDate(rs.getTimestamp("end_date") != null ? rs.getTimestamp("end_date").toLocalDateTime() : null)
                    .status(rs.getString("status") != null ? Status.valueOf(rs.getString("status")) : null)
                    .creatorProfileUid(rs.getString("creator_profile_uid") != null ? UUID.fromString(rs.getString("creator_profile_uid")) : null)
                    .modifierProfileUid(rs.getString("modifier_profile_uid") != null ? UUID.fromString(rs.getString("modifier_profile_uid")) : null)
                    .build();
        }
    }


}
