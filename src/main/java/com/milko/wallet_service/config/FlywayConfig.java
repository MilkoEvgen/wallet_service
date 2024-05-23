package com.milko.wallet_service.config;


import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flywayDs0(@Qualifier("ds0") DataSource ds0) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds0)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    public Flyway flywayDs1(@Qualifier("ds1") DataSource ds1) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds1)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }
}
