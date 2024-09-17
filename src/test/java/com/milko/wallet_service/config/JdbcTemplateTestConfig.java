package com.milko.wallet_service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@TestConfiguration
public class JdbcTemplateTestConfig {

    @Bean(name = "jdbcTemplateDs0")
    public JdbcTemplate jdbcTemplateDs0(@Qualifier("ds0") DataSource ds0DataSource) {
        return new JdbcTemplate(ds0DataSource);
    }

    @Bean(name = "jdbcTemplateDs1")
    public JdbcTemplate jdbcTemplateDs1(@Qualifier("ds1") DataSource ds1DataSource) {
        return new JdbcTemplate(ds1DataSource);
    }
}

