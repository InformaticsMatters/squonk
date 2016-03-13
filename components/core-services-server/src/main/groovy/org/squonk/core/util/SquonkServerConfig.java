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
    private final PGPoolingDataSource dataSource;
    private final String coreServiceBaseUrl;


    private SquonkServerConfig() {
        String s = IOUtils.getConfiguration("SQUONK_DB_SERVER", "localhost");
        String po = IOUtils.getConfiguration("SQUONK_DB_PORT", "5432");
        String d = "squonk";
        String u = "squonk";
        String pw = IOUtils.getConfiguration("POSTGRES_SQUONK_PASS", "squonk");

        dataSource = new PGPoolingDataSource();
        dataSource.setServerName(s);
        dataSource.setPortNumber(new Integer(po));
        dataSource.setDatabaseName(d);
        dataSource.setUser(u);
        dataSource.setPassword(pw);

        LOG.log(Level.INFO, "Using datasource for server {0}@{1}:{2}/{3}", new Object[]{u, s, po, d});
        //LOG.log(Level.INFO, "Connecting as {0}/{1}", new Object[]{u, pw});


        coreServiceBaseUrl = IOUtils.getConfiguration("SQUONK_SERVICES_CORE", "http://localhost/coreservices/rest/v1");
        LOG.info("Using core services base URL: " + coreServiceBaseUrl);

    }


    public DataSource getDataSource() {
        return dataSource;
    }

    public String getCoreServiceBaseUrl() {
        return coreServiceBaseUrl;
    }
}
