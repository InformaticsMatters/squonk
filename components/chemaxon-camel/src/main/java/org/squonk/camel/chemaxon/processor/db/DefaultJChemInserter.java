package org.squonk.camel.chemaxon.processor.db;

import chemaxon.jchem.db.UpdateHandler;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.struc.MPropertyContainer;
import org.squonk.types.MoleculeObject;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;

/**
 *
 * @author timbo
 */
public class DefaultJChemInserter extends AbstractUpdateHandlerProcessor {

    private static final Logger LOG = Logger.getLogger(DefaultJChemInserter.class.getName());

    private final Map<String, Class> fieldDefs;

    public DefaultJChemInserter(String table, String extraCols, Map<String, Class> fieldDefs) {
        super(UpdateHandler.INSERT, table, extraCols);
        this.fieldDefs = fieldDefs;
    }

    @Override
    protected void setValues(Exchange exchange, UpdateHandler updateHandler) throws SQLException {
        LOG.log(Level.FINER, "Processing Exchange {0}", exchange);
        MRecord record = exchange.getIn().getBody(MRecord.class);
        if (record != null) {
            handleMRecord(exchange, updateHandler, record);
            return;
        }
        MoleculeObject mo = exchange.getIn().getBody(MoleculeObject.class);
        if (mo != null) {
            handleMoleculeObject(exchange, updateHandler, mo);
            return;
        }
        // what about chemaxon.struc.Molecule? Problem is which format to generate.

        // string only, no props
        String s = exchange.getIn().getBody(String.class);
        if (s != null) {
            updateHandler.setStructure(s);
            return;
        }
        throw new IllegalStateException("Can't handle data of type " + exchange.getIn().getBody().getClass().getName());
    }

    private void handleMRecord(Exchange exchange, UpdateHandler updateHandler, MRecord record) throws SQLException {
        MPropertyContainer properties = record.getPropertyContainer();
        updateHandler.setStructure(record.getString());
        int i = 1;
        TypeConverter converter = exchange.getContext().getTypeConverter();
        for (Map.Entry<String, Class> e : fieldDefs.entrySet()) {
            Object val = MPropHandler.convertToString(properties, e.getKey());
            handleProperty(updateHandler, i++, converter, val, e.getValue());
        }
    }

    private void handleMoleculeObject(Exchange exchange, UpdateHandler updateHandler, MoleculeObject mo) throws SQLException {
        updateHandler.setStructure(mo.getSource());
        Map<String, Object> values = mo.getValues();
        int i = 1;
        TypeConverter converter = exchange.getContext().getTypeConverter();
        for (Map.Entry<String, Class> e : fieldDefs.entrySet()) {
            Object val = values.get(e.getKey());
            handleProperty(updateHandler, i++, converter, val, e.getValue());
        }
    }

    private void handleProperty(UpdateHandler updateHandler, int index, TypeConverter converter, Object value, Class type) throws SQLException {
        if (value != null) {
            Object converted = converter.convertTo(type, value);
            //LOG.info("Handling property " + index + " -> " + value);
            updateHandler.setValueForAdditionalColumn(index, converted);
        } else {
            updateHandler.setValueForAdditionalColumn(index, null);
        }
    }
}
