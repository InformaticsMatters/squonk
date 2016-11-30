package org.squonk.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.squonk.api.HttpHandler;
import org.squonk.camel.typeConverters.MoleculeStreamTypeConverter;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.http.CamelRequestResponseExecutor;
import org.squonk.http.RequestInfo;
import org.squonk.types.TypeResolver;
import org.squonk.util.CamelRouteStatsRecorder;
import org.squonk.util.StatsRecorder;

import java.io.IOException;
import java.util.logging.Logger;

import static org.squonk.util.CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
import static org.squonk.util.CommonMimeTypes.MIME_TYPE_MDL_SDF;

/**
 * Created by timbo on 30/03/16.
 */
public abstract class AbstractMoleculeObjectHttpProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectRouteHttpProcessor.class.getName());

    public static final String[] DEFAULT_INPUT_MIME_TYPES = new String[]{
            MIME_TYPE_DATASET_MOLECULE_JSON,
            MIME_TYPE_MDL_SDF};

    protected final TypeResolver resolver;

    protected final String[] supportedInputMimeTypes;
    protected final String[] supportedOutputMimeTypes;

    /**
     * where to route the execution stats to
     */
    private final String statsRouteUri;


    public AbstractMoleculeObjectHttpProcessor(
            TypeResolver resolver,
            String[] supportedInputMimeTypes,
            String[] supportedOutputMimeTypes,
            String statsRouteUri) {
        this.resolver = resolver;
        this.supportedInputMimeTypes = supportedInputMimeTypes;
        this.supportedOutputMimeTypes = supportedOutputMimeTypes;
        this.statsRouteUri = statsRouteUri;
    }

    public AbstractMoleculeObjectHttpProcessor(
            TypeResolver resolver,
            String[] supportedInputMimeTypes,
            String[] supportedOutputMimeTypes) {
        this(resolver, supportedInputMimeTypes, supportedOutputMimeTypes, null);
    }


    @Override
    public void process(Exchange exch) throws Exception {

        // get all the info about the requested input and output
        RequestInfo requestInfo = RequestInfo.build(supportedInputMimeTypes, supportedOutputMimeTypes, exch);
        LOG.info(requestInfo.toString());

        // work out what handlers are needed for the input and output
        // might as well fail now if we can't handle input or output
        HttpHandler contentHandler = resolver.createHttpHandler(requestInfo.getContentType());
        if (contentHandler == null) {
            throw new IllegalStateException("No HttpHandler for content mime type " + requestInfo.getContentType());
        }

        HttpHandler acceptHandler;
        if (requestInfo.getContentType().equals(requestInfo.getAcceptType())) {
            acceptHandler = contentHandler;
        } else {
            acceptHandler = resolver.createHttpHandler(requestInfo.getAcceptType());
            if (acceptHandler == null) {
                throw new IllegalStateException("No HttpHandler for accept mime type " + requestInfo.getContentType());
            }
        }
        LOG.info("Using accept handler of " + acceptHandler.getClass().getName() + " for mime type " + requestInfo.getAcceptType());

        CamelRequestResponseExecutor executor = new CamelRequestResponseExecutor(exch);

        MoleculeObjectDataset dataset = readInput(exch, requestInfo, contentHandler, executor);

        String jobId = exch.getIn().getHeader(StatsRecorder.HEADER_SQUONK_JOB_ID, String.class);
        if (statsRouteUri != null) {
            ProducerTemplate pt = exch.getContext().createProducerTemplate();
            pt.setDefaultEndpointUri(statsRouteUri);
            exch.getIn().setHeader(StatsRecorder.HEADER_STATS_RECORDER, new CamelRouteStatsRecorder(jobId, pt));
        }

        Object results = processDataset(exch, dataset, requestInfo);

        writeOutput(exch, results, requestInfo,  acceptHandler, executor);

    }

    protected abstract Object processDataset(
            Exchange exch,
            MoleculeObjectDataset dataset,
            RequestInfo requestInfo) throws IOException;

    protected MoleculeObjectDataset readInput(Exchange exch, RequestInfo requestInfo, HttpHandler contentHandler, CamelRequestResponseExecutor executor) throws IOException {
        Object inputData = contentHandler.readResponse(executor, requestInfo.isGzipContent());
        MoleculeObjectDataset dataset = null;
        if (inputData instanceof MoleculeObjectDataset) {
            dataset = (MoleculeObjectDataset)inputData;
        } else if (inputData instanceof Dataset) {
            // this will return null if the dataset if not of type MoleculeObject
            dataset = MoleculeStreamTypeConverter.convertMoleculeObjectDatasetToDataset((Dataset)inputData, exch);
        }
        if (dataset == null) {
            dataset = exch.getContext().getTypeConverter().convertTo(MoleculeObjectDataset.class, exch, inputData);
        }
        if (dataset == null) {
            throw new IllegalStateException("Cannot generate MoleculeObjectDataset from " + inputData.getClass().getName());
        }
        return dataset;
    }

    protected void writeOutput(Exchange exch, Object output, RequestInfo requestInfo,  HttpHandler acceptHandler, CamelRequestResponseExecutor executor) throws IOException {
        Message msg = exch.getOut(); // get the out message. it now exists and result will be written to it.
        msg.setHeader("Content-Type", requestInfo.getAcceptType());
        if (requestInfo.isGzipAccept()) {
            msg.setHeader("Content-Encoding", "gzip");
        }
        acceptHandler.writeResponse(output, executor, false); // setting the Content-Encoding header handles compression
    }
}
