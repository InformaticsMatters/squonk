/*
 * Copyright (c) 2021 Informatics Matters Ltd.
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

package org.squonk.rdkit.db.loaders

import groovy.util.logging.Log
import org.squonk.rdkit.db.ChemcentralConfig
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.impl.LTKBTable
import org.squonk.util.IOUtils

/** Loader for LKTB dataset.
 *
 * Created by timbo on 16/12/2015.
 */
@Log
class LTKBLoader extends AbstractRDKitLoader {

    static final String DEFAULT_FILENAME = "ltkb.csv";

    LTKBLoader(RDKitTable table, ChemcentralConfig config) {
        super(table, config)
        separator = ","
        structureCol = 13
    }

    LTKBLoader() {
        super(new LTKBTable())
        separator = ","
        structureCol = 13
    }

    def propertyToTypeMappings = [
            '0': String.class,   // ltkbid
            '2': String.class,   // drug_name
            '3': Integer.class,  // approval_year
            '4': Integer.class,  // dili_concern
            '8': Integer.class,  // vdili_concern
            '5': Integer.class,  // severity_class
            '6': String.class,   // label_section
            '9': String.class,   // greene_annotation
            '10': String.class,  // sakatis_annotation
            '11': String.class,  // xu_annotation
            '12': String.class,  // zhu_annotation
    ]

    @Override
    void load() {
        String filename = IOUtils.getConfiguration("CHEMCENTRAL_LOADER_FILE", DEFAULT_FILENAME)
        int limit = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_LIMIT", "0"))
        int reportingChunk = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_REPORTING_CHUNK", "100"))

        log.info("Using LTKBLoader to load $filename")
        loadSmiles(filename, limit, reportingChunk, propertyToTypeMappings)
        log.info("Loading finished")
    }

    protected String[] tokenizeLine(String line) {
        def tokens = []
        String current = null
        boolean inQuotes = false
        for (int i; i< line.length(); i++) {
            char c = line.charAt(i)
            if (current == null && c == '"') {
                //println "$c starting quotes $current"
                inQuotes = true
            } else if (!inQuotes && c == ',') {
                //println "$c finalising token $current"
                tokens << current
                current = null
            } else if (inQuotes && c == '"') {
                //println "$c ending quotes $current"
                inQuotes = false
            } else {
                //println "$c appending char $current"
                if (current == null) {
                    current = ""
                }
                current += c
            }
        }
        tokens << current
        //println "Found ${tokens.size()} tokens $tokens"

        return prcocessTokens(tokens.toArray([] as String[]))
    }

    protected String[] prcocessTokens(String[] data) {
        // convert year to an integer. Has value like 2008.0
        String sYear = data[3]
        if (sYear != null) {
            data[3] = sYear.substring(0, sYear.length() - 2)
        }

        // DILIConcern field
        String dili = data[4]
        if ('No-DILI-concern'.equalsIgnoreCase(dili)) {
            data[4] = 0
        } else if ('Less-DILI-Concern'.equalsIgnoreCase(dili)) {
            data[4] = 1
        } else if ('Most-DILI-Concern'.equalsIgnoreCase(dili)) {
            data[4] = 2
        } else {
            println "WARNING - unexpected DILI value: $dili"
            data[4] = null
        }

        // vDILIConcern field
        String vdili = data[8]
        if ('vNo-DILI-Concern'.equalsIgnoreCase(vdili)) {
            data[8] = 0
        } else if ('vLess-DILI-Concern'.equalsIgnoreCase(vdili)) {
            data[8] = 1
        } else if ('vMost-DILI-Concern'.equalsIgnoreCase(vdili)) {
            data[8] = 2
        } else if ('Ambiguous DILI-concern'.equalsIgnoreCase(vdili)) {
            data[8] = -1
        } else {
            println "WARNING - unexpected vDILI value: $vdili"
            data[8] = null
        }

        return data
    }

}
