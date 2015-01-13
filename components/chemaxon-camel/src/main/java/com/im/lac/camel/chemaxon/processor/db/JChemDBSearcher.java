package com.im.lac.camel.chemaxon.processor.db;

import chemaxon.enumeration.supergraph.SupergraphException;
import chemaxon.formats.MolExporter;
import chemaxon.jchem.db.DatabaseSearchException;
import chemaxon.jchem.db.JChemSearch;
import chemaxon.sss.search.JChemSearchOptions;
import chemaxon.sss.search.SearchException;
import chemaxon.struc.Molecule;
import chemaxon.util.ConnectionHandler;
import com.im.lac.ClosableQueue;
import com.im.lac.util.CollectionUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.camel.Exchange;

/** Processor that can execute a JChem search in a variety of ways, and with a variety 
 * of outputs. Assumes the body contains the query structure as a String (e.g. in 
 * molfile or smiles format, and the results are set to the body as determined by 
 * the options.
 * <p>
 * This processor supports the fluent builder approach, allowing it to be easily 
 * configured. An example of usage would be like this:
 * <br>
 * <code>
 * from("direct:dhfr/molecules")
 *     .convertBodyTo(String.class)
 *     .process(new JChemDBSearcher()
 *         .connection(con)
 *         .structureTable("dhfr")
 *         .searchOptions("t:s")
 *         .outputMode(JChemDBSearcher.OutputMode.MOLECULES)   
 *         .outputColumns(['mset','name'])
 *     );
 * </code>
 * <p>
 * In certain output modes the results are streamed providing the first results as
 * early as possible and can be passed through to other components like ChemAxonMoleculeProcessor
 * and StandardizerProcessor
 *
 * @author Tim Dudgeon
 */
public class JChemDBSearcher extends AbstractJChemDBSearcher {

    private final static Logger LOG = Logger.getLogger(JChemDBSearcher.class.getName());

    public static final String HEADER_SEARCH_OPTIONS = "JChemSearchOptions";

    /**
     * The different types of output that can be generated.
     * <br>
     * RAW generates the raw int[] arrray returned by JChemSearch
     * <br>
     * CD_IDS generates an Iterable<Integer> containing the CD_ID values
     * <br>
     * MOLECULES generates an Iterable<Molecule> with additional properties added
     * according the value of the outputColumns field. This is most suitable if the 
     * results are to be passed to another ChemAxon component. This format generally 
     * is streamed, with the first results being available immediately.
     * <br>
     * TEXT generates a String containing the structures (as if generated using
     * the MOLECULES options converted to a text in the format specified by the
     * outputFormat field. NOTE: this builds the entire String in memory so is
     * only suitable for small result sets.
     * <br>
     * STREAM allows the resulting structures to be read as an InputStream. 
     * This is similar in nature to the TEXT option but suitable for large numbers 
     * of structures. Typically SDF format would be used allowing results to be passed
     * into non-ChemAxon components or across remote interfaces. This format generally 
     * is streamed, with the first results being available immediately.
     *
     */
    public enum OutputMode {

        RAW,
        CD_IDS,
        MOLECULES,
        TEXT,
        STREAM
    }

    /**
     * The type of output that is generated. One of the values of the OutputMode
     * enum. Default is RAW as this is the cheapest in terms of processing time,
     * but leaves you with the most work to do.
     *
     */
    protected OutputMode outputMode = OutputMode.RAW;

   
    protected String outputFormat = "sdf";

    /** The list of columns to retrieve
     *
     */
    protected List<String> outputColumns = Collections.EMPTY_LIST;

    /** The output mode. See the docs for the OutputMode enum for details.
     * 
     * @param outputMode
     * @return This instance, allowing fluent builder pattern to be used. 
     */
    public JChemDBSearcher outputMode(OutputMode outputMode) {
        this.outputMode = outputMode;
        return this;
    }

    /** Include data for these database columns in the output. This only applies to
     * MOLECULES, STREAM and TEXT outputs.
     * 
     * @param outputColumns
     * @return This instance, allowing fluent builder pattern to be used. 
     */
    public JChemDBSearcher outputColumns(List<String> outputColumns) {
        this.outputColumns = outputColumns;
        return this;
    }

     /**
     * Specifies the file format when using TEXT or STREAM as the output mode. 
     * Default is "sdf"
     * 
     * @return This instance, allowing fluent builder pattern to be used. 
     * @see https://docs.chemaxon.com/display/FF/Molecule+Formats
     */
    public JChemDBSearcher outputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    /** Specify the name of the structure table to search. This parameter MUST be
     * specified. If necessary include the schema name e.g. "schema.table". 
     * 
     * @param structureTable
     * @return This instance, allowing fluent builder pattern to be used. 
     */
    public JChemDBSearcher structureTable(String structureTable) {
        setStructureTable(structureTable);
        return this;
    }

    /** These are the default search options and can be overridden using the 
     * HEADER_SEARCH_OPTIONS header property allowing the same table to be searched
     * with other search types (e.g. this option might be substructure as the default 
     * but you can change that (e.g. to similarity) using the header property. 
     * 
     * @param searchOptions
     * @return This instance, allowing fluent builder pattern to be used. 
     */
    public JChemDBSearcher searchOptions(String searchOptions) {
        setSearchOptions(searchOptions);
        return this;
    }

    /** Specify the database as a javax.sql.DataSource.
     * ONE of a dataSource, connection or connectionHandler MUST be specified
     * 
     * @param ds
     * @return This instance, allowing fluent builder pattern to be used. 
     */
    public JChemDBSearcher dataSource(DataSource ds) {
        setDataSource(ds);
        return this;
    }

    /** Specify the database as a java.sql.Connection.
     * ONE of a dataSource, connection or connectionHandler MUST be specified
     * 
     * @param con
     * @return This instance, allowing fluent builder pattern to be used. 
     */
    public JChemDBSearcher connection(Connection con) {
        setConnection(con);
        return this;
    }

    /** Specify the database as a ConnectionHandler.
     * ONE of a dataSource, connection or connectionHandler MUST be specified
     * 
     * @param conh
     * @return This instance, allowing fluent builder pattern to be used. 
     */
    public JChemDBSearcher connectionHandler(ConnectionHandler conh) {
        setConnectionHandler(conh);
        return this;
    }

    /** Specify the property table if it is not called JCHEMPROPERTIES and/or is 
     * not in the default schema
     * 
     * @param tableName
     * @return This instance, allowing fluent builder pattern to be used. 
     */
    public JChemDBSearcher propertyTable(String tableName) {
        setPropertyTable(tableName);
        return this;
    }

    /**
     * Sets the search options based on what is specified in the
     * HEADER_SEARCH_OPTIONS if present, or the defaults provided by the
     * searchOptions property if not.
     *
     * @param exchange the Camel Exchange.
     * @param jcs the JChemSearch instance
     */
    @Override
    protected void handleSearchParams(Exchange exchange, JChemSearch jcs) {

        String headerOpts = exchange.getIn().getHeader(HEADER_SEARCH_OPTIONS, String.class);
        if (headerOpts != null) {
            LOG.log(Level.INFO, "Using search options from header: {0}", headerOpts);
            JChemSearchOptions opts = new JChemSearchOptions(JChemSearch.SUBSTRUCTURE);
            opts.setOptions(headerOpts);
            jcs.setSearchOptions(opts);
        } else {
            super.handleSearchParams(exchange, jcs);
        }
    }

    @Override
    protected void startSearch(JChemSearch jcs) throws Exception {

        switch (outputMode) {
            case STREAM:
            case MOLECULES:
                jcs.setOrder(JChemSearch.NO_ORDERING);
                jcs.setRunMode(JChemSearch.RUN_MODE_ASYNCH_PROGRESSIVE);
                jcs.setRunning(true);
                break;
            default:
                super.startSearch(jcs);
        }
    }

    @Override
    protected void handleSearchResults(Exchange exchange, JChemSearch jcs) throws Exception {
        // TOOD - investigate stream the results in more detail - various complications here, 
        // such as similarity search not supporting asynch mode 
        switch (outputMode) {
            case RAW:
                int[] hits = jcs.getResults();
                exchange.getOut().setBody(hits);
                break;
            case CD_IDS:
                // TODO - stream this
                exchange.getOut().setBody(getHitsAsList(jcs));
                break;
            case MOLECULES:
                handleAsMoleculeStream(exchange, jcs);
                break;
            case TEXT:
                handleAsText(exchange, jcs);
                break;
            case STREAM:
                handleAsTextStream(exchange, jcs);
                break;
            default:
                throw new UnsupportedOperationException("Mode " + outputMode + " not yet supported");
        }
    }

    private List<Integer> getHitsAsList(JChemSearch jcs) {
        return CollectionUtils.asIntegerList(jcs.getResults());
    }

    /** Create the molecules as text in the format specified by the outputFormat
     * property. Note: this is only suitable for relatively small numbers of
     * molecules. Use handleAsStream for large sets.
     *
     * @param exchange
     * @param jcs
     * @throws SQLException
     * @throws IOException
     * @throws SearchException
     * @throws SupergraphException
     * @throws PropertyNotSetException
     * @throws DatabaseSearchException
     */
    private void handleAsText(final Exchange exchange, final JChemSearch jcs)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {
        final Molecule[] mols = jcs.getHitsAsMolecules(jcs.getResults(), null, outputColumns, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final MolExporter exporter = new MolExporter(out, outputFormat);
        try {
            writeMoleculesToMolExporter(exporter, mols);
            exchange.getOut().setBody(out.toString());
        } finally {
            exporter.close();
        }
    }

    private void handleAsMoleculeStream(final Exchange exchange, final JChemSearch jcs)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {

        LOG.info("handleAsMoleculeStream");

        final ClosableQueue q = new ClosableQueue(100);

        writeMoleculeStream(jcs, new MoleculeWriter() {
            @Override
            public void writeMolecules(Molecule[] mols) {
                try {
                    writeMoleculesToQueue(q, mols);
                } catch (IOException ex) {
                    // TODO - how to handle?
                    LOG.log(Level.SEVERE, "Failed to write molecules", ex);
                }
            }

            @Override
            public void close() {
                q.close();
            }
        });
        exchange.getIn().setBody(q);
    }

    private void handleAsTextStream(final Exchange exchange, final JChemSearch jcs)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {

        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(pis);
        final MolExporter exporter = new MolExporter(out, outputFormat);

        writeMoleculeStream(jcs, new MoleculeWriter() {
            @Override
            public void writeMolecules(Molecule[] mols) {
                try {
                    writeMoleculesToMolExporter(exporter, mols);
                } catch (IOException ex) {
                    // TODO - how to handle?
                    LOG.log(Level.SEVERE, "Failed to write molecules", ex);
                }
            }

            @Override
            public void close() {
                try {
                    exporter.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Failed to close MolExporter", ex);
                }
            }
        });
        exchange.getIn().setBody(pis);
    }

    private void writeMoleculeStream(final JChemSearch jcs, final MoleculeWriter molWriter)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (jcs.getRunMode() == JChemSearch.RUN_MODE_ASYNCH_PROGRESSIVE) {
                                // async mode - this will be the norm
                                while (jcs.hasMoreHits()) {
                                    int[] hits = jcs.getAvailableNewHits(1);
                                    // TODO - chunk this as list could be huge 
                                    LOG.log(Level.FINER, "Processing {0} hits", hits.length);
                                    List<Object[]> props = new ArrayList<Object[]>();
                                    Molecule[] mols = jcs.getHitsAsMolecules(hits, null, outputColumns, props);
                                    
                                    int i = 0;
                                    for (Molecule mol: mols) {
                                        Object[] vals = props.get(i);
                                        int j = 0;
                                        for (String col : outputColumns) {
                                            mol.setPropertyObject(col, vals[j]);
                                            j++;
                                        }
                                        i++;
                                    }
                                    molWriter.writeMolecules(mols);
                                }
                            } else {
                                // just in case we also handle sync mode
                                // TODO - break into chunks
                                Molecule[] mols = jcs.getHitsAsMolecules(jcs.getResults(), null, outputColumns, null);
                                LOG.log(Level.FINER, "Processing {0} hits", mols.length);
                                molWriter.writeMolecules(mols);
                            }
                            //} catch (InterruptedException | DatabaseSearchException | SQLException | IOException | SearchException | SupergraphException e) {
                        } catch (Exception e) {
                            LOG.log(Level.SEVERE, "Error writing molecules", e);
                        } finally {
                            molWriter.close();
                        }
                    }

                }
        ).start();
    }

    private void writeMoleculesToMolExporter(final MolExporter exporter, final Molecule[] mols) throws IOException {
        for (Molecule mol : mols) {
            exporter.write(mol);
        }
    }

    private void writeMoleculesToQueue(final ClosableQueue q, final Molecule[] mols) throws IOException {
        for (Molecule mol : mols) {
            q.add(mol);
        }
    }

    interface MoleculeWriter {
        void writeMolecules(Molecule[] mols);
        void close();
    }
}
