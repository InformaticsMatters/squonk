package org.squonk.camel.processor;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import static org.squonk.util.CommonConstants.*;

import java.util.logging.Logger;
import java.util.stream.Stream;

/** Base class for a Processor that verifies whether structures are valid.
 * Specific toolkits can implement by overriding the @{link #validateMolecule(MoleculeObject)} method.
 * Headers values:
 * <ul>
 *    <li><b><mode</b>: one of INCLUDE_PASS, INCLUDE_FAIL or INCLUDE_ALL to indicate whether to filter and if so which
 *    results to return</li>
 * </ul>
 * If not filtering a boolean field is added with the name specified by the fieldName property to indicate whether the
 * structure is valid or not.
 *
 * TODO: provide a way for implementations to signify WHY the structure is invalid
 *
 * Created by timbo on 28/05/16.
 */
public abstract class VerifyStructureProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(VerifyStructureProcessor.class.getName());

    protected final String fieldName;


    public VerifyStructureProcessor(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public void process(Exchange exch) throws Exception {
        Dataset<MoleculeObject> dataset = exch.getIn().getBody(Dataset.class);
        if (dataset == null) {
            Object o = exch.getIn().getBody();
            throw new IllegalArgumentException("Body was " + o.getClass().getName() + " not a Dataset");
        }

        final String filterMode;
        String filterModeOpt = exch.getIn().getHeader(OPTION_FILTER_MODE, String.class);
        if (filterModeOpt == null) {
            filterMode = VALUE_INCLUDE_FAIL;
        } else {
            filterMode = filterModeOpt.toUpperCase();
            if (!VALUE_INCLUDE_ALL.equals(filterMode) && !VALUE_INCLUDE_PASS.equals(filterMode) && !VALUE_INCLUDE_FAIL.equals(filterMode)) {
                throw new IllegalArgumentException("Unsupported filter mode: " + filterMode);
            }
        }

        boolean filter = filterMode == null || !VALUE_INCLUDE_ALL.equals(filterMode.toUpperCase());

        Stream<MoleculeObject> results = dataset.getStream().peek((mo) -> {
            mo.putValue(fieldName, validateMolecule(mo));
        });

        DatasetMetadata meta = dataset.getMetadata();
        if (meta != null) {
            meta.getValueClassMappings().put(fieldName, Boolean.class);
        }
        if (filter) {
            results = results.filter((o) -> {
                boolean b = o.getValue(fieldName, Boolean.class);
                if (!VALUE_INCLUDE_ALL.equals(filterMode)) {
                    o.getValues().remove(fieldName);
                }
                if (VALUE_INCLUDE_PASS.equals(filterMode.toUpperCase())) {
                    return b;
                } else if (VALUE_INCLUDE_FAIL.equals(filterMode.toUpperCase())) {
                    return !b;
                } else {
                    // should never happen
                    return false;
                }
            });
            if (meta != null) {
                meta.setSize(0);
            }
        }
        MoleculeObjectDataset output = new MoleculeObjectDataset(results, meta);
        exch.getIn().setBody(output);
    }

    /** Override with the structure verify function
     *
     * @param mo
     * @return
     */
    protected abstract boolean validateMolecule(MoleculeObject mo);
}
