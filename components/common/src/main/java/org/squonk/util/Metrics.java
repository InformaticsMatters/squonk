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

package org.squonk.util;

/**
 * Created by timbo on 30/06/16.
 */
public interface Metrics {

    String PROVIDER_SQUONK = "Squonk";
    String PROVIDER_CHEMAXON = "CXN";
    String PROVIDER_CDK = "CDK";
    String PROVIDER_RDKIT = "RDKit";
    String PROVIDER_OPENCHEMLIB = "OCL";
    String PROVIDER_UNI_COPENHAGEN = "UniCPH";
    String PROVIDER_DATA = "Data";
    String PROVIDER_DATA_TABLE = PROVIDER_DATA + ".Table";

    String METRICS_CPU_MINUTES = "CpuMinutes";
    String METRICS_MASS = "Mass";
    String METRICS_LOGP = "LogP";
    String METRICS_LOGD = "LogD";
    String METRICS_LOGS = "LogS";
    String METRICS_HBD = "HBondDonorCount";
    String METRICS_HBA = "HBondAcceptorCount";
    String METRICS_RING_COUNT = "RingCount";
    String METRICS_ROTATABLE_BOND_COUNT = "RotatableBondCount";
    String METRICS_ATOM_COUNT = "AtomCount";
    String METRICS_BOND_COUNT = "BondCount";
    String METRICS_FRACTION_C_SP3 = "FracSP3C";
    String METRICS_MOL_FORMULA = "MolFormula";
    String METRICS_MOLAR_REFRACTIVITY = "MolarRefractivity";
    String METRICS_PSA = "PSA";
    String METRICS_CHARGE = "Charge";
    String METRICS_PKA = "pKa";
    String METRICS_MPO = "MPO";
    String METRICS_RXN_ENUM = "ReactionEnumeration";
    String METRICS_SMARTCyp = "SMARTCyp";


    String METRICS_STRUCTURE_SEARCH_EXACT = "StructureSearch.Exact";
    String METRICS_STRUCTURE_SEARCH_SSS = "StructureSearch.SSS";
    String METRICS_STRUCTURE_SEARCH_SIMILARITY = "StructureSearch.Similarity";

    static String generate(String provider, String metric) {
        return provider + "." + metric;
    }


}
