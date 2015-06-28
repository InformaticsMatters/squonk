package com.im.lac.services.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.services.camel.Constants;
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
        PGSimpleDataSource ds = new PGSimpleDataSource();

        String server = System.getenv("CHEMCENTRAL_DB_SERVER");
        ds.setServerName(server == null ? "localhost" : server);
        String port = System.getenv("CHEMCENTRAL_DB_PORT");
        ds.setPortNumber(new Integer(port == null ? "5432" : port));
        String dbname = System.getenv("CHEMCENTRAL_DB_NAME");
        ds.setDatabaseName(dbname == null ? "chemcentral" : dbname);
        String user = System.getenv("CHEMCENTRAL_DB_USERNAME");
        ds.setUser(user == null ? "chemcentral" : user);
        String password = System.getenv("CHEMCENTRAL_DB_PASSWORD");
        ds.setPassword(password == null ? "chemcentral" : password);

        user = null;
        password = null;

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
        return exch.getContext().getRegistry().lookupByNameAndType(Constants.DATASET_HANDLER, DatasetHandler.class);
    }

}
