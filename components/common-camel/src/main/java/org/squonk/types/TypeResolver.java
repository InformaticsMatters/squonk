/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.types;

import org.squonk.api.GenericHandler;
import org.squonk.api.HttpHandler;
import org.squonk.api.MimeTypeResolver;
import org.squonk.api.VariableHandler;
import org.squonk.dataset.Dataset;
import org.squonk.io.IODescriptor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import java.io.InputStream;
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

    //TO Remove singleton usage once CDI is everywhere
    private static TypeResolver INSTANCE;

    public static TypeResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TypeResolver();
        }
        return INSTANCE;
    }

    public TypeResolver() {
        registerMimeType(MIME_TYPE_DATASET_BASIC_JSON, Dataset.class, BasicObject.class);
        registerMimeType(MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class);
        registerMimeType(MIME_TYPE_MDL_SDF, SDFile.class);
        registerMimeType(MIME_TYPE_PDB, PDBFile.class);
        registerMimeType(MIME_TYPE_TRIPOS_MOL2, Mol2File.class);
        registerMimeType(MIME_TYPE_CPSIGN_TRAIN_RESULT, CPSignTrainResult.class);

        registerHttpHandler(Dataset.class, DatasetHandler.class);
        registerHttpHandler(SDFile.class, SDFileHandler.class);
        registerHttpHandler(PDBFile.class, PDBFileHandler.class);
        registerHttpHandler(Mol2File.class, Mol2FileHandler.class);
        registerHttpHandler(CPSignTrainResult.class, CPSignTrainResultHandler.class);

        registerVariableHandler(Dataset.class, DatasetHandler.class);
        registerVariableHandler(InputStream.class, InputStreamHandler.class);
        registerVariableHandler(String.class, StringHandler.class);
        registerVariableHandler(CPSignTrainResult.class, CPSignTrainResultHandler.class);
        registerVariableHandler(SDFile.class, SDFileHandler.class);
        registerVariableHandler(PDBFile.class, PDBFileHandler.class);
        registerVariableHandler(Mol2File.class, Mol2FileHandler.class);
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

    @Override
    public Class resolvePrimaryType(String mediaType) {
        return primaryTypes.get(mediaType);
    }

    @Override
    public Class resolveGenericType(String mediaType) {
        return genericTypes.get(mediaType);
    }


    public IODescriptor createIODescriptor(String name, String mediaType) {
        return new IODescriptor(name, mediaType, resolvePrimaryType(mediaType), resolveGenericType(mediaType));
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