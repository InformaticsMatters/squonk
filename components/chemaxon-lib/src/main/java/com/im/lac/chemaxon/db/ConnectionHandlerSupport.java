package com.im.lac.chemaxon.db;

import chemaxon.jchem.db.DatabaseProperties;
import chemaxon.util.ConnectionHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author timbo
 */
public class ConnectionHandlerSupport {

    private Connection connection;
    private DataSource dataSource;
    private ConnectionHandler conh;
    private String propertyTable = ConnectionHandler.DEFAULT_PROPERTY_TABLE;
    private static final Logger LOG = Logger.getLogger(ConnectionHandlerSupport.class.getName());

    public ConnectionHandlerSupport() {

    }

    public ConnectionHandlerSupport(ConnectionHandler conh) {
        this.conh = conh;
    }

    public ConnectionHandlerSupport(Connection connection) {
        this.connection = connection;
    }

    public ConnectionHandlerSupport(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Get the value of propertyTable
     *
     * @return the value of propertyTable
     */
    public String getPropertyTable() {
        return propertyTable;
    }

    /**
     * Set the value of propertyTable
     *
     * @param propertyTable new value of propertyTable
     */
    public void setPropertyTable(String propertyTable) {
        this.propertyTable = propertyTable;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ConnectionHandler getConnectionHandler() {
        if (conh == null) {
            LOG.warning("No ConnectionHandler. Should call start() before getConnectionHandler()");
            try {
                start();
            } catch (Exception ex) {
                throw new RuntimeException("start() failed. Was ConnectionHandler specified correctly?", ex);
            }
        }
        return conh;
    }

    public void setConnectionHandler(ConnectionHandler conh) {
        this.conh = conh;
    }

    public void start() throws Exception {
        if (conh == null) {
            if (connection == null) {
                if (dataSource == null) {
                    throw new IllegalStateException("Must provide ConnectionHandler, DataSource or Connection");
                }
                connection = dataSource.getConnection();
            }
            conh = new ConnectionHandler(connection, propertyTable);
        }
    }

    public void stop() {
        if (conh != null) {
            conh.setConnection(null);
        }
    }

    public void createPropertyTable(boolean failIfPresent) throws SQLException {
        ConnectionHandler conh = getConnectionHandler();
        if (!DatabaseProperties.propertyTableExists(conh)) {
            DatabaseProperties.createPropertyTable(conh);
        } else {
            if (failIfPresent) {
                throw new IllegalStateException("Property table already exists");
            }
        }
    }

}
