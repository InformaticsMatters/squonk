package org.squonk.camel.processor;

import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.http.RequestInfo;
import org.squonk.types.SDFile;
import org.squonk.types.TypeResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.squonk.types.TypeResolver.*;

/**
 * Created by timbo on 26/03/2016.
 */
public class MoleculeObjectRouteHttpProcessor extends AbstractMoleculeObjectHttpProcessor {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectRouteHttpProcessor.class.getName());

    public static final String[] DEFAULT_INPUT_MIME_TYPES = new String[]{
            MIME_TYPE_DATASET_MOLECULE_JSON,
            MIME_TYPE_MDL_SDF};
    public static final String[] DEFAULT_OUTPUT_MIME_TYPES = new String[]{
            MIME_TYPE_DATASET_MOLECULE_JSON,
            MIME_TYPE_DATASET_BASIC_JSON,
            MIME_TYPE_MDL_SDF};

    protected final String routeUri;
    protected final Class sdfClass;


    public MoleculeObjectRouteHttpProcessor(String route, TypeResolver resolver) {
        this(route, resolver, DEFAULT_INPUT_MIME_TYPES, DEFAULT_OUTPUT_MIME_TYPES, null, null);
    }

    public MoleculeObjectRouteHttpProcessor(String route, TypeResolver resolver, String statsRouteUri) {
        this(route, resolver, DEFAULT_INPUT_MIME_TYPES, DEFAULT_OUTPUT_MIME_TYPES, statsRouteUri, null);
    }

    public MoleculeObjectRouteHttpProcessor(String route, TypeResolver resolver, String statsRouteUri, Class<? extends SDFile> sdfClass) {
        this(route, resolver, DEFAULT_INPUT_MIME_TYPES, DEFAULT_OUTPUT_MIME_TYPES, statsRouteUri, sdfClass);
    }

    public MoleculeObjectRouteHttpProcessor(
            String routeUri,
            TypeResolver resolver,
            String[] supportedInputMimeTypes,
            String[] supportedOutputMimeTypes,
            String statsRouteUri,
            Class<? extends SDFile> sdfClass) {
        super(resolver, supportedInputMimeTypes, supportedOutputMimeTypes, statsRouteUri);
        this.routeUri = routeUri;
        this.sdfClass = sdfClass;
    }

    @Override
    protected Object processDataset(
            Exchange exch,
            MoleculeObjectDataset dataset,
            RequestInfo requestInfo) throws IOException {

        // prepare it
        MoleculeObjectDataset processed = prepareDataset(dataset, requestInfo);

        if (routeUri != null) {
            // send the molecules to the route for processing and get back an updated set of molecules
            ProducerTemplate pt = exch.getContext().createProducerTemplate();
            processed = pt.requestBodyAndHeaders(routeUri, processed, exch.getIn().getHeaders(), MoleculeObjectDataset.class);
        } else {
            LOG.info("No route URL provided - just doing format conversion");
        }

        // generate the results of the required type
        Object converted = generateOutput(exch, processed, requestInfo);

        return converted;
    }


    protected MoleculeObjectDataset prepareDataset(MoleculeObjectDataset mods, RequestInfo requestInfo) throws IOException {
        if (requestInfo.getAcceptType().equalsIgnoreCase(MIME_TYPE_DATASET_BASIC_JSON)) {
            // thin service so strip out everything except the molecule
            Dataset<MoleculeObject> dataset = mods.getDataset();
            Stream<MoleculeObject> mols = dataset.getStream().map((mo) -> new MoleculeObject(mo.getUUID(), mo.getSource(), mo.getFormat()));
            DatasetMetadata oldMeta = dataset.getMetadata();
            DatasetMetadata newMeta = new DatasetMetadata(MoleculeObject.class, Collections.emptyMap(),
                    oldMeta == null ? 0 : oldMeta.getSize(),
                    oldMeta == null ? Collections.emptyMap() : oldMeta.getProperties());
            return new MoleculeObjectDataset(new Dataset<>(MoleculeObject.class, mols, newMeta));
        } else {
            return mods;
        }
    }

    protected Object generateOutput(Exchange exch, MoleculeObjectDataset results, RequestInfo requestInfo)
            throws IOException {

        if (requestInfo.getAcceptType().equalsIgnoreCase(MIME_TYPE_DATASET_MOLECULE_JSON)) {
            // good old thick service that consumes everything and returns everything plus the added stuff
            return results.getDataset();

        } else if (requestInfo.getAcceptType().equalsIgnoreCase(MIME_TYPE_DATASET_BASIC_JSON)) {
            // thin service that returns the uuid and only the data that has been generated
            Stream<BasicObject> st = results.getStream().map((mo) -> {
                return new BasicObject(mo.getUUID(), mo.getValues());
            });
            return new Dataset<>(BasicObject.class, st);

        } else if (requestInfo.getAcceptType().equalsIgnoreCase(MIME_TYPE_MDL_SDF)) {
            // handle as SDF
            // this relies on a type converter being registered
            Class<SDFile> cls = (sdfClass == null ? SDFile.class : sdfClass);
            LOG.warning("Converting to cls " + cls);
            SDFile sdf = exch.getContext().getTypeConverter().convertTo(cls, exch, results);
            if (sdf == null) {
                throw new IllegalStateException("No type converter registered for converting to SDF?");
            }
            return sdf;
        } else {
            throw new IllegalStateException("Don't know how to generate output of type " + requestInfo.getAcceptType());
        }
    }

}
