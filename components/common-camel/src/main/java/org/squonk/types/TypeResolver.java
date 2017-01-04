package org.squonk.types;

import org.squonk.api.GenericHandler;
import org.squonk.api.HttpHandler;
import org.squonk.api.MimeTypeResolver;
import org.squonk.api.VariableHandler;
import org.squonk.dataset.Dataset;
import org.squonk.io.IODescriptor;
import org.squonk.io.IORoute;
import org.squonk.io.IOMultiplicity;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by timbo on 20/03/2016.
 */
@Default
@ApplicationScoped
public class TypeResolver implements MimeTypeResolver {

    private final Map<String, Class> primaryTypes = new HashMap<>();
    private final Map<String, Class> genericTypes = new HashMap<>();
    private final Map<String, IOMultiplicity> ioTypes = new HashMap<>();
    private final Map<Class, Class> httpHandlers = new HashMap<>();
    private final Map<Class, Class> variableHandlers = new HashMap<>();

    public TypeResolver() {
        registerMimeType(MIME_TYPE_DATASET_BASIC_JSON, IOMultiplicity.ARRAY, Dataset.class, BasicObject.class);
        registerMimeType(MIME_TYPE_DATASET_MOLECULE_JSON, IOMultiplicity.ARRAY, Dataset.class, MoleculeObject.class);
        registerMimeType(MIME_TYPE_MDL_SDF, IOMultiplicity.ARRAY, SDFile.class);
        registerMimeType(MIME_TYPE_CPSIGN_TRAIN_RESULT, IOMultiplicity.ITEM, CPSignTrainResult.class);
        registerHttpHandler(Dataset.class, DatasetHandler.class);
        registerHttpHandler(SDFile.class, SDFileHandler.class);
        registerHttpHandler(CPSignTrainResult.class, CPSignTrainResultHandler.class);
        registerVariableHandler(Dataset.class, DatasetHandler.class);
        registerVariableHandler(CPSignTrainResult.class, CPSignTrainResultHandler.class);
    }

    public void registerMimeType(String mimeType, IOMultiplicity ioMultiplicity, Class primaryType) {
        ioTypes.put(mimeType, ioMultiplicity);
        primaryTypes.put(mimeType, primaryType);

    }

    public void registerMimeType(String mimeType, IOMultiplicity ioMultiplicity, Class primaryType, Class genericType) {
        registerMimeType(mimeType, ioMultiplicity, primaryType);
        if (genericType != null) {
            genericTypes.put(mimeType, genericType);
        }
    }

    public void registerHttpHandler(Class primaryCls, Class handlerCls) {
        if (!HttpHandler.class.isAssignableFrom(handlerCls)) {
            throw new RuntimeException(handlerCls.getName() + " is not instance of HttpHandler ");
        }
        httpHandlers.put(primaryCls, handlerCls);
    }

    public void registerVariableHandler(Class primaryCls, Class handlerCls) {
        if (!VariableHandler.class.isAssignableFrom(handlerCls)) {
            throw new RuntimeException(handlerCls.getName() + " is not instance of VariableHandler");
        }
        variableHandlers.put(primaryCls, handlerCls);
    }

    @Override
    public Class resolvePrimaryType(String mediaType) {
        return primaryTypes.get(mediaType);
    }

    @Override
    public Class resolveGenericType(String mediaType) {
        return genericTypes.get(mediaType);
    }

    @Override
    public IOMultiplicity resolveIOType(String mediaType) {
        return ioTypes.get(mediaType);
    }

    public IODescriptor createIODescriptor(String name, String mediaType) {
        return new IODescriptor(name, mediaType, resolvePrimaryType(mediaType), resolveGenericType(mediaType),  resolveIOType(mediaType));
    }

    @Override
    public HttpHandler createHttpHandler(String mimeType) {
        Class p = resolvePrimaryType(mimeType);
        Class g = resolveGenericType(mimeType);
        return createHttpHandler(p, g);
    }

    @Override
    public HttpHandler createHttpHandler(Class primaryType, Class genericType) {

        Class type = httpHandlers.get(primaryType);
        if (type == null) {
            return null;
        }
        try {
            HttpHandler h = (HttpHandler)type.newInstance();
            if (h instanceof GenericHandler) {
                ((GenericHandler)h).setGenericType(genericType);
            }
            return h;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create HTTP handler from class " + type.getName());
        }
    }

    public VariableHandler createVariableHandler(Class primaryType, Class genericType) {

        Class type = variableHandlers.get(primaryType);
        if (type == null) {
            return null;
        }
        try {
            VariableHandler h = (VariableHandler)type.newInstance();
            if (h instanceof GenericHandler) {
                ((GenericHandler)h).setGenericType(genericType);
            }
            return h;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create variable handler from class " + type.getName());
        }
    }
}