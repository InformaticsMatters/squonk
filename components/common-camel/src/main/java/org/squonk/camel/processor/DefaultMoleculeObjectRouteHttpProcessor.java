package org.squonk.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.http.RequestInfo;
import org.squonk.types.TypeResolver;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * HttpProcessor that reads molecules, sends to a route for processing and gets something back. Does not do anything to
 * the results so that they are handled by whatever VariableHandler is appropriate.
 *
 * Created by timbo on 24/10/16.
 */
public class DefaultMoleculeObjectRouteHttpProcessor extends AbstractMoleculeObjectRouteHttpProcessor {

    private static final Logger LOG = Logger.getLogger(DefaultMoleculeObjectRouteHttpProcessor.class.getName());

    private boolean isThin;

    public DefaultMoleculeObjectRouteHttpProcessor(
            String routeUri,
            TypeResolver resolver,
            String[] outputMimeTypes,
            String statsRouteUri,
            boolean isThin) {
        super(routeUri, resolver, DEFAULT_INPUT_MIME_TYPES, outputMimeTypes, statsRouteUri);
        this.isThin = isThin;
    }


    @Override
    protected boolean isThin(RequestInfo requestInfo) {
        return isThin;
    }

    @Override
    protected Object processDataset(
            Exchange exch,
            MoleculeObjectDataset dataset,
            RequestInfo requestInfo) throws IOException {

        // prepare molecule input
        MoleculeObjectDataset input = prepareDataset(dataset, isThin(requestInfo));

        Object results;
        if (routeUri != null) {
            // send the molecules to the route for processing and get back the results
            ProducerTemplate pt = exch.getContext().createProducerTemplate();
            results = pt.requestBodyAndHeaders(routeUri, input, exch.getIn().getHeaders());

        } else {
            LOG.info("No route URL provided - just doing format conversion");
            results = input;
        }

        // generate the results of the required type
        return generateOutput(exch, results, requestInfo);
    }

    /**
     * Default is to do nothing and return the results unmodified. Override if something different is needed
     *
     * @param exch
     * @param results
     * @param requestInfo
     * @return
     * @throws IOException
     */
    protected Object generateOutput(Exchange exch, Object results, RequestInfo requestInfo)
            throws IOException {

        return results;
    }

}
