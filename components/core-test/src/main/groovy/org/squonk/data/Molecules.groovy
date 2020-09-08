/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

package org.squonk.data

import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.reader.SDFReader
import org.squonk.types.MoleculeObject

/**
 * Created by timbo on 24/01/2016.
 */
class Molecules {

    static final String BUILDING_BLOCKS_SDF = "../../data/testfiles/Building_blocks_GBP.sdf.gz"
    static final String SCREENING_COMPOUNDS_SDF = "../../data/testfiles/Screening_Collection.sdf.gz"
    static final String DHFR_STANDARDIZED_SDF = "../../data/testfiles/dhfr_standardized.sdf.gz"
    static final String DHFR_STANDARDIZED_JSON = "../../data/testfiles/dhfr_standardized.json.gz"
    static final String KINASE_INHIBS_SDF = "../../data/testfiles/Kinase_inhibs.sdf.gz"
    static final String SMILES_10000 = "../../data/testfiles/nci10000.smiles"
    static final String SMILES_1000 = "../../data/testfiles/nci1000.smiles"
    static final String SMILES_100 = "../../data/testfiles/nci100.smiles"
    static final String SMILES_10 = "../../data/testfiles/nci10.smiles";

    static def ethanol = [

        v2000 : '''
  Mrv0541 02231512112D

  3  2  0  0  0  0            999 V2000
   -4.1987    0.0884    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -3.4842    0.5009    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -2.9008   -0.0825    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0
  1  2  1  0  0  0  0
  2  3  1  0  0  0  0
M  END
''',

        v3000 : '''
  Mrv0541 01191615492D

  0  0  0     0  0            999 V3000
M  V30 BEGIN CTAB
M  V30 COUNTS 3 2 0 0 0
M  V30 BEGIN ATOM
M  V30 1 C 2.31 -1.3337 0 0
M  V30 2 C 3.6437 -2.1037 0 0
M  V30 3 O 4.9774 -1.3337 0 0
M  V30 END ATOM
M  V30 BEGIN BOND
M  V30 1 1 1 2
M  V30 2 1 2 3
M  V30 END BOND
M  V30 END CTAB
M  END
''',

        smiles : 'CCO',

        inchi : 'InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3'
    ]

    static def caffeine = [
        smiles: 'CN1C=NC2=C1C(=O)N(C)C(=O)N2C',

            v2000: '''caffeine
  Mrv0541 04051617522D

 14 15  0  0  0  0            999 V2000
    0.7145   -1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    0.7145   -0.4125    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0
    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -0.7846   -0.2549    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0
   -1.2695    0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -0.7846    1.0799    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0
   -1.0396    1.8646    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    0.0000    0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    0.7145    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    0.7145    2.0625    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0
    1.4289    0.8250    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0
    2.1434    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    1.4289    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    2.1434   -0.4125    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0
  1  2  1  0  0  0  0
  2  3  1  0  0  0  0
  3  4  1  0  0  0  0
  4  5  2  0  0  0  0
  5  6  1  0  0  0  0
  6  7  1  0  0  0  0
  6  8  1  0  0  0  0
  3  8  2  0  0  0  0
  8  9  1  0  0  0  0
  9 10  2  0  0  0  0
  9 11  1  0  0  0  0
 11 12  1  0  0  0  0
 11 13  1  0  0  0  0
  2 13  1  0  0  0  0
 13 14  2  0  0  0  0
M  END
''',

            v3000: '''caffeine
  Mrv0541 04051617522D

  0  0  0     0  0            999 V3000
M  V30 BEGIN CTAB
M  V30 COUNTS 14 15 0 0 0
M  V30 BEGIN ATOM
M  V30 1 C 1.3337 -2.31 0 0
M  V30 2 N 1.3337 -0.77 0 0
M  V30 3 C 0 0 0 0
M  V30 4 N -1.4646 -0.4759 0 0
M  V30 5 C -2.3698 0.77 0 0
M  V30 6 N -1.4646 2.0159 0 0
M  V30 7 C -1.9405 3.4805 0 0
M  V30 8 C 0 1.54 0 0
M  V30 9 C 1.3337 2.31 0 0
M  V30 10 O 1.3337 3.85 0 0
M  V30 11 N 2.6674 1.54 0 0
M  V30 12 C 4.001 2.31 0 0
M  V30 13 C 2.6674 0 0 0
M  V30 14 O 4.001 -0.77 0 0
M  V30 END ATOM
M  V30 BEGIN BOND
M  V30 1 1 1 2
M  V30 2 1 2 3
M  V30 3 1 3 4
M  V30 4 2 4 5
M  V30 5 1 5 6
M  V30 6 1 6 7
M  V30 7 1 6 8
M  V30 8 2 3 8
M  V30 9 1 8 9
M  V30 10 2 9 10
M  V30 11 1 9 11
M  V30 12 1 11 12
M  V30 13 1 11 13
M  V30 14 1 2 13
M  V30 15 2 13 14
M  V30 END BOND
M  V30 END CTAB
M  END
'''
    ]

    static List<MoleculeObject> nci10Molecules() {
        List mols = []
        File f = new File(SMILES_10)
        f.eachLine {
            String[] tokens = it.split("\t")
            mols << new MoleculeObject(tokens[0], 'smiles')
        }
        return mols
    }

    static Dataset<MoleculeObject> nci10Dataset() {
        return new MoleculeObjectDataset(nci10Molecules(), new DatasetMetadata(MoleculeObject.class, [:], 10)).getDataset()
    }

    static List<MoleculeObject> nci100Molecules() {
        List mols = []
        File f = new File(SMILES_100)
        f.eachLine {
            String[] tokens = it.split("\t")
            mols << new MoleculeObject(tokens[0], 'smiles')
        }
        return mols
    }

    static Dataset<MoleculeObject> nci100Dataset() {
        return new MoleculeObjectDataset(nci100Molecules(), new DatasetMetadata(MoleculeObject.class, [:], 100)).getDataset()
    }

    static List<MoleculeObject> nci1000Molecules() {
        List mols = []
        File f = new File(SMILES_1000)
        f.eachLine {
            String[] tokens = it.split("\t")
            mols << new MoleculeObject(tokens[0], 'smiles')
        }
        return mols
    }

    static Dataset<MoleculeObject> nci1000Dataset() {
        return new MoleculeObjectDataset(nci1000Molecules(), new DatasetMetadata(MoleculeObject.class, [:], 1000)).getDataset()
    }

    static List<MoleculeObject> nci10000Molecules() {
        List mols = []
        File f = new File(SMILES_10000)
        f.eachLine {
            String[] tokens = it.split("\t")
            mols << new MoleculeObject(tokens[0], 'smiles')
        }
        return mols
    }

    static Dataset<MoleculeObject> nci10000Dataset() {
        return new MoleculeObjectDataset(nci10000Molecules(), new DatasetMetadata(MoleculeObject.class, [:], 10000)).getDataset()
    }

    static Dataset<MoleculeObject> datasetFromSDF(String file) {
        SDFReader reader = new SDFReader(new FileInputStream(file))
        return new MoleculeObjectDataset(reader.asStream()).getDataset()
    }

    static Dataset<MoleculeObject> datasetFromJSON(String file) {
        InputStream is = new FileInputStream(file)
        return new Dataset(MoleculeObject.class, is)
    }

}
