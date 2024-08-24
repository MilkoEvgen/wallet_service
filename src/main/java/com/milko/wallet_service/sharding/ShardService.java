package com.milko.wallet_service.sharding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShardService {
    private final List<DataSource> dataSources;

    @Autowired
    private ShardService(List<DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    public DataSource getDataSourceByUuid(UUID uuid) {
        int index = Math.abs(uuid.hashCode() % dataSources.size());
        return dataSources.get(index);
    }

    public DataSource getRandomDataSource() {
        int index = Math.abs(UUID.randomUUID().hashCode() % dataSources.size());
        return dataSources.get(index);
    }

    public List<DataSource> getAllDataSources(){
        return new ArrayList<>(dataSources);
    }
}
