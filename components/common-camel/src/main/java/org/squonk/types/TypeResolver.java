package org.squonk.types;

import com.im.lac.types.BasicObject;
import com.im.lac.types.MoleculeObject;
import org.squonk.api.GenericHandler;
import org.squonk.api.HttpHandler;
import org.squonk.api.MimeTypeResolver;
import org.squonk.api.VariableHandler;
import org.squonk.dataset.Dataset;

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
    private final Map<Class, Class> httpHandlers = new HashMap<>();
    private final Map<Class, Class> variableHandlers = new HashMap<>();

    public TypeResolver() {
        registerMimeType(MIME_TYPE_DATASET_BASIC_JSON, Dataset.class, BasicObject.class);
        registerMimeType(MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class);
        registerMimeType(MIME_TYPE_MDL_SDF, SDFile.class);
        registerHttpHandler(Dataset.class, DatasetHandler.class);
        registerHttpHandler(SDFile.class, SDFileHandler.class);
        registerVariableHandler(Dataset.class, DatasetHandler.class);
    }

    public void registerMimeType(String mimeType, Class primaryType) {
        primaryTypes.put(mimeType, primaryType);
    }

    public void registerMimeType(String mimeType, Class primaryType, Class genericType) {
        registerMimeType(mimeType, primaryType);
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

    public Class resolvePrimaryType(String mimeType) {
        return primaryTypes.get(mimeType);
    }

    public Class resolveGenericType(String mimeType) {
        return genericTypes.get(mimeType);
    }

    public HttpHandler createHttpHandler(String mimeType) {
        Class p = resolvePrimaryType(mimeType);
        Class g = resolveGenericType(mimeType);
        return createHttpHandler(p, g);
    }

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