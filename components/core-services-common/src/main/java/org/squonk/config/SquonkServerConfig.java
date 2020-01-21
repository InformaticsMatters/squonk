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

package org.squonk.config;

import org.postgresql.ds.PGPoolingDataSource;
import org.squonk.util.IOUtils;

import javax.sql.DataSource;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 12/03/16.
 */
public class SquonkServerConfig {

    private static final Logger LOG = Logger.getLogger(SquonkServerConfig.class.getName());

    public static final String SQUONK_DB_SERVER = IOUtils.getConfiguration("POSTGRES_HOSTNAME", "postgres");

    private static DataSource squonkDataSource;


    public SquonkServerConfig() {
        LOG.info("Using PostgreSQL at " + SQUONK_DB_SERVER);
    }

    public static DataSource getSquonkDataSource() {
        if (squonkDataSource == null) {
            String s = SQUONK_DB_SERVER;
            String po = "5432";
            String d = IOUtils.getConfiguration("POSTGRES_SQUONK_DATABASE", "squonk");
            String u = IOUtils.getConfiguration("POSTGRES_SQUONK_USER", "squonk");
            String pw = IOUtils.getConfiguration("POSTGRES_SQUONK_PASSWORD", "squonk");

            squonkDataSource = createDataSource(s, new Integer(po), u, pw, d);

            LOG.log(Level.INFO, "Using datasource for squonk {0}@{1}:{2}/{3}", new Object[]{u, s, po, d});
        }
        return squonkDataSource;
    }


    public static DataSource createDataSource(String server, Integer port, String username, String password, String database) {
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(server);
        dataSource.setPortNumber(port);
        dataSource.setDatabaseName(database);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }

}
