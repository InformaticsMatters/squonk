package com.im.lac.services.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.services.CommonConstants;
import com.im.lac.services.ServerConstants;
import com.im.lac.services.dataset.service.DatasetHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class Utils {
    
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private static ObjectMapper mapper = new ObjectMapper();

    public static DataSource createDataSource() {
        DataSource ds = createDataSource(null, null, null, null, null);
        return ds;
    }

    public static DataSource createDataSource(String server, String port, String dbName, String user, String password) {
        
        PGSimpleDataSource ds = new PGSimpleDataSource();

        if (server == null) {
            server = System.getenv("SQUONK_DB_SERVER");
        }
        if (server == null) {
            server = "localhost";
        }
        if (port == null) {
            port = System.getenv("SQUONK_DB_PORT");
        }
        if (port == null) {
            port = "5432";
        }
        if (dbName == null) {
            dbName = System.getenv("SQUONK_DB_NAME");
        }
        if (dbName == null) {
            dbName = "squonk";
        }
        if (user == null) {
            user = System.getenv("SQUONK_DB_USERNAME");
        }
        if (user == null) {
            user = "tester";
        }
        if (password == null) {
            password = System.getenv("SQUONK_DB_PASSWORD");
        }
        ds.setServerName(server);
        ds.setPortNumber(new Integer(port));
        ds.setDatabaseName(dbName);
        ds.setUser(user);
        ds.setPassword(password == null ? "lacrocks" : password);
        
        LOG.log(Level.INFO, "Created datasource for server {0}@{1}:{2}/{3}", new Object[]{user, server, port, dbName});

        return ds;
    }

    public static String toJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public static <T> T fromJson(InputStream is, Class<T> type) throws IOException {
        return mapper.readValue(is, type);
    }

    public static <T> T fromJson(String s, Class<T> type) throws IOException {
        return mapper.readValue(s, type);
    }

    public static DatasetHandler getDatasetHandler(Exchange exch) {
        return exch.getContext().getRegistry().lookupByNameAndType(ServerConstants.DATASET_HANDLER, DatasetHandler.class);
    }

    public static String fetchUsername(Exchange exchange) {
        String username = exchange.getIn().getHeader(CommonConstants.HEADER_SQUONK_USERNAME, String.class);
        if (username == null) {
            throw new IllegalStateException("Validated username not specified");
        }
        return username;
    }

}
