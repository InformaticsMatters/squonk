package org.squonk.core.util;

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

    public static final SquonkServerConfig INSTANCE = new SquonkServerConfig();
    private DataSource squonkDataSource;
    private final String coreServiceBaseUrl;


    private SquonkServerConfig() {
        coreServiceBaseUrl = IOUtils.getConfiguration("SQUONK_SERVICES_CORE", "http://localhost/coreservices/rest/v1");
        LOG.info("Using core services base URL: " + coreServiceBaseUrl);
    }

    public DataSource getSquonkDataSource() {
        if (squonkDataSource == null) {
            String s = IOUtils.getConfiguration("SQUONK_DB_SERVER", "localhost");
            String po = IOUtils.getConfiguration("SQUONK_DB_PORT", "5432");
            String d = "squonk";
            String u = "squonk";
            String pw = IOUtils.getConfiguration("POSTGRES_SQUONK_PASS", "squonk");

            squonkDataSource = createDataSource(u, pw, d);

            LOG.log(Level.INFO, "Using datasource for squonk {0}@{1}:{2}/{3}", new Object[]{u, s, po, d});
        }
        return squonkDataSource;
    }


    private DataSource createDataSource(String username, String password, String database) {
        String s = IOUtils.getConfiguration("SQUONK_DB_SERVER", "localhost");
        String po = IOUtils.getConfiguration("SQUONK_DB_PORT", "5432");

        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(s);
        dataSource.setPortNumber(new Integer(po));
        dataSource.setDatabaseName(database);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    public String getCoreServiceBaseUrl() {
        return coreServiceBaseUrl;
    }
}
