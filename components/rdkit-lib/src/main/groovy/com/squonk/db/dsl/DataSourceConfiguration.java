package com.squonk.db.dsl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by timbo on 16/12/2015.
 */
public class DataSourceConfiguration implements IConfiguration {

    final DataSource dataSource;
    final Map props;

    public DataSourceConfiguration(DataSource dataSource, Map props) {
        this.dataSource = dataSource;
        this.props = props;

    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public <T> T getProperty(String name) {
        return (T) props.get(name);
    }

    @Override
    public <T> T getProperty(String name, T defaultValue) {
        return (T) props.getOrDefault(name, defaultValue);
    }

}
