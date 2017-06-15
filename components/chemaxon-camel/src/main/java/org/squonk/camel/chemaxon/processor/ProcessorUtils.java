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

package org.squonk.camel.chemaxon.processor;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import org.squonk.util.CloseableQueue;
import java.io.IOException;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class ProcessorUtils {

    public static String determineStringProperty(Exchange exchange, String defaultValue, String headerProperty) {
        String headerOpt = exchange.getIn().getHeader(headerProperty, String.class);
        if (headerOpt != null) {
            return headerOpt;
        } else {
            return defaultValue;
        }
    }
    
    public static Integer determineIntProperty(Exchange exchange, Integer defaultValue, String headerProperty) {
        Integer headerOpt = exchange.getIn().getHeader(headerProperty, Integer.class);
        if (headerOpt != null) {
            return headerOpt;
        } else {
            return defaultValue;
        }
    }

    public static void writeMoleculesToMolExporter(final MolExporter exporter, final Molecule[] mols) throws IOException {
        for (Molecule mol : mols) {
            exporter.write(mol);
        }
    }

    public static void writeMoleculesToQueue(final CloseableQueue q, final Molecule[] mols) throws IOException {
        for (Molecule mol : mols) {
            q.add(mol);
        }
    }

}
