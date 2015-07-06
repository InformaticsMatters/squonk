package com.im.lac.services.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.services.ServerConstants;
import com.im.lac.services.dataset.service.DatasetHandler;
import java.io.IOException;
import java.io.InputStream;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class Utils {

    private static ObjectMapper mapper = new ObjectMapper();

    public static DataSource createDataSource() {


        DataSource ds = createDataSource(null, null, null, null, null);
        return ds;
    }

    public static DataSource createDataSource(String server, String port, String dbName, String user, String password) {
        PGSimpleDataSource ds = new PGSimpleDataSource();

        if (server == null) {
            server = System.getenv("CHEMCENTRAL_DB_SERVER");
        }
        if (port == null) {
            port = System.getenv("CHEMCENTRAL_DB_PORT");
        }
        if (dbName == null) {
            dbName = System.getenv("CHEMCENTRAL_DB_NAME");
        }
        if (user == null) {
            user = System.getenv("CHEMCENTRAL_DB_USERNAME");
        }
        if (password == null) {
            password = System.getenv("CHEMCENTRAL_DB_PASSWORD");
        }
        ds.setServerName(server == null ? "localhost" : server);
        ds.setPortNumber(new Integer(port == null ? "5432" : port));
        ds.setDatabaseName(dbName == null ? "chemcentral" : dbName);
        ds.setUser(user == null ? "chemcentral" : user);
        ds.setPassword(password == null ? "chemcentral" : password);

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

}
