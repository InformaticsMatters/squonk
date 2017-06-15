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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Allows to create a new Dataset from the molecules in a specified field. That field must be of type MoleculeObject[]
 *
 * @author timbo
 */
public class DatasetMoleculesFromFieldStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DatasetMoleculesFromFieldStep.class.getName());

    public static final String OPTION_MOLECULES_FIELD = StepDefinitionConstants.DatasetMoleculesFromFieldStep.OPTION_MOLECULES_FIELD;
    public static final String FIELD_NAME_ORIGIN = "MoleculeSource";

    /**
     * Create a slice of the dataset skipping a number of records specified by the skip option (or 0 if not specified)
     * and including only the number of records specified by the count option (or till teh end if not specified).
     *
     * @param varman
     * @param context
     * @throws Exception
     */
    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;
        Dataset<? extends BasicObject> ds = fetchMappedInput("input", Dataset.class, varman);
        if (ds == null) {
            throw new IllegalStateException("Input variable not found: input");
        }
        LOG.info("Input Dataset: " + ds);

        String fieldName = getOption(OPTION_MOLECULES_FIELD, String.class);
        if (fieldName == null) {
            throw new IllegalStateException("Selected field not found. Option named " + OPTION_MOLECULES_FIELD + " must present");
        }
        LOG.info("Input fieldName: " + fieldName);


        statusMessage = "Setting molecule extractor ...";
        Stream<MoleculeObject> stream = ds.getStream()
                .sequential()
                .flatMap((mo) -> {
                    UUID uuid = mo.getUUID();
                    MoleculeObject[] mols = mo.getValue(fieldName, MoleculeObject[].class);
                    if (mols == null) {
                        return Stream.empty();
                    } else {
                        return Arrays.stream(mols).peek(mol -> mol.putValue(FIELD_NAME_ORIGIN, uuid));
                    }
                });

        DatasetMetadata origMeta = ds.getMetadata();
        DatasetMetadata meta = new DatasetMetadata(MoleculeObject.class);
        meta.appendDatasetHistory("Dataset created molecules from field " + fieldName + " of source dataset: " + origMeta.getProperty(DatasetMetadata.PROP_DESCRIPTION));
        meta.createField(FIELD_NAME_ORIGIN, this.getClass().getSimpleName() , "UUID of source molecule", UUID.class);

        Dataset results = new Dataset(stream, meta);

        statusMessage = "Writing results ...";
        String outFldName = mapOutputVariable("output");
        if (outFldName != null) {
            createVariable(outFldName, Dataset.class, results, varman);
        }

        statusMessage = generateStatusMessage(ds.getSize(), results.getSize(), -1);
        LOG.info("Results: " + results.getMetadata());
    }

}
