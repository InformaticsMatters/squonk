package com.im.lac.camel.chemaxon.processor.db;

import chemaxon.enumeration.supergraph.SupergraphException;
import chemaxon.formats.MolExporter;
import chemaxon.jchem.db.DatabaseSearchException;
import chemaxon.jchem.db.JChemSearch;
import chemaxon.sss.SearchConstants;
import chemaxon.sss.search.JChemSearchOptions;
import chemaxon.sss.search.SearchException;
import chemaxon.struc.Molecule;
import chemaxon.util.ConnectionHandler;
import chemaxon.util.HitColoringAndAlignmentOptions;
import com.im.lac.camel.chemaxon.processor.ProcessorUtils;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.types.MoleculeObject;

import com.im.lac.util.CollectionUtils;
import com.im.lac.util.SimpleStreamProvider;
import com.im.lac.util.StreamProvider;
import com.squonk.dataset.MoleculeObjectDataset;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.sql.DataSource;
import org.apache.camel.Exchange;

/**
 * Processor that can execute a JChem search in a variety of ways, and with a
 * variety of outputs. Assumes the body contains the query structure as a String
 * (e.g. in molfile or smiles format, and the results are set to the body as
 * determined by the options.
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
 * In certain output modes the results are streamed providing the first results
 * as early as possible and can be passed through to other components like
 * ChemAxonMoleculeProcessor and StandardizerProcessor.
 * <p>
 * The following properties can be dynamically set at runtime using header
 * properties allowing a default route to be specified, but specific options set
 * as needed:
 * <br>
 * <b>searchOptions</b> overridden by the header with name of the
 * HEADER_SEARCH_OPTIONS constant which must have a String value.
 * <br>
 * <b>outputMode</b> overridden by the header with name of the
 * HEADER_OUTPUT_MODE constant which can have a value of the OutputMode enum or
 * its text value.
 * <br>
 * <b>outputColumns</b> overridden by the header with name of the
 * HEADER_OUTPUT_COLUMNS constant which must contain List<String> or a String of
 * comma separated column names.
 * <br>
 * <b>structureFormat</b> overridden by the header with name of the
 * HEADER_STRUCTURE_FORMAT constant which must have a String value.
 * <br>
 * <b>hitColorAndAlignOptions</b> overridden by the header with name of the
 * HEADER_HIT_COLOR_ALIGN_OPTIONS constant which must have a String value.
 * <br>
 *
 * @author Tim Dudgeon
 */
public class JChemDBSearcher extends AbstractJChemDBSearcher {

    private final static Logger LOG = Logger.getLogger(JChemDBSearcher.class.getName());

    public static final String HEADER_SEARCH_OPTIONS = "JChemSearchOptions";
    public static final String HEADER_OUTPUT_MODE = "JChemSearchOutputMode";
    public static final String HEADER_OUTPUT_COLUMNS = "JChemSearchOutputColumns";
    public static final String HEADER_STRUCTURE_FORMAT = "JChemSearchStructureFormat";
    public static final String HEADER_HIT_COLOR_ALIGN_OPTIONS = "JChemSearchHitColorAndAlignOptions";
    public static final String HEADER_SIMILARITY_SCORE_PROP_NAME = "SimilarityScorePropertyName";

    /**
     * The different types of output that can be generated.
     * <br>
     * RAW generates the raw int[] array returned by JChemSearch
     * <br>
     * CD_IDS generates an List&lt;Integer&gt; containing the CD_ID values
     * <br>
     * MOLECULES generates an StreamProvider&lt;Molecule&gt;with additional properties
     * added according the value of the outputColumns field. This is most
     * suitable if the results are to be passed to another ChemAxon component.
     * This format generally is streamed, with the first results being available
     * immediately.
     * <br>
     * MOLECULE_OBJECTS generates an StreamProvider&lt;MoleculeObject&gt;with additional 
     * properties added according the value of the outputColumns field. This is most
     * suitable if the results are to be passed to non-ChemAxon components.
     * This format generally is streamed, with the first results being available
     * immediately.
     * <br>
     * TEXT generates a String containing the structures (as if generated using
     * the MOLECULES options converted to a text in the format specified by the
     * structureFormat field (or overridden using the header property with the
     * name of the HEADER_STRUCTURE_FORMAT constant).
     * <br>
     * NOTE: this builds the entire String in memory so is only suitable for
     * small result sets.
     * <br>
     * STREAM allows the resulting structures to be read as an InputStream. This
     * is similar in nature to the TEXT option but suitable for large numbers of
     * structures. Typically SDF format would be used allowing results to be
     * passed into non-ChemAxon components or across remote interfaces. This
     * format generally is streamed, with the first results being available
     * immediately.
     *
     */
    public enum OutputMode {

        RAW,
        CD_IDS,
        MOLECULES,
        MOLECULE_OBJECTS,
        TEXT,
        STREAM
    }

    /**
     * The type of output that is generated. One of the values of the OutputMode
     * enum. Default is RAW as this is the cheapest in terms of processing time,
     * but leaves you with the most work to do. This value can be overriden at
     * runtime by the header property with the name of the HEADER_OUTPUT_MODE
     * constant. Default is RAW.
     *
     */
    private OutputMode outputMode = OutputMode.RAW;

    /**
     * The default output format that is used to generate textual output. This
     * value can be overriden at runtime by the header property with the name of
     * the HEADER_STRUCTURE_FORMAT constant. Default value is "sdf"
     *
     */
    private String structureFormat = "sdf";

    /**
     * The list of columns to retrieve This value can be overriden at runtime by
     * the header property with the name of the HEADER_OUTPUT_COLUMNS constant.
     *
     */
    private List<String> outputColumns = new ArrayList();

    /**
     * Options for hit alignment and coloring. If null then no alignment or
     * coloring. The effect of this option is very dependent on the structure
     * format being used. Coloring will only work for MRV format or for
     * MOLECULES output, alignment will also work for file formats that support
     * 2D coordinates, but not for smiles etc.
     *
     */
    private String hitColorAndAlignOptions;

    private String similarityScorePropertyName = "similarity";

    /**
     * The output mode. See the docs for the OutputMode enum for details.
     *
     * @param outputMode
     * @return This instance, allowing fluent builder pattern to be used.
     */
    public JChemDBSearcher outputMode(OutputMode outputMode) {
        this.outputMode = outputMode;
        return this;
    }

    /**
     * Include data for these database columns in the output. This only applies
     * to MOLECULES, STREAM and TEXT outputs.
     *
     * @param outputColumns
     * @return This instance, allowing fluent builder pattern to be used.
     */
    public JChemDBSearcher outputColumns(List<String> outputColumns) {
        this.outputColumns = outputColumns;
        return this;
    }

    /**
     * Add this column to the list of columns that are retrieved TODO: allow the
     * data type to be specified
     *
     * @param outputColumn
     * @return
     */
    public JChemDBSearcher outputColumn(String outputColumn) {
        this.outputColumns.add(outputColumn);
        return this;
    }

    /**
     * Set the name for the similarity score. Only used for similarity searches.
     * Default is "similarity". Can be overridden using the header of the name
     * of the HEADER_SIMILARITY_SCORE_PROP_NAME constant.
     *
     * @param propName
     * @return
     */
    public JChemDBSearcher similarityScorePropertyName(String propName) {
        this.similarityScorePropertyName = propName;
        return this;
    }

    /**
     * Specifies the default file format when using TEXT or STREAM as the output
     * mode. e.g. "smiles", "cxsmiles:a-H", "sdf". Default is "sdf"
     *
     * @param structureFormat The format for output in Chemaxon syntax
     * @return This instance, allowing fluent builder pattern to be used.
     * @see https://docs.chemaxon.com/display/FF/Molecule+Formats
     */
    public JChemDBSearcher structureFormat(String structureFormat) {
        this.structureFormat = structureFormat;
        return this;
    }

    /**
     * Options for hit alignment and coloring. e.g. "hitColoring:y align:r"
     *
     * @param hitColorAndAlignOptions
     * @return This instance, allowing fluent builder pattern to be used.
     * @see
     * https://www.chemaxon.com/jchem/doc/dev/java/api/chemaxon/util/HitColoringAndAlignmentOptions.html#setOptions(chemaxon.util.HitColoringAndAlignmentOptions,%20java.lang.String)
     */
    public JChemDBSearcher hitColorAndAlignOptions(String hitColorAndAlignOptions) {
        this.hitColorAndAlignOptions = hitColorAndAlignOptions;
        return this;
    }

    /**
     * Specify the name of the structure table to search. This parameter MUST be
     * specified. If necessary include the schema name e.g. "schema.table".
     *
     * @param structureTable
     * @return This instance, allowing fluent builder pattern to be used.
     */
    public JChemDBSearcher structureTable(String structureTable) {
        setStructureTable(structureTable);
        return this;
    }

    /**
     * These are the default search options and can be overridden using the
     * HEADER_SEARCH_OPTIONS header property allowing the same table to be
     * searched with other search types (e.g. this option might be substructure
     * as the default but you can change that (e.g. to similarity) using the
     * header property.
     *
     * @param searchOptions
     * @return This instance, allowing fluent builder pattern to be used.
     */
    public JChemDBSearcher searchOptions(String searchOptions) {
        setSearchOptions(searchOptions);
        return this;
    }

    /**
     * These options (if defined) are always set LAST so that they can be used
     * to override or limit searches. e.g use "maxResults:1000" to force a
     * restriction to the number of hits that are returned. This will override
     * dynamic settings (through the HEADER_SEARCH_OPTIONS header property) so
     * the user cannot override this.
     *
     * @param searchOptions
     * @return
     */
    public JChemDBSearcher searchOptionsOverride(String searchOptions) {
        setSearchOptionsOverride(searchOptions);
        return this;
    }

    /**
     * Specify the database as a javax.sql.DataSource. ONE of a dataSource,
     * connection or connectionHandler MUST be specified
     *
     * @param ds
     * @return This instance, allowing fluent builder pattern to be used.
     */
    public JChemDBSearcher dataSource(DataSource ds) {
        setDataSource(ds);
        return this;
    }

    /**
     * Specify the database as a java.sql.Connection. ONE of a dataSource,
     * connection or connectionHandler MUST be specified
     *
     * @param con
     * @return This instance, allowing fluent builder pattern to be used.
     */
    public JChemDBSearcher connection(Connection con) {
        setConnection(con);
        return this;
    }

    /**
     * Specify the database as a ConnectionHandler. ONE of a dataSource,
     * connection or connectionHandler MUST be specified
     *
     * @param conh
     * @return This instance, allowing fluent builder pattern to be used.
     */
    public JChemDBSearcher connectionHandler(ConnectionHandler conh) {
        setConnectionHandler(conh);
        return this;
    }

    /**
     * Specify the property table if it is not called JCHEMPROPERTIES and/or is
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

        String headerOpt = exchange.getIn().getHeader(HEADER_SEARCH_OPTIONS, String.class);
        if (headerOpt != null) {
            LOG.log(Level.INFO, "Using search options from header: {0}", headerOpt);
            JChemSearchOptions opts = new JChemSearchOptions(JChemSearch.SUBSTRUCTURE);
            opts.setOptions(headerOpt);
            jcs.setSearchOptions(opts);
        } else {
            super.handleSearchParams(exchange, jcs);
        }
    }

    private OutputMode determineOutputMode(Exchange exchange) {
        Object headerOpt = exchange.getIn().getHeader(HEADER_OUTPUT_MODE);
        if (headerOpt != null) {
            if (headerOpt instanceof OutputMode) {
                return (OutputMode) headerOpt;
            } else {
                return OutputMode.valueOf(headerOpt.toString());
            }
        } else {
            return this.outputMode;
        }
    }

    private List<String> determineOutputColumns(Exchange exchange) {
        Object headerOpt = exchange.getIn().getHeader(HEADER_OUTPUT_MODE);
        if (headerOpt != null) {
            if (headerOpt instanceof List) {
                return (List<String>) headerOpt;
            } else {
                String[] cols = headerOpt.toString().split(",");
                List<String> result = new ArrayList<>();
                for (String col : cols) {
                    String c = col.trim();
                    if (c.length() > 0) {
                        result.add(c);
                    }
                }
                return result;
            }
        } else {
            return this.outputColumns;
        }
    }

    private HitColoringAndAlignmentOptions determineHitColorAndAlignOptions(Exchange exchange) {
        String headerOpt = exchange.getIn().getHeader(HEADER_HIT_COLOR_ALIGN_OPTIONS, String.class);
        String opts;
        if (headerOpt != null) {
            opts = headerOpt;
        } else {
            opts = hitColorAndAlignOptions;
        }
        if (opts == null) {
            return null;
        } else {
            LOG.log(Level.INFO, "Using hit colour alignment options of {0}", opts);
            HitColoringAndAlignmentOptions hcao = new HitColoringAndAlignmentOptions();
            HitColoringAndAlignmentOptions.setOptions(hcao, opts);
            return hcao;
        }
    }

    @Override
    protected void startSearch(Exchange exchange, JChemSearch jcs) throws Exception {

        if (jcs.getSearchOptions().getSearchType() == SearchConstants.SIMILARITY) {
            // similarity search needs the entire set to be searched before it can provide
            // results so must run in sync mode
            jcs.setRunMode(JChemSearch.RUN_MODE_SYNCH_COMPLETE);
            jcs.setRunning(true);
        } else {
            switch (determineOutputMode(exchange)) {
                case STREAM:
                case MOLECULES:
                case MOLECULE_OBJECTS:
                    // ordering may need some attention in edge cases
                    jcs.setOrder(JChemSearch.NO_ORDERING);
                    jcs.setRunMode(JChemSearch.RUN_MODE_ASYNCH_PROGRESSIVE);
                    jcs.setRunning(true);
                    break;
                default:
                    super.startSearch(exchange, jcs);
            }
        }
    }

    @Override
    protected void handleSearchResults(Exchange exchange, JChemSearch jcs) throws Exception {
        OutputMode mode = determineOutputMode(exchange);
        switch (mode) {
            case RAW:
                int[] hits = jcs.getResults();
                exchange.getIn().setBody(hits);
                break;
            case CD_IDS:
                // TODO - stream this?
                exchange.getIn().setBody(getHitsAsList(jcs));
                break;
            case MOLECULES:
                handleAsMoleculeStream(exchange, jcs);
                break;
            case MOLECULE_OBJECTS:
                handleAsDataset(exchange, jcs);
                break;
            case TEXT:
                handleAsText(exchange, jcs);
                break;
            case STREAM:
                handleAsTextStream(exchange, jcs);
                break;
            default:
                throw new UnsupportedOperationException("Mode " + mode + " not yet supported");
        }
    }

    private List<Integer> getHitsAsList(JChemSearch jcs) {
        return CollectionUtils.asIntegerList(jcs.getResults());
    }

    /**
     * Create the molecules as text in the format specified by the
     * structureFormat property. Note: this is only suitable for relatively
     * small numbers of molecules. Use handleAsStream for large sets.
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
        int[] hits = jcs.getResults();
        float[] dissimilarities = getDissimilarities(jcs);
        Molecule[] mols = loadMoleculesFromDB(exchange, jcs, hits, dissimilarities, determineHitColorAndAlignOptions(exchange));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final MolExporter exporter = new MolExporter(out, ProcessorUtils.determineStringProperty(exchange, this.structureFormat, HEADER_STRUCTURE_FORMAT));
        try {
            ProcessorUtils.writeMoleculesToMolExporter(exporter, mols);
            exchange.getIn().setBody(out.toString());
        } finally {
            exporter.close();
        }
    }

    private void handleAsMoleculeStream(final Exchange exchange, final JChemSearch jcs)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {

        Stream<Molecule> molStream = createMoleculeStream(exchange, jcs);
        StreamProvider<Molecule> p = new SimpleStreamProvider(molStream, Molecule.class);
        exchange.getIn().setBody(p);
    }

    private void handleAsDataset(final Exchange exchange, final JChemSearch jcs)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {
        Stream<Molecule> molStream = createMoleculeStream(exchange, jcs);
        Stream<MoleculeObject> molObjStream = molStream.map(mol -> {
            try {
                return MoleculeUtils.createMoleculeObject(mol, structureFormat);
            } catch (IOException ex) {
                throw new RuntimeException("Unable to create MoleculeObject", ex);
            }
        });
        StreamProvider<MoleculeObject> p = new MoleculeObjectDataset(molObjStream);
        exchange.getIn().setBody(p);
    }

    private Stream<Molecule> createMoleculeStream(final Exchange exchange, final JChemSearch jcs) {
        HitColoringAndAlignmentOptions hcao = determineHitColorAndAlignOptions(exchange);

        Spliterator spliterator = jcs.getRunMode() == JChemSearch.RUN_MODE_ASYNCH_PROGRESSIVE
                ? new AsyncJChemSearchSpliterator(exchange, jcs, hcao) : new SyncJChemSearchSpliterator(exchange, jcs, hcao);
        return StreamSupport.stream(spliterator, false);
    }


    private void handleAsTextStream(final Exchange exchange, final JChemSearch jcs)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {

        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(pis);
        final MolExporter exporter = new MolExporter(out, ProcessorUtils.determineStringProperty(exchange, this.structureFormat, HEADER_STRUCTURE_FORMAT));

        final Stream<Molecule> stream = createMoleculeStream(exchange, jcs);
        new Thread(
                () -> {
                    try {
                        stream.forEachOrdered(mol -> {
                            try {
                                exporter.write(mol);
                            } catch (IOException ex) {
                                throw new RuntimeException("Failed to export Molecule", ex);
                            }
                        });
                    } finally {
                        try {
                            exporter.close();
                        } catch (IOException ex) {
                            LOG.log(Level.SEVERE, "Failed to close MolExporter", ex);
                        }
                    }
                }).start();

        exchange.getIn().setBody(pis);
    }

    private float[] getDissimilarities(JChemSearch jcs) {
        float[] dissimilarities = null;
        if (jcs.getSearchOptions().getSearchType() == SearchConstants.SIMILARITY) {
            // similarity search always run in sync mode
            dissimilarities = jcs.getDissimilarity();
        }
        return dissimilarities;
    }

    private Molecule[] loadMoleculesFromDB(Exchange exchange, JChemSearch jcs, int[] hits, float[] dissimilarities, HitColoringAndAlignmentOptions hcao)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {
        List<Object[]> props = new ArrayList<>();
        List<String> outCols = determineOutputColumns(exchange);
        Molecule[] mols = jcs.getHitsAsMolecules(hits, hcao, outCols, props);
        if (dissimilarities != null) {
            if (mols.length != dissimilarities.length) {
                LOG.warning("Number of scores and molecules do not correspond. Skipping adding similarity scores");
            } else {
                String simProp = ProcessorUtils.determineStringProperty(exchange, similarityScorePropertyName, HEADER_SIMILARITY_SCORE_PROP_NAME);
                for (int i = 0; i < mols.length; i++) {
                    float sim = 1.0f - dissimilarities[i];
                    // Marvin for some reason doesn't export values correctly if they are Float so we use Double 
                    mols[i].setPropertyObject(simProp, new Double(sim));
                }
            }
        }

        int i = 0;
        for (Molecule mol : mols) {
            Object[] vals = props.get(i);
            int j = 0;
            for (String col : outCols) {
                mol.setPropertyObject(col, vals[j]);
                j++;
            }
            i++;
        }
        return mols;
    }

    interface MoleculeWriter {

        void writeMolecules(Molecule[] mols);

        void close();
    }

    abstract class AbstractJChemSearchSpliterator extends Spliterators.AbstractSpliterator<Molecule> {

        protected final Exchange exchange;
        protected final JChemSearch jcs;
        protected final HitColoringAndAlignmentOptions hcao;
        protected Molecule[] mols;
        protected int index;
        protected int total = 0;

        AbstractJChemSearchSpliterator(Exchange exchange, JChemSearch jcs, HitColoringAndAlignmentOptions hcao) {
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
            this.exchange = exchange;
            this.jcs = jcs;
            this.hcao = hcao;
        }

        boolean tryNext(Consumer<? super Molecule> action) throws IOException {
            if (index >= mols.length) {
                return false;
            } else {
                action.accept(mols[index]);
                total++;
                index++;
                return true;
            }
        }
    }

    class AsyncJChemSearchSpliterator extends AbstractJChemSearchSpliterator {

        AsyncJChemSearchSpliterator(Exchange exchange, JChemSearch jcs, HitColoringAndAlignmentOptions hcao) {
            super(exchange, jcs, hcao);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Molecule> action) {
            try {
                if (mols == null || index >= mols.length) {
                    if (!readNextMols()) {
                        return false;
                    }
                }
                return tryNext(action);

            } catch (InterruptedException | DatabaseSearchException | SQLException | IOException | SearchException | SupergraphException e) {
                throw new RuntimeException("Failed to read search results", e);
            }
        }

        boolean readNextMols() throws InterruptedException, DatabaseSearchException, SQLException, IOException, SearchException, SupergraphException {
            index = 0;
            if (!jcs.hasMoreHits()) {
                return false;
            } else {
                int[] hits = jcs.getAvailableNewHits(50);
                // TODO - chunk this as list could be huge 
                LOG.log(Level.INFO, "Processing {0} async hits", hits.length);
                mols = loadMoleculesFromDB(exchange, jcs, hits, null, hcao);
                return true;
            }
        }
    }

    class SyncJChemSearchSpliterator extends AbstractJChemSearchSpliterator {

        SyncJChemSearchSpliterator(Exchange exchange, JChemSearch jcs, HitColoringAndAlignmentOptions hcao) {
            super(exchange, jcs, hcao);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Molecule> action) {
            try {
                if (mols == null) {
                    int[] hits = jcs.getResults();
                    if (hits.length == 0) {
                        return false;
                    } else {
                        LOG.log(Level.INFO, "Obtained {0} hits", hits.length);
                        // TODO - break into chunks
                        float[] dissimilarities = getDissimilarities(jcs);
                        mols = loadMoleculesFromDB(exchange, jcs, hits, dissimilarities, hcao);
                    }
                }
                return tryNext(action);

            } catch (DatabaseSearchException | SQLException | IOException | SearchException | SupergraphException e) {
                throw new RuntimeException("Failed to read search results", e);
            }
        }

    }
}
