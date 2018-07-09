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
import org.squonk.util.Utils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by timbo on 20/03/2016.
 */
@Default
@ApplicationScoped
public class TypeResolver implements MimeTypeResolver {

    private final Map<String, TypeDescriptor> typeDescriptors = new HashMap<>();
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

    /** Constructor manually registers supported mime types, http handlers nad variable handlers.
     * TODO - replace this manual registration with annotation or CDI based approach
     *
     */
    public TypeResolver() {
        registerMimeType(MIME_TYPE_DATASET_BASIC_JSON, Dataset.class, BasicObject.class);
        registerMimeType(MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class);
        registerMimeType(MIME_TYPE_MDL_MOLFILE, MolFile.class);
        registerMimeType(MIME_TYPE_MDL_SDF, SDFile.class);
        registerMimeType(MIME_TYPE_PDB, PDBFile.class);
        registerMimeType(MIME_TYPE_TRIPOS_MOL2, Mol2File.class);
        registerMimeType(MIME_TYPE_CPSIGN_TRAIN_RESULT, CPSignTrainResult.class);
        registerMimeType(MIME_TYPE_ZIP_FILE, ZipFile.class);
        registerMimeType(MIME_TYPE_PNG, PngImageFile.class);

        registerHttpHandler(Dataset.class, DatasetHandler.class);
        registerHttpHandler(MolFile.class, MolFileHandler.class);
        registerHttpHandler(SDFile.class, SDFileHandler.class);
        registerHttpHandler(PDBFile.class, PDBFileHandler.class);
        registerHttpHandler(Mol2File.class, Mol2FileHandler.class);
        registerHttpHandler(CPSignTrainResult.class, CPSignTrainResultHandler.class);
        registerHttpHandler(ZipFile.class, ZipFileHandler.class);
        registerHttpHandler(PngImageFile.class, PngImageFileHandler.class);

        registerVariableHandler(Dataset.class, DatasetHandler.class);
        registerVariableHandler(InputStream.class, InputStreamHandler.class);
        registerVariableHandler(String.class, StringHandler.class);
        registerVariableHandler(CPSignTrainResult.class, CPSignTrainResultHandler.class);
        registerVariableHandler(MolFile.class, MolFileHandler.class);
        registerVariableHandler(SDFile.class, SDFileHandler.class);
        registerVariableHandler(PDBFile.class, PDBFileHandler.class);
        registerVariableHandler(Mol2File.class, Mol2FileHandler.class);
        registerVariableHandler(ZipFile.class, ZipFileHandler.class);
        registerVariableHandler(PngImageFile.class, PngImageFileHandler.class);
    }

    private void registerMimeType(String mimeType, Class primaryType) {
        registerMimeType(mimeType, primaryType, null);

    }

    private void registerMimeType(String mimeType, Class primaryType, Class genericType) {
        typeDescriptors.put(mimeType, new TypeDescriptor(primaryType, genericType));
    }

    private void registerHttpHandler(Class primaryCls, Class handlerCls) {
        if (!HttpHandler.class.isAssignableFrom(handlerCls)) {
            throw new RuntimeException(handlerCls.getName() + " is not instance of HttpHandler ");
        }
        httpHandlers.put(primaryCls, handlerCls);
    }

    private void registerVariableHandler(Class primaryCls, Class handlerCls) {
        if (!VariableHandler.class.isAssignableFrom(handlerCls)) {
            throw new RuntimeException(handlerCls.getName() + " is not instance of VariableHandler");
        }
        variableHandlers.put(primaryCls, handlerCls);
    }

    @Override
    public Class resolvePrimaryType(String mediaType) {
        TypeDescriptor t = typeDescriptors.get(mediaType);
        return t == null ? null : t.getPrimaryType();
    }

    @Override
    public Class resolveGenericType(String mediaType) {
        TypeDescriptor t = typeDescriptors.get(mediaType);
        return t == null ? null : t.getSecondaryType();
    }

    public IODescriptor createIODescriptor(String name, String mediaType) {
        return new IODescriptor(name, mediaType, resolvePrimaryType(mediaType), resolveGenericType(mediaType));
    }

    public String resolveMediaType(Class primaryType, Class secondaryType) {
        for (Map.Entry<String,TypeDescriptor> e: typeDescriptors.entrySet()) {
            TypeDescriptor t = e.getValue();
            if (t.getPrimaryType() == primaryType && t.getSecondaryType() == secondaryType) {
                return e.getKey();
            }
        }
        return null;
    }

    @Override
    public <T> HttpHandler<T> createHttpHandler(String mimeType) {
        Class<T> p = resolvePrimaryType(mimeType);
        Class g = resolveGenericType(mimeType);
        return createHttpHandler(p, g);
    }

    @Override
    public <T> HttpHandler<T> createHttpHandler(Class<T> primaryType, Class genericType) {

        Class type = httpHandlers.get(primaryType);
        if (type == null) {
            return null;
        }
        try {
            if (genericType == null) {
                return (HttpHandler)type.newInstance();
            } else {
                return (HttpHandler<T>)Utils.instantiate(type, new Class[] {Class.class}, new Class[] {genericType});
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create HTTP handler from class " + type.getName());
        }
    }

    public <T> VariableHandler<T> createVariableHandler(Class<T> primaryType, Class genericType) {

        Class<T> type = variableHandlers.get(primaryType);
        if (type == null) {
            return null;
        }
        try {
            if (genericType == null) {
                return  (VariableHandler<T>) type.newInstance();
            } else {
                return (VariableHandler<T>)Utils.instantiate(type, new Class[] {Class.class}, new Class[] {genericType});
            }

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create variable handler from class " + type.getName() + " and generic type " + genericType);
        }
    }

    public class TypeDescriptor {

        Class primaryType;
        Class secondaryType;

        TypeDescriptor(Class primaryType, Class secondaryType) {
            assert primaryType != null;
            this.primaryType = primaryType;
            this.secondaryType = secondaryType;
        }

        public Class getPrimaryType() {
            return primaryType;
        }

        public Class getSecondaryType() {
            return secondaryType;
        }

        @Override
        public int hashCode() {
            if (secondaryType == null) {
                return primaryType.getName().hashCode();
            } else {
                return (primaryType.getName() + "+" + secondaryType.getName()).hashCode();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (! (o instanceof TypeDescriptor)) {
                return false;
            }
            TypeDescriptor t = (TypeDescriptor)o;

            if (primaryType != t.getPrimaryType()) {
                return false;
            }
            if (secondaryType == null && t.getSecondaryType() == null) {
                return true;
            }
            if (secondaryType == t.getSecondaryType()) {
                return true;
            }
            return false;
        }
    }
}