/*
 * Copyright (c) 2020 Informatics Matters Ltd.
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
package org.squonk.chemaxon.molecule;

import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.MoleculeObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculates BBB MPS score as defined in Gupta et al., J. Med. Chem. 2019, 62, 9824−9836.
 * DOI: 10.1021/acs.jmedchem.9b01220
 */
public class BBBGuptaMPSCalculator {

    private static final Logger LOG = Logger.getLogger(BBBGuptaMPSCalculator.class.getName());

    public static final String PROP_AROMATIC_RING_COUNT = "aro";
    public static final String PROP_HEAVY_ATOM_COUNT = "hac";
    public static final String PROP_ACCEPTOR_COUNT = "hba";
    public static final String PROP_DONOR_COUNT = "hbd";
    public static final String PROP_MOL_WEIGHT = "mw";
    public static final String PROP_TPSA = "tpsa";
    public static final String PROP_ROTATABLE_BOND_COUNT = "rotb";
    public static final String PROP_APKA = "apka";
    public static final String PROP_BPKA = "bpka";

    public static final String FIELD_PKA = "pKa_BBB";

    public static final String DEFAULT_RESULT_NAME = "BBB_Gupta";


    private Map<String, String> inputNameMappings = new HashMap<>();
    private final String resultPropName;
    private int errorCount = 0;

    public BBBGuptaMPSCalculator() {
        this(DEFAULT_RESULT_NAME);
    }

    public BBBGuptaMPSCalculator(String resultPropName) {
        inputNameMappings.put(PROP_AROMATIC_RING_COUNT, ChemTermsEvaluator.AROMATIC_RING_COUNT);
        inputNameMappings.put(PROP_HEAVY_ATOM_COUNT, ChemTermsEvaluator.HEAVY_ATOM_COUNT);
        inputNameMappings.put(PROP_ACCEPTOR_COUNT, ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT);
        inputNameMappings.put(PROP_DONOR_COUNT, ChemTermsEvaluator.HBOND_DONOR_COUNT);
        inputNameMappings.put(PROP_MOL_WEIGHT, ChemTermsEvaluator.MOLECULAR_WEIGHT);
        inputNameMappings.put(PROP_TPSA, ChemTermsEvaluator.TPSA);
        inputNameMappings.put(PROP_ROTATABLE_BOND_COUNT, ChemTermsEvaluator.ROTATABLE_BOND_COUNT);
        inputNameMappings.put(PROP_APKA, ChemTermsEvaluator.APKA);
        inputNameMappings.put(PROP_BPKA, ChemTermsEvaluator.BPKA);

        this.resultPropName = resultPropName;
    }


    public String getResultPropName() {
        return resultPropName;
    }

    public void updateMetadata(DatasetMetadata meta) {
        meta.createField(FIELD_PKA, this.getClass().getName(),
                "pKa value used for Gupta BBB calculation", Double.class);
        meta.createField(resultPropName, this.getClass().getName(),
                "BBB score from Gupta et.al. DOI: 10.1021/acs.jmedchem.9b01220", Double.class);
    }

    public Double calculate(MoleculeObject mo) {

//        LOG.info("Calculating ...");

        try {

            Double apka = findInput(inputNameMappings.get(PROP_APKA), Double.class, mo);
            Double bpka = findInput(inputNameMappings.get(PROP_BPKA), Double.class, mo);
            Double mw = findInput(inputNameMappings.get(PROP_MOL_WEIGHT), Double.class, mo);
            Double tpsa = findInput(inputNameMappings.get(PROP_TPSA), Double.class, mo);
            Integer aro = findInput(inputNameMappings.get(PROP_AROMATIC_RING_COUNT), Integer.class, mo);
            Number hac_num = findInput(inputNameMappings.get(PROP_HEAVY_ATOM_COUNT), Number.class, mo);
            Integer hac = hac_num == null ? null : hac_num.intValue();
            Integer hba = findInput(inputNameMappings.get(PROP_ACCEPTOR_COUNT), Integer.class, mo);
            Integer hbd = findInput(inputNameMappings.get(PROP_DONOR_COUNT), Integer.class, mo);
            Integer rot = findInput(inputNameMappings.get(PROP_ROTATABLE_BOND_COUNT), Integer.class, mo);

            if (mw == null || tpsa == null || aro == null || hac == null || hba == null || hbd == null || rot == null) {
                LOG.info(String.format("Data missing. Inputs apka=%s bpka=%s mw=%s tpsa=%s aro=%s hac=%s hba=%s hbd=%s rot=%s", apka, bpka, mw, tpsa, aro, hac, hba, hbd, rot));
                return null;
            }

            double pka = findCorrectPKa(apka, bpka);

            double score_aro = calculateAROScore(aro);
            double score_hac = calculateHACScore(hac);
            double mwhbn = calculateMWHBN(mw, hbd, hba);
            double score_mwhbn = calculateMWHBNScore(mwhbn);
            double score_tpsa = calculateTPSAScore(tpsa);
            double score_pka = calculatePKAScore(pka);

            double score_mps = score_aro + score_hac + (1.5d * score_mwhbn) + (2d * score_tpsa) + (0.5d * score_pka);

//            LOG.info("BBB score: " + score_mps);

            mo.putValue(resultPropName, score_mps);
            mo.putValue(FIELD_PKA, pka);

            return score_mps;

        } catch (Throwable t) {
            errorCount += 1;
            if (errorCount < 10) {
                LOG.log(Level.WARNING, "Calculation failed!", t);
            } else if (errorCount == 10) {
                LOG.log(Level.WARNING, "Calculation failed! Suppressing warning as this has occurred 10 times", t);
            }
        }

        return null;
    }

    protected <T> T findInput(String propName, Class<T> type, MoleculeObject mo) {
        T value = mo.getValue(propName, type);
        return value;
    }

    protected double findCorrectPKa(Double apka, Double bpka) {

        /*
        1.) If the molecule has basic pKa's ≥ 5, take the most basic pKa
        2.) If molecule has no basic pKa, take the most acidic pKa ≤ 9
        3.) If molecule has neither (neutral molecule), take the pKa that
        would give the maximal score for pKa descriptor (pKa=0 for MPO,
        pKa = 8.81 for BBB)
         */

        if (bpka != null && bpka >= 5d) {
            return bpka;
        }
        // a check if bpka was null was removed from the next line at Sygnature's request
        if (apka != null && apka <= 9d) {
            return apka;
        }
        return 8.81d;
    }

    /**
     * Score component for aromatic ring count
     *
     * @param aro
     * @return
     */
    protected double calculateAROScore(int aro) {
        switch (aro) {
            case 0:
                return 0.336376d;
            case 1:
                return 0.816016d;
            case 2:
                return 1d;
            case 3:
                return 0.691115d;
            case 4:
                return 0.199399d;
            default:
                return 0d;
        }
    }

    /**
     * Score component for heavy atom count
     *
     * @param hac
     * @return
     */
    protected double calculateHACScore(int hac) {
        if (hac > 5d && hac <= 45) {
            return (
                    (0.0000443d * Math.pow(hac, 3d))
                            - (0.004556d * Math.pow(hac, 2d))
                            + (0.12775d * hac)
                            - 0.463d
            ) / 0.624231d;
        } else {
            return 0d;
        }
    }

    /**
     * Generate the MWHBM value which is needed in order to calculate the MWHBN score
     *
     * @param mw
     * @param hbd
     * @param hba
     * @return
     */
    protected double calculateMWHBN(double mw, int hbd, int hba) {
        return (double)(hbd + hba) / Math.sqrt(mw);
    }

    /**
     * Score component for MWHBM
     *
     * @param mwhbn
     * @return
     */
    protected double calculateMWHBNScore(double mwhbn) {
        if (mwhbn > 0.05d && mwhbn <= 0.45d) {
            return (
                    (26.733d * Math.pow(mwhbn, 3d))
                            - (31.495d * Math.pow(mwhbn, 2d))
                            + (9.5202d * mwhbn)
                            - 0.1358d
            ) / 0.72258d;
        } else {
            return 0d;
        }
    }

    /**
     * Score component for TPSA
     *
     * @param tpsa
     * @return
     */
    protected double calculateTPSAScore(double tpsa) {
        if (tpsa > 0d && tpsa <= 120d) {
            return ((-0.0067d * tpsa) + 0.9598) / 0.9598d;
        } else {
            return 0d;
        }
    }

    /**
     * Score component for pKa
     *
     * @param pka
     * @return
     */
    protected double calculatePKAScore(double pka) {
        if (pka > 3d && pka <= 11d) {
            return (
                    (0.00045068d * Math.pow(pka, 4d))
                            - (0.016331d * Math.pow(pka, 3d))
                            + (0.18618d * Math.pow(pka, 2d))
                            - (0.71043d * pka)
                            + 0.8579d
            ) / 0.597488d;
        } else {
            return 0d;
        }
    }

}
