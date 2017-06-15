/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.chemaxon.db;

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
            conh = new ConnectionHandler();
            conh.setPropertyTable(propertyTable);
        }
        if (connection != null) {
            conh.setConnection(connection);
        } else if (dataSource != null) {
            try {
                conh.setConnection(dataSource.getConnection());
            } catch (SQLException ex) {
                throw new RuntimeException("Can't get connection from datasource", ex);
            }
        }
        return conh;
    }

    public void setConnectionHandler(ConnectionHandler conh) {
        this.conh = conh;
    }

    public void start() throws Exception {

    }

    public void stop() throws Exception {
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
