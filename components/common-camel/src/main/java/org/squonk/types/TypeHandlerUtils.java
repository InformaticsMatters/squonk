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

import org.squonk.api.VariableHandler;
import org.squonk.io.IODescriptor;
import org.squonk.io.SquonkDataSource;
import org.squonk.io.StringDataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeHandlerUtils {

    public static Object convertDataSourcesToVariable(List<SquonkDataSource> dataSources, Class primaryType, Class secondaryType) throws Exception {
        VariableHandler vh = DefaultHandler.createVariableHandler(primaryType, secondaryType);
        Object value = vh.create(dataSources);
        return value;
    }

    public static List<SquonkDataSource> convertVariableToDataSources(Object variable) throws IOException {
        List<SquonkDataSource> results = new ArrayList<>();
        if (variable instanceof StreamType) {
            StreamType streamType = (StreamType) variable;
            SquonkDataSource[] dataSources = streamType.getDataSources();
            results.addAll(Arrays.asList(dataSources));
        } else {
            // hope this never happens, but would at least handle simple types
            String txt = variable.toString();
            SquonkDataSource ds = new StringDataSource(SquonkDataSource.ROLE_DEFAULT, null, "text/plain", txt, false);
            results.add(ds);
        }
        return results;
    }
}
