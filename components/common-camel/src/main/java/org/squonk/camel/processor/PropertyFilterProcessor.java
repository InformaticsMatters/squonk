package org.squonk.camel.processor;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.property.PropertyFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.squonk.util.CommonConstants.*;

/**
 * Allows to filter a dataset based on properties
 * <p>
 * Created by timbo on 29/05/16.
 */
public class PropertyFilterProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(PropertyFilterProcessor.class.getName());

    protected final List<PropertyFilter> filters = new ArrayList<>();
    protected final String resultPropertyName;


    public PropertyFilterProcessor(String resultPropertyName) {
        this.resultPropertyName = resultPropertyName;
    }

    public PropertyFilterProcessor filterInteger(String propName) {
        return filterInteger(propName, false, null, null);
    }


    public PropertyFilterProcessor filterInteger(String propName, boolean includeNull, Integer min, Integer max) {
        filters.add(new PropertyFilter.IntegerRangeFilter(propName, includeNull, min, max));
        return this;
    }

    public PropertyFilterProcessor filterDouble(String propName) {
        return filterDouble(propName, false, null, null);
    }

    public PropertyFilterProcessor filterDouble(String propName, boolean includeNull, Double min, Double max) {
        filters.add(new PropertyFilter.DoubleRangeFilter(propName, includeNull, min, max));
        return this;
    }

    @Override
    public void process(Exchange exch) throws Exception {
        Dataset<MoleculeObject> dataset = exch.getIn().getBody(Dataset.class);
        if (dataset == null) {
            throw new IllegalStateException("No dataset found");
        }
        Stream<MoleculeObject> mols = dataset.getStream();

        String filterMode = exch.getIn().getHeader(OPTION_FILTER_MODE, String.class);
        LOG.info("Filter mode: " + filterMode);
        boolean inverse = filterMode != null && VALUE_INCLUDE_FAIL.equals(filterMode.toUpperCase());
        boolean filter = filterMode == null || !VALUE_INCLUDE_ALL.equals(filterMode.toUpperCase());


        LOG.info("Configuring " + filters.size() + " filters");
        final List<PropertyFilter> filtersToUse = new ArrayList<>();
        for (PropertyFilter f : filters) {
            Object min = exch.getIn().getHeader(f.getPropertyName() + ".min", f.getDataType());
            Object max = exch.getIn().getHeader(f.getPropertyName() + ".max", f.getDataType());
            PropertyFilter d = f.derrive(min, max);
            if (d.isActive()) {
                LOG.fine("Adding filter " + d);
                filtersToUse.add(d);
            }
        }
        LOG.info("Generated " + filtersToUse.size() + " active filters");

        mols = mols.peek((mo) -> {
            //LOG.finest("Testing mol");
            int fails = 0;
            for (PropertyFilter f : filtersToUse) {
                if (!f.test(mo)) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Filter failed [" + f.toString() + "]: " + mo.getValue(f.getPropertyName()));
                    }
                    fails++;
                } else {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Filter passed [" + f.toString() + "]: " + mo.getValue(f.getPropertyName()));
                    }
                }
            }
            mo.putValue(resultPropertyName, fails);
            LOG.fine("Molecule had " + fails + " fails");
        });

        if (filter) {
            LOG.info("Adding filter " + filterMode);
            mols = mols.filter((mo) -> {
                int count = mo.getValue(resultPropertyName, Integer.class);
                return inverse ? count > 0 : count == 0;
            });
        }

        DatasetMetadata meta = dataset.getMetadata();
        if (meta == null) {
            meta = new DatasetMetadata(MoleculeObject.class);
        }
        meta.setSize(0);
        exch.getIn().setBody(new MoleculeObjectDataset(mols, meta));
    }
}
