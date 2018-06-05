package org.squonk.rdkit.db;

import org.postgresql.ds.PGPoolingDataSource;
import org.squonk.util.IOUtils;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChemcentralConfig {

    private static final Logger LOG = Logger.getLogger(ChemcentralConfig.class.getName());

    public static final String DEFAULT_SCHEMA = "vendordbs";

    private boolean initialised = false;
    private Map<String, RDKitTable> chemTables;
    private final DataSource dataSource;
    private final String statsRoute;

    public ChemcentralConfig() {
        this(null);
    }

    public ChemcentralConfig(String statsRoute) {

        this(
                IOUtils.getConfiguration("CHEMCENTRAL_HOST", "postgres"),
                new Integer(IOUtils.getConfiguration("CHEMCENTRAL_PORT", "5432")),
                IOUtils.getConfiguration("CHEMCENTRAL_DB", "chemcentral"),
                IOUtils.getConfiguration("CHEMCENTRAL_USER", "chemcentral"),
                IOUtils.getConfiguration("CHEMCENTRAL_PASSWORD", "chemcentral"),
                statsRoute);
    }

    public ChemcentralConfig(
            String host,
            Integer port,
            String database,
            String username,
            String password,
            String statsRoute) {

        this.statsRoute = statsRoute;

        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setServerName(host);
        ds.setPortNumber(new Integer(port));
        ds.setDatabaseName(database);
        ds.setUser(username);
        ds.setPassword(password);
        dataSource = ds;
        LOG.info("Created postgres datasource for " + host + " as user " + username);
    }

    public ChemcentralConfig(DataSource dataSource, String statsRoute) {
        this.dataSource = dataSource;
        this.statsRoute = statsRoute;

        LOG.info("Connecting to postgres using provided DataSource");
    }


    public String getStatsRoute() {
        return statsRoute;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void initRDKitTables() throws SQLException {

        if (!initialised) {

            LOG.info("Checking if table_info table is present");

            try (Connection con = dataSource.getConnection()) {
                try (Statement stmt = con.createStatement()) {
                    String sql = "SELECT count(*) FROM table_info";
                    LOG.info("SQL: " + sql);
                    stmt.execute(sql);
                    LOG.info("table_info table already exists");
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "Failed to select from TABLE_INFO");
                    LOG.info("Table table_info does not appear to exist. Creating it.");

                    String sql = "CREATE TABLE table_info (\n" +
                            "schema_name VARCHAR(64) NOT NULL,\n" +
                            "table_name VARCHAR(64) NOT NULL,\n" +
                            "class_name TEXT NOT NULL,\n" +
                            "CONSTRAINT pk_table_info PRIMARY KEY (schema_name,table_name)\n" +
                            ")";
                    LOG.info("SQL: " + sql);

                    try (Statement stmt = con.createStatement()) {
                        stmt.execute(sql);
                    }
                    LOG.info("table_info created");
                }
                initialised = true;
            }
        }
    }

    public RDKitTable getRDKitTable(String table) {
        // expand out the schema if not specified
        if (!table.contains(".")) {
            table = DEFAULT_SCHEMA + "." + table;
        }
        RDKitTable rdkt;
        // first look in the cached tables
        if (chemTables != null) {
            rdkt = chemTables.get(table);
            if (rdkt != null) {
                return rdkt;
            }
        }
        // if not found then try reading the tables
        try {
            rdkt = readRDKitTables().get(table);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read table definitions", e);
        }
        return rdkt;
    }

    /**
     * Get the cached set of table definitions if they are present, or read them from the database if not.
     *
     * @return
     * @throws SQLException
     */
    public Map<String, RDKitTable> getRDKitTables() {
        if (chemTables == null) {
            try {
                chemTables = readRDKitTables();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load table definitions", e);
            }
        }
        return chemTables;
    }

    /**
     * Read the table definitions from the database and updated the cache.
     *
     * @return
     * @throws SQLException
     */
    public Map<String, RDKitTable> readRDKitTables() throws SQLException {

        initRDKitTables();

        try (Connection con = dataSource.getConnection()) {
            try (Statement stmt = con.createStatement()) {
                stmt.execute("SELECT * FROM table_info");
                try (ResultSet rs = stmt.getResultSet()) {
                    Map<String, RDKitTable> map = new HashMap<>();
                    while (rs.next()) {
                        String schema = rs.getString("schema_name");
                        String table = rs.getString("table_name");
                        String className = rs.getString("class_name");
                        String schemaPlusTable = schema + "." + table;

                        try {
                            RDKitTable rdkt = createRDKitTable(schema, table, className);
                            map.put(schemaPlusTable, rdkt);
                            LOG.info("Discovered table " + schemaPlusTable);
                        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            LOG.log(Level.WARNING, "Invalid table definition for " + schemaPlusTable + " as " + className, e);
                        }
                    }
                    chemTables = map;
                    return map;
                }
            }
        }
    }

    public String[] getShortTableNames() {

        LOG.info("Getting short names");

        Set<String> longNames = getRDKitTables().keySet();
        List<String> shortNames = new ArrayList();
        for (String longName : longNames) {
            if (longName.startsWith(DEFAULT_SCHEMA + ".")) {
                String shortName = longName.substring(DEFAULT_SCHEMA.length() + 1, longName.length());
                shortNames.add(shortName);
                LOG.info("Found table " + longName + " -> " + shortName);
            }
        }
        return shortNames.toArray(new String[shortNames.size()]);
    }

    private RDKitTable createRDKitTable(String schema, String tableName, String className)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class cls = Class.forName(className);
        Constructor constr = cls.getConstructor(String.class, String.class);
        RDKitTable rdkt = (RDKitTable) constr.newInstance(schema, tableName);
        return rdkt;
    }

    /**
     * Add this table to the set of definitions stored in the database and clear the cache.
     *
     * @param schema
     * @param tableName
     * @throws SQLException
     */
    public void putRDKitTable(String schema, String tableName, Class className) throws SQLException {

        initRDKitTables();

        try (Connection con = dataSource.getConnection()) {
            doRemoveRDKitTable(con, schema, tableName);
            doPutRDKitTable(con, schema, tableName, className);
        }
        chemTables = null;
    }

    /**
     * Remove this table from the set of definitions stored in the database and clear the cache.
     *
     * @param schema
     * @param tableName
     * @throws SQLException
     */
    public void removeRDKitTable(String schema, String tableName) throws SQLException {

        initRDKitTables();

        try (Connection con = dataSource.getConnection()) {
            doRemoveRDKitTable(con, schema, tableName);
        }
        chemTables = null;
    }

    private void doRemoveRDKitTable(Connection con, String schema, String tableName) throws SQLException {
        String sql = "DELETE FROM table_info WHERE schema_name=? AND table_name=?";
        LOG.info("SQL: " + sql);
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            int count = stmt.executeUpdate();
            if (count == 1) {
                LOG.info("Existing table info for " + schema + "." + tableName + " deleted");
            }
        }
    }

    private void doPutRDKitTable(Connection con, String schema, String tableName, Class className) throws SQLException {
        String sql = "INSERT INTO table_info (schema_name, table_name, class_name) VALUES (?,?,?)";
        LOG.info("SQL: " + sql);
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            stmt.setString(3, className.getName());
            int count = stmt.executeUpdate();
            if (count == 1) {
                LOG.info("Table info for " + schema + "." + tableName + " added");
            }
        }
    }

}
