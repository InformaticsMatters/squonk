package org.squonk.camel.processor;

import com.im.lac.types.BasicObject;
import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.TypeConverterRegistry;
import org.squonk.api.GenericHandler;
import org.squonk.api.HttpHandler;
import org.squonk.camel.typeConverters.MoleculeStreamTypeConverter;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.http.CamelRequestResponseExecutor;
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

    protected final String route;
    protected final Class sdfClass;

    public static final String[] DEFAULT_INPUT_MIME_TYPES = new String[]{
            MIME_TYPE_DATASET_MOLECULE_JSON,
            MIME_TYPE_MDL_SDF};
    public static final String[] DEFAULT_OUTPUT_MIME_TYPES = new String[]{
            MIME_TYPE_DATASET_MOLECULE_JSON,
            MIME_TYPE_DATASET_BASIC_JSON,
            MIME_TYPE_MDL_SDF};

    public MoleculeObjectRouteHttpProcessor(String route, TypeResolver resolver) {
        this(route, resolver, DEFAULT_INPUT_MIME_TYPES, DEFAULT_OUTPUT_MIME_TYPES, null);
    }

    public MoleculeObjectRouteHttpProcessor(String route, TypeResolver resolver, Class<? extends SDFile> sdfClass) {
        this(route, resolver, DEFAULT_INPUT_MIME_TYPES, DEFAULT_OUTPUT_MIME_TYPES, sdfClass);
    }

    public MoleculeObjectRouteHttpProcessor(
            String route,
            TypeResolver resolver,
            String[] supportedInputMimeTypes,
            String[] supportedOutputMimeTypes,
            Class<? extends SDFile> sdfClass) {
        super(resolver, supportedInputMimeTypes, supportedOutputMimeTypes);
        this.route = route;
        this.sdfClass = sdfClass;
    }

    protected Object processDataset(
            Exchange exch,
            MoleculeObjectDataset dataset,
            RequestInfo requestInfo) throws IOException {

        // prepare it
        MoleculeObjectDataset processed = prepareDataset(dataset, requestInfo);

        // send the molecules to the route for processing and get back an updated set of molecules
        ProducerTemplate pt = exch.getContext().createProducerTemplate();
        processed = pt.requestBodyAndHeaders(route, processed, exch.getIn().getHeaders(), MoleculeObjectDataset.class);

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

//    public Dataset<MoleculeObject> fetchInput(Exchange exch, RequestInfo requestInfo, HttpHandler h) throws IOException {
//
//        CamelRequestResponseExecutor<Dataset, MoleculeObject> executor = new CamelRequestResponseExecutor(exch);
//
//        Object output = h.readResponse(executor, requestInfo.isGzipContent());
//        if (output == null) {
//            return null;
//        }
//        Dataset<? extends BasicObject> ds = null;
//        if (output instanceof Dataset) {
//            ds = (Dataset) output;
//        } else {
//            ds = exch.getContext().getTypeConverter().convertTo(Dataset.class, exch, output);
//        }
//        if (ds == null) {
//            return null;
//        } else if (ds.getType() == MoleculeObject.class) {
//            return (Dataset<MoleculeObject>) ds;
//        } else if (h instanceof GenericHandler) {
//            GenericHandler<Dataset, MoleculeObject> gh = (GenericHandler) h;
//            TypeConverterRegistry tcr = exch.getContext().getTypeConverterRegistry();
//            if (gh.canConvertGeneric(ds.getType(), tcr)) {
//                return gh.convertGeneric(ds, ds.getType(), tcr);
//            }
//        }
//        return null;
//    }
}
