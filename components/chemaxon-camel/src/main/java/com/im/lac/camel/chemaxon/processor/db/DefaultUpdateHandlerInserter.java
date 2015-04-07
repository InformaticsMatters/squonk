/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.camel.chemaxon.processor.db;

import chemaxon.jchem.db.UpdateHandler;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.struc.MPropertyContainer;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class DefaultUpdateHandlerInserter extends AbstractUpdateHandlerProcessor {
    
    private static final Logger LOG = Logger.getLogger(DefaultUpdateHandlerInserter.class.getName());

    private Map<String, Class> fieldDefs;

    public DefaultUpdateHandlerInserter(String table, String extraCols, Map<String, Class> fieldDefs) {
        super(UpdateHandler.INSERT, table, extraCols);
        this.fieldDefs = fieldDefs;
    }

    @Override
    protected void setValues(Exchange exchange, UpdateHandler updateHandler) throws SQLException {
        LOG.log(Level.FINER, "Processing Exchange {0}", exchange);
        MRecord record = exchange.getIn().getBody(MRecord.class);
        MPropertyContainer properties = record.getPropertyContainer();
        updateHandler.setStructure(record.getString());
        int i = 0;
        for (Map.Entry<String, Class> e : fieldDefs.entrySet()) {
            i++;
            Object val = MPropHandler.convertToString(properties, e.getKey());
            
            Object converted = exchange.getContext().getTypeConverter().convertTo(e.getValue(), val);
            LOG.log(Level.FINER, "Handling property {0} with value {1} converted to {2}", new Object[]{e.getKey(), val, converted});
            updateHandler.setValueForAdditionalColumn(i, converted);
        }
    }
}
