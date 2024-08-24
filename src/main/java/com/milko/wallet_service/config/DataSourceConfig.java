package com.milko.wallet_service.config;


import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;


@Configuration
public class DataSourceConfig {

    @Bean(name = "ds0")
    public DataSource dataSource0(Environment env) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(env.getProperty("datasource.ds0.url"));
        dataSource.setUsername(env.getProperty("datasource.ds0.user"));
        dataSource.setPassword(env.getProperty("datasource.ds0.password"));
        dataSource.setDriverClassName(env.getProperty("datasource.ds0.driver"));
        return dataSource;
    }

    @Bean(name = "ds1")
    public DataSource dataSource1(Environment env) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(env.getProperty("datasource.ds1.url"));
        dataSource.setUsername(env.getProperty("datasource.ds1.user"));
        dataSource.setPassword(env.getProperty("datasource.ds1.password"));
        dataSource.setDriverClassName(env.getProperty("datasource.ds1.driver"));
        return dataSource;
    }

}