package org.squonk.camel.processor;

import org.squonk.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.property.PropertyFilter;
import org.squonk.types.NumberRange;

import java.util.ArrayList;
import java.util.List;
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
        filters.add(new PropertyFilter(propName, includeNull, new NumberRange.Integer(min, max)));
        return this;
    }

    public PropertyFilterProcessor filterDouble(String propName) {
        return filterDouble(propName, false, null, null);
    }

    public PropertyFilterProcessor filterDouble(String propName, boolean includeNull, Double min, Double max) {
        filters.add(new PropertyFilter(propName, includeNull, new NumberRange.Double(min, max)));
        return this;
    }

    public PropertyFilterProcessor filterFloat(String propName) {
        return filterFloat(propName, false, null, null);
    }

    public PropertyFilterProcessor filterFloat(String propName, boolean includeNull, Float min, Float max) {
        filters.add(new PropertyFilter(propName, includeNull, new NumberRange.Float(min, max)));
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
        Integer filterThreshold = exch.getIn().getHeader(OPTION_FILTER_THRESHOLD, Integer.class);
        LOG.info("Filter mode: " + filterMode);
        boolean inverse = filterMode != null && VALUE_INCLUDE_FAIL.equals(filterMode.toUpperCase());
        boolean filter = filterMode == null || !VALUE_INCLUDE_ALL.equals(filterMode.toUpperCase());


        LOG.info("Configuring " + filters.size() + " filters");
        final List<PropertyFilter> filtersToUse = new ArrayList<>();
        String filterString = "";
        for (PropertyFilter f : filters) {
            String range = exch.getIn().getHeader(f.getPropertyName(), String.class);
            LOG.fine("Filtering for " + f.getPropertyName() + " is " + range);
            PropertyFilter d = null;
            if (range == null) {
                d = f;
            } else {
                d = f.derive(range);
            }
            if (d.isActive()) {
                LOG.fine("Adding filter " + d);
                filtersToUse.add(d);
                if (filterString.length() > 0) {
                    filterString += " && ";
                }
                filterString += d.asText();
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
            int threshold = (filterThreshold == null ? 0 : filterThreshold);
            LOG.info("Adding filter " + filterMode + " with threshold of " + threshold);
            mols = mols.filter((mo) -> {
                int count = mo.getValue(resultPropertyName, Integer.class);
                return inverse ? count > threshold : count <= threshold;
            });
        }

        DatasetMetadata meta = dataset.getMetadata();
        if (meta == null) {
            meta = new DatasetMetadata(MoleculeObject.class);
        }
        meta.setSize(-1);
        meta.createField(resultPropertyName, "Filter: " + filterString, "Property filter fail count", Integer.class);
        if (filter) {
            if (inverse) {
                meta.appendDatasetHistory("Filter rows where " + resultPropertyName + " > 0" );
            } else {
                meta.appendDatasetHistory("Filter rows where " + resultPropertyName + " = 0" );
            }
        }
        exch.getIn().setBody(new MoleculeObjectDataset(mols, meta));
    }
}
