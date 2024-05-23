package com.milko.wallet_service.sharding;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.UUID;

@Service
public class ShardService {
    private final DataSource ds0;
    private final DataSource ds1;
    private final DataSource trans_ds0;
    private final DataSource trans_ds1;

    private ShardService(@Qualifier("ds0") DataSource ds0,
                         @Qualifier("ds1") DataSource ds1,
                         @Qualifier("trans_ds0") DataSource trans_ds0,
                         @Qualifier("trans_ds1") DataSource trans_ds1) {
        this.ds0 = ds0;
        this.ds1 = ds1;
        this.trans_ds0 = trans_ds0;
        this.trans_ds1 = trans_ds1;
    }

    public DataSource getDataSourceByUuid(UUID uuid) {
        int index = uuid.hashCode() % 2;
        if (index == 0) return ds0;
        else return ds1;
    }

    public DataSource getRandomDataSource() {
        int index = UUID.randomUUID().hashCode() % 2;
        if (index == 0) return ds0;
        else return ds1;
    }

    public DataSource getFirstDataSource() {
        return ds0;
    }

    public DataSource getSecondDataSource() {
        return ds1;
    }

    public DataSource getFirstTransactionalDataSource() {
        return trans_ds0;
    }

    public DataSource getSecondTransactionalDataSource() {
        return trans_ds1;
    }

}
