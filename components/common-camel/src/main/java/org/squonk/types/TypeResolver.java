/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

import org.squonk.api.HttpHandler;
import org.squonk.api.MimeTypeResolver;
import org.squonk.api.VariableHandler;
import org.squonk.dataset.Dataset;
import org.squonk.io.IODescriptor;
import org.squonk.io.SquonkDataSource;
import org.squonk.util.Utils;

import javax.activation.DataSource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by timbo on 20/03/2016.
 */
@Default
@ApplicationScoped
public class TypeResolver implements MimeTypeResolver {

    private final Map<Class, Class> httpHandlers = new HashMap<>();
    private final Map<Class, Class> variableHandlers = new HashMap<>();

    //TODO - Remove singleton usage once CDI is everywhere
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

        // also register the mime types in the TypeDescriptor class

        registerHttpHandler(Dataset.class, DatasetHandler.class);
        registerHttpHandler(SquonkDataSource.class, SquonkDataSourceHandler.class);
        registerHttpHandler(MolFile.class, MolFileHandler.class);
        registerHttpHandler(SDFile.class, SDFileHandler.class);
        registerHttpHandler(PDBFile.class, PDBFileHandler.class);
        registerHttpHandler(Mol2File.class, Mol2FileHandler.class);
        registerHttpHandler(CPSignTrainResult.class, CPSignTrainResultHandler.class);
        registerHttpHandler(ZipFile.class, ZipFileHandler.class);
        registerHttpHandler(PngImageFile.class, PngImageFileHandler.class);

        registerVariableHandler(Dataset.class, DatasetHandler.class);
        registerVariableHandler(SquonkDataSource.class, SquonkDataSourceHandler.class);
        registerVariableHandler(String.class, StringHandler.class);
        registerVariableHandler(CPSignTrainResult.class, CPSignTrainResultHandler.class);
        registerVariableHandler(MolFile.class, MolFileHandler.class);
        registerVariableHandler(SDFile.class, SDFileHandler.class);
        registerVariableHandler(PDBFile.class, PDBFileHandler.class);
        registerVariableHandler(Mol2File.class, Mol2FileHandler.class);
        registerVariableHandler(ZipFile.class, ZipFileHandler.class);
        registerVariableHandler(PngImageFile.class, PngImageFileHandler.class);
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
        return TypeDescriptor.resolvePrimaryType(mediaType);
    }

    @Override
    public Class resolveGenericType(String mediaType) {
        return TypeDescriptor.resolveGenericType(mediaType);
    }

    public String resolveMediaType(Class primaryType, Class secondaryType) {
        return TypeDescriptor.resolveMediaType(primaryType, secondaryType);
    }

    public IODescriptor createIODescriptor(DataSource dataSource) {
        String mediaType = dataSource.getContentType();
        return new IODescriptor(dataSource.getName(), mediaType, resolvePrimaryType(mediaType), resolveGenericType(mediaType));
    }

    public IODescriptor createIODescriptor(String name, String mediaType) {
        return new IODescriptor(name, mediaType, resolvePrimaryType(mediaType), resolveGenericType(mediaType));
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
            throw new RuntimeException("Unable to create variable handler from class " + type.getName() + " and generic type " + genericType, e);
        }
    }

}