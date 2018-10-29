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
import org.squonk.api.VariableHandler;
import org.squonk.io.IODescriptor;
import org.squonk.io.InputStreamDataSource;
import org.squonk.io.SquonkDataSource;
import org.squonk.util.IOUtils;
import org.squonk.util.Utils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public abstract class DefaultHandler<T> implements HttpHandler<T>, VariableHandler<T> {

    private static final TypeResolver typeResolver = TypeResolver.getInstance();

    protected final Class<T> type;
    protected final Class genericType;

    public DefaultHandler(Class<T> type, Class genericType) {
        this.type = type;
        this.genericType = genericType;
    }

    public DefaultHandler(Class<T> type) {
        this.type = type;
        this.genericType = null;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    public Class getGenericType() {
        return genericType;
    }


    /**
     * Create the variable handled by this handler
     *
     * @param input An SquonkDataSource from which the value must be composed
     * @return The assembled value
     */
    @Override
    public T create(SquonkDataSource input) throws Exception {
        if (input == null) {
            throw new IllegalArgumentException("Input not defined");
        }
        T value;
        // first look for constructor that specifies generic type and SquonkDataSource
        if (genericType != null) {
            value = Utils.instantiate(getType(), new Class[]{Class.class, SquonkDataSource.class}, new Object[]{genericType, input});
            if (value != null) {
                return value;
            }
        }
        // OK, we'll try a constructor with just an SquonkDataSource
        value = Utils.instantiate(getType(), new Class[]{SquonkDataSource.class}, new Object[]{input});
        if (value != null) {
            return value;
        }
        throw new IllegalStateException("No suitable constructor defined for creating instance of " + getType().getName());
    }

    /**
     * Sub-classes supporting multiple datasources MUST override this methood
     *
     * @param inputs multiple datasources defining the variable.
     * @return
     * @throws Exception This execption i
     */
    @Override
    public T create(List<SquonkDataSource> inputs) throws Exception {
        if (inputs.size() == 1) {
            return create(inputs.get(0));
        } else {
            throw new IllegalStateException("Subclasses must override if there is not a single dataSource");
        }
    }

//    /**
//     * Create an instance from one or more SquonkDataSource. The inputs are passed in as a Map with the key identifying the
//     * type of input. Where there is only a single input the name is ignored and the
//     * {@link #create(SquonkDataSource)} method is called with that one input.
//     * Where there are multiple inputs the {@link #createMultiple(Map<String,SquonkDataSource>)} method is
//     * called that MUST be overrided by any subclass wanting to handle multiple inputs. The overriding method should use
//     * the names that are present as the keys to the Map to distinguish the different inputs.
//     *
//     * @param inputs multiple InputStreams where the key is a name that identifies the type of input
//     * @return
//     * @throws Exception
//     */
//    @Override
//    public T create(Map<String, SquonkDataSource> inputs) throws Exception {
//
//        if (inputs == null || inputs.size() == 0) {
//            throw new IllegalArgumentException("At least one input must be defined");
//        }
//
//        if (inputs.size() == 1) {
//            SquonkDataSource input = inputs.values().iterator().next();
//            return create(input);
//        } else {
//            return createMultiple(inputs);
//        }
//    }
//
//    protected T createMultiple(Map<String, SquonkDataSource> inputs) throws Exception {
//        throw new UnsupportedOperationException("Do not know how to create value from multiple inputs. Subclass must override this method to implement");
//    }

    @Override
    public T create(String mediaType, Class genericType, Map<String, InputStream> inputs) throws Exception {
        if (inputs.size() > 1) {
            return createMultiple(mediaType, genericType, inputs);
        }
        Map.Entry<String, InputStream> e = inputs.entrySet().iterator().next();
        return create(mediaType, genericType, e.getKey(), e.getValue());
    }

    protected T createMultiple(String mediaType, Class genericType, Map<String, InputStream> inputs) throws Exception {
        throw new UnsupportedOperationException("Do not know how to create value from multiple inputs. Subclass must override this method to implement");
    }

    protected T create(String mediaType, Class genericType, String role, InputStream input) throws Exception {
        SquonkDataSource ds = new InputStreamDataSource(role, role, mediaType, input, null);
        return create(ds);
    }

    public static <P> VariableHandler<P> createVariableHandler(Class<P> primaryType, Class secondaryType) {
        return typeResolver.createVariableHandler(primaryType, secondaryType);
    }

    public static Object buildObject(IODescriptor iod, Map<String, InputStream> inputs) throws Exception {
        VariableHandler vh = typeResolver.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
        if (vh == null) {
            if (inputs.size() > 1) {
                throw new IllegalStateException("Unable to handle multiple inputs when no VariableHandler is defined");
            }
            return IOUtils.convertStreamToString(inputs.values().iterator().next());
        } else {
            return vh.create(iod.getMediaType(), iod.getSecondaryType(), inputs);
        }
    }
}
