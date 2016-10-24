package org.squonk.camel.processor;

import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.http.RequestInfo;
import org.squonk.types.MoleculeObject;
import org.squonk.types.TypeResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * <p>
 * Created by timbo on 26/03/2016.
 */
public abstract class AbstractMoleculeObjectRouteHttpProcessor extends AbstractMoleculeObjectHttpProcessor {

    private static final Logger LOG = Logger.getLogger(AbstractMoleculeObjectRouteHttpProcessor.class.getName());


    protected final String routeUri;


    public AbstractMoleculeObjectRouteHttpProcessor(
            String routeUri,
            TypeResolver resolver,
            String[] inputMimeTypes,
            String[] outputMimeTypes,
            String statsRouteUri) {
        super(resolver, inputMimeTypes, outputMimeTypes, statsRouteUri);
        this.routeUri = routeUri;
    }

    protected abstract boolean isThin(RequestInfo requestInfo);

    protected MoleculeObjectDataset prepareDataset(MoleculeObjectDataset mods, boolean isThin) throws IOException {
        if (isThin) {
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

}
