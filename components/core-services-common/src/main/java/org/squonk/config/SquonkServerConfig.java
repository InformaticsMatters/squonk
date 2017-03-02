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

    public static final String SQUONK_DB_SERVER = "postgres";

    private static DataSource squonkDataSource;

    public static DataSource getSquonkDataSource() {
        if (squonkDataSource == null) {
            String s = SQUONK_DB_SERVER;
            String po = "5432";
            String d = "squonk";
            String u = "squonk";
            String pw = IOUtils.getConfiguration("POSTGRES_SQUONK_PASS", "squonk");

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
