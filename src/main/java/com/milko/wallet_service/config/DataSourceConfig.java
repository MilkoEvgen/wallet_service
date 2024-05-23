package com.milko.wallet_service.config;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.UserTransaction;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.xa.PGXADataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.sql.XADataSource;

@Configuration
@EnableTransactionManagement
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

    @Bean(name = "trans_ds0")
    public XADataSource transactionalDataSource0(Environment env) {
        PGXADataSource xaDataSource = new PGXADataSource();
        xaDataSource.setUrl(env.getProperty("datasource.ds0.url"));
        xaDataSource.setUser(env.getProperty("datasource.ds0.user"));
        xaDataSource.setPassword(env.getProperty("datasource.ds0.password"));
        return xaDataSource;
    }

    @Bean(name = "trans_ds1")
    public XADataSource transactionalDataSource1(Environment env) {
        PGXADataSource xaDataSource = new PGXADataSource();
        xaDataSource.setUrl(env.getProperty("datasource.ds1.url"));
        xaDataSource.setUser(env.getProperty("datasource.ds1.user"));
        xaDataSource.setPassword(env.getProperty("datasource.ds1.password"));
        return xaDataSource;
    }

    @Bean
    public jakarta.transaction.UserTransaction userTransaction() {
        return UserTransaction.userTransaction();
    }

    @Bean
    public jakarta.transaction.TransactionManager transactionManager() {
        return TransactionManager.transactionManager();
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager(jakarta.transaction.UserTransaction userTransaction, jakarta.transaction.TransactionManager transactionManager) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setUserTransaction(userTransaction);
        jtaTransactionManager.setTransactionManager(transactionManager);
        return jtaTransactionManager;
    }

}
