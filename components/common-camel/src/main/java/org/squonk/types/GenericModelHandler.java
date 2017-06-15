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
import org.squonk.api.VariableHandler;
import org.squonk.types.io.JsonHandler;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 18/10/2016.
 */
public class GenericModelHandler<T> implements VariableHandler<GenericModel>, GenericHandler<GenericModel, T> {

    private static final Logger LOG = Logger.getLogger(GenericModelHandler.class.getName());

    private Class<T> genericType;

    public GenericModelHandler(Class<T> genericType) {
        this.genericType = genericType;
    }

    public GenericModelHandler() {
    }


    @Override
    public Class<GenericModel> getType() {
        return null;
    }

    @Override
    public void writeVariable(GenericModel value, WriteContext context) throws Exception {
        String json = JsonHandler.getInstance().objectToJson(value);
        try {
            // first write the stream values as that's more likely to go wrong
            for (String name : value.getStreamNames()) {
                InputStream is = value.getStream(name);
                if (is != null) {
                    context.writeStreamValue(is);
                }
            }
            // now write the text variable
            context.writeTextValue(json);
        } catch (Exception ex) {
            try {
                // clear the variable
                context.deleteVariable();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to delete variable after error", e);
            }
            throw ex;
        }
    }

    @Override
    public GenericModel<T> readVariable(ReadContext context) throws Exception {

        String json = context.readTextValue();
        GenericModel<T> model = JsonHandler.getInstance().objectFromJson(json, GenericModel.class);
        for (String name : model.getStreamNames()) {
            InputStream is = context.readStreamValue(name);
            model.setStream(name, is);
        }
        return model;
    }

    @Override
    public void setGenericType(Class<T> genericType) {
        this.genericType = genericType;
    }

    @Override
    public Class<T> getGenericType() {
        return genericType;
    }
}
