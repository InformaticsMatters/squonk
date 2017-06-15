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

package org.squonk.cdk.services;

import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.cdk.processor.CDKMolecularDescriptorProcessor;
import org.squonk.camel.cdk.processor.CDKVerifyStructureProcessor;
import org.squonk.cdk.molecule.MolecularDescriptors;
import org.apache.camel.builder.RouteBuilder;

/**
 * Basic services based on CDK
 *
 * @author timbo
 */
public class CdkCalculatorsRouteBuilder extends RouteBuilder {


    static final String CDK_STRUCTURE_VERIFY = "direct:structure_verify";
    static final String CDK_LOGP = "direct:logp";
    static final String CDK_DONORS_ACCEPTORS = "direct:donors_acceptors";
    static final String CDK_WIENER_NUMBERS = "direct:wiener_numbers";

    @Override
    public void configure() throws Exception {

        from(CDK_STRUCTURE_VERIFY)
                .log("CDK_STRUCTURE_VERIFY starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new CDKVerifyStructureProcessor())
                .log("CDK_STRUCTURE_VERIFY finished");

        from(CDK_LOGP )
                .log("CDK_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new CDKMolecularDescriptorProcessor()
                        .calculate(MolecularDescriptors.Descriptor.XLogP)
                        .calculate(MolecularDescriptors.Descriptor.ALogP));

        from(CDK_DONORS_ACCEPTORS)
                .log("CDK_DONORS_ACCEPTORS starting")
                .process(new CDKMolecularDescriptorProcessor()
                        .calculate(MolecularDescriptors.Descriptor.HBondDonorCount)
                        .calculate(MolecularDescriptors.Descriptor.HBondAcceptorCount));

        from(CDK_WIENER_NUMBERS)
                .log("CDK_WIENER_NUMBERS starting")
                .process(new CDKMolecularDescriptorProcessor()
                        .calculate(MolecularDescriptors.Descriptor.WienerNumbers));

    }
}
