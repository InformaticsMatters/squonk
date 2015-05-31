/**
 * Processor that handles inserting and updating JChemBase tables. This is built
 * around an UpdateHandler. This class is abstract and the key parts are handled
 * by callbacks that you must implement in your subclass. You must implement the
 * void setValues(Exchange exchange, UpdateHandler updateHandler) method so that
 * the values from the Exchange are processed and set to the UpdateHandler
 * before it is executed. You can optionally implement the void
 * extractValues(Exchange exchange, UpdateHandler updateHandler) method which is
 * called after the UpdateHandler is executed. This gives you a chance to
 * extract values from the UpdateHandler (e.g. the inserted CD_ID) and set it to
 * the Exchange in the way you want. By default this method does nothing. You
 * can optionally implement the void configure(UpdateHandler uh) method which is
 * called straight after the UpdateHandler is created so that you can set
 * configure it before it is used for inserts or updates.
 *
 * A minimalist implementation could look like this:  <code>
 * Processor p = new AbstractUpdateHandlerProcessor(UpdateHandler.INSERT, 'TEST', null) {
 *
 * @Override
 * protected void setValues(Exchange exchange, UpdateHandler updateHandler) {
 *     String mol = exchange.in.getBody(String.class);
 *     updateHandler.setStructure(mol);
 * }
 * };
 * </code>
 *
 * The class is a Camel Service so understands about the Camel lifecycle. The
 * UpdateHandler is created when Camel start()s and is cleaned up when Camel
 * stop()s.
 *
 *
 * Status: experimental
 *
 */
package com.im.lac.camel.chemaxon.processor.db;

import chemaxon.jchem.db.UpdateHandler;
import com.im.lac.chemaxon.db.ConnectionHandlerSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Service;

/**
 * Created by timbo on 26/04/2014.
 */
public abstract class AbstractUpdateHandlerProcessor extends ConnectionHandlerSupport implements Service, Processor {

    private static final Logger LOG = Logger.getLogger(AbstractUpdateHandlerProcessor.class.getName());

    private UpdateHandler updateHandler;

    protected int mode;
    protected String tableName;
    protected String additionalColumns;
    private int executionCount = 0;
    private int errorCount = 0;

    public AbstractUpdateHandlerProcessor(int mode, String tableName, String additionalColumns) {
        this.mode = mode;
        this.tableName = tableName;
        this.additionalColumns = additionalColumns;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    @Override
    public void start() throws Exception {
        LOG.log(Level.INFO, "Starting AbstractUpdateHandlerProcessor {0}", this.toString());
        super.start();
        updateHandler = createUpdateHandler();
    }

    private UpdateHandler createUpdateHandler() throws SQLException {
        LOG.log(Level.INFO, "Creating UpdateHandler for table {0}", tableName);
        UpdateHandler uh = new UpdateHandler(getConnectionHandler(), mode, tableName, additionalColumns);
        configure(uh);
        return uh;
    }

    @Override
    public void stop() throws Exception {
        LOG.log(Level.INFO, "Stopping AbstractUpdateHandlerProcessor {0}", this.toString());

        if (updateHandler != null) {
            updateHandler.close();
        }
        super.stop();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.finer("process()");
        setValues(exchange, updateHandler);
        execute(exchange);
    }

    protected void execute(Exchange exchange) throws SQLException {
        try {
            executionCount++;
            int cdid = updateHandler.execute(true);
            extractValues(exchange, updateHandler, cdid);
        } catch (SQLException e) {
            errorCount++;
            throw e;
        }
    }

    /**
     * Call back to allow the compound_structures to be configured once it is
     * created. Default is to do nothing.
     *
     * @param uh The UpdateHandler to configure
     */
    protected void configure(UpdateHandler uh) {
        // noop
    }

    /**
     * Call back to allow the values to be set to the UpdateHandler before being
     * executed. Typically the structure and maybe other properties will be
     * extracted from the body and set to the UpdateHandler.
     *
     * @param exchange
     * @param updateHandler The UpdateHandler to configure
     * @throws java.sql.SQLException
     */
    protected abstract void setValues(Exchange exchange, UpdateHandler updateHandler) throws SQLException;

    /**
     * Callback to allow values to be extracted from the UpdateHandler after the
     * structure is inserted. Typically used to handle the generated CD_ID
     * value. Default is to do nothing.
     *
     * @param exchange
     * @param updateHandler
     * @param cdid The generated value for the CD_ID column. Value will be negative 
     * if duplicate filtering is in place and a the structure is already present
     */
    protected void extractValues(Exchange exchange, UpdateHandler updateHandler, int cdid) {

    }

}
