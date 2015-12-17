package com.squonk.db.rdkit.dsl;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by timbo on 16/12/2015.
 */
public interface IConfiguration {

    Connection getConnection() throws SQLException;

    <T> T getProperty(String name);

    <T> T getProperty(String name, T defaultValue);
}
