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

package org.squonk.types

import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 25/01/16.
 */
class MoleculeObjectSpec extends Specification {

    static String tab = '''CHEMBL1969592\tCCCCCCc1cc2C=C(C(=O)Oc2cc1O)c3cn4ccccc4n3
CHEMBL2093947\tCc1c2cc[n+](cc2c(C)c3c4cc(O)ccc4[nH]c13)[C@@H]5O[C@H](CO)[C@@H](O)[C@@H]5O
CHEMBL2094073\tCc1c2cc[n+](cc2c(C)c3c4cc(O)ccc4[nH]c13)[C@H]5O[C@H](CO)[C@@H](O)[C@@H]5O
CHEMBL1923988\tCNc1nc2[nH]c(cc2c3c1ncn3C)c4cccc(CNC(=O)CCN(C)C)n4
CHEMBL1652699\tOC(=O)c1ccc2c(c1)nc(NC3CCCC3)c4ccncc24
CHEMBL1652707\tOC(=O)c1ccc2c(c1)nc(Nc3ccc(F)c(Cl)c3)c4ccncc24
CHEMBL1682282\tOC(=O)c1ccc2c(c1)nc(Nc3cccc(F)c3)c4ncncc24
CHEMBL1682283\tOC(=O)c1ccc2c(c1)nc(Nc3cccc(c3)C#C)c4ncncc24
CHEMBL275526\tCN(C)CCCNc1ccc2nnn3c4ccc(cc4C(=O)c1c23)[N+](=O)[O-]
CHEMBL1649770\tOC(=O)c1ccc2c(c1)nc(Nc3cccc(Cl)c3)c4ncncc24
CHEMBL1652703\tOC(=O)c1ccc2c(c1)nc(NCc3ccccc3)c4ccncc24'''

    void "read from tab"() {

        when:
        def mols = []

        tab.split('\n').each { l ->
            def parts = l.split('\t')
            //println parts[0]
            def mol = new MoleculeObject(parts[1], 'smiles')
            mol.putValue('chemblid', parts[0])
            mols << mol
        }
        String json = JsonHandler.getInstance().objectToJson(mols)
        //println json

        then:
        mols.size() == 11
        json.length() > 0

    }



    void "read json"() {

        def json = '''[{"source": "\\n  Mrv0541 03191509162D          \\n\\n 21 23  0  0  0  0            999 V2000\\n    2.1434   -2.8875    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n    2.1434   -3.7125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    2.8579   -4.1250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    2.8579   -4.9500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    2.1434   -5.3625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -4.9500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -4.1250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.7145   -5.3625    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.7145   -6.1875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -6.6000    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -7.4250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.7145   -7.8375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.0000   -7.4250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.0000   -6.6000    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.7145   -7.8375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.7145   -8.6625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   -1.4289   -9.0750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   -2.1434   -8.6625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   -2.1434   -7.8375    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n   -1.4289   -7.4250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    2.1434   -6.1875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n  1  2  1  0  0  0  0\\n  2  3  4  0  0  0  0\\n  3  4  4  0  0  0  0\\n  4  5  4  0  0  0  0\\n  5  6  4  0  0  0  0\\n  6  7  4  0  0  0  0\\n  2  7  4  0  0  0  0\\n  6  8  1  0  0  0  0\\n  8  9  1  0  0  0  0\\n  9 10  4  0  0  0  0\\n 10 11  4  0  0  0  0\\n 11 12  4  0  0  0  0\\n 12 13  4  0  0  0  0\\n 13 14  4  0  0  0  0\\n  9 14  4  0  0  0  0\\n 13 15  1  0  0  0  0\\n 15 16  4  0  0  0  0\\n 16 17  4  0  0  0  0\\n 17 18  4  0  0  0  0\\n 18 19  4  0  0  0  0\\n 19 20  4  0  0  0  0\\n 15 20  4  0  0  0  0\\n  5 21  1  0  0  0  0\\nM  END\\n", "values": {"cluster": 1, "version_id": 2727697, "similarity": 0.1506849315068493}, "format": "mol"},
{"source": "\\n  Mrv0541 03191509162D          \\n\\n 18 19  0  0  0  0            999 V2000\\n   -0.7145   -0.4125    0.0000 Br  0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.7145   -1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.0000   -1.6500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.0000   -2.4750    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.7145   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   -1.4289   -2.4750    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n   -1.4289   -1.6500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.7145   -3.7125    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.0000   -4.1250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.7145   -3.7125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -4.1250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -4.9500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.7145   -5.3625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.0000   -4.9500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.7145   -6.1875    0.0000 N   0  3  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -6.6000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.0000   -6.6000    0.0000 O   0  5  0  0  0  0  0  0  0  0  0  0\\n    2.1434   -5.3625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n  1  2  1  0  0  0  0\\n  2  3  4  0  0  0  0\\n  3  4  4  0  0  0  0\\n  4  5  4  0  0  0  0\\n  5  6  4  0  0  0  0\\n  6  7  4  0  0  0  0\\n  2  7  4  0  0  0  0\\n  5  8  1  0  0  0  0\\n  8  9  1  0  0  0  0\\n  9 10  4  0  0  0  0\\n 10 11  4  0  0  0  0\\n 11 12  4  0  0  0  0\\n 12 13  4  0  0  0  0\\n 13 14  4  0  0  0  0\\n  9 14  4  0  0  0  0\\n 13 15  1  0  0  0  0\\n 15 16  2  0  0  0  0\\n 15 17  1  0  0  0  0\\n 12 18  1  0  0  0  0\\nM  CHG  2  15   1  17  -1\\nM  END\\n", "values": {"cluster": 1, "version_id": 48551553, "similarity": 0.09859154929577464}, "format": "mol"},
{"source": "\\n  Mrv0541 03191509162D          \\n\\n 21 23  0  0  0  0            999 V2000\\n    2.1434   -2.8875    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n    2.1434   -3.7125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    2.8579   -4.1250    0.0000 C   0  5  0  0  0  0  0  0  0  0  0  0\\n    2.8579   -4.9500    0.0000 C   0  5  0  0  0  0  0  0  0  0  0  0\\n    2.1434   -5.3625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -4.9500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -4.1250    0.0000 C   0  5  0  0  0  0  0  0  0  0  0  0\\n    0.7145   -5.3625    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.7145   -6.1875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -6.6000    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n    1.4289   -7.4250    0.0000 C   0  5  0  0  0  0  0  0  0  0  0  0\\n    0.7145   -7.8375    0.0000 C   0  5  0  0  0  0  0  0  0  0  0  0\\n    0.0000   -7.4250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n    0.0000   -6.6000    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.7145   -7.8375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   -0.7145   -8.6625    0.0000 C   0  5  0  0  0  0  0  0  0  0  0  0\\n   -1.4289   -9.0750    0.0000 C   0  5  0  0  0  0  0  0  0  0  0  0\\n   -2.1434   -8.6625    0.0000 C   0  5  0  0  0  0  0  0  0  0  0  0\\n   -2.1434   -7.8375    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\n   -1.4289   -7.4250    0.0000 C   0  5  0  0  0  0  0  0  0  0  0  0\\n    2.1434   -6.1875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n  1  2  1  0  0  0  0\\n  2  3  4  0  0  0  0\\n  3  4  4  0  0  0  0\\n  4  5  4  0  0  0  0\\n  5  6  4  0  0  0  0\\n  6  7  4  0  0  0  0\\n  2  7  4  0  0  0  0\\n  6  8  1  0  0  0  0\\n  8  9  1  0  0  0  0\\n  9 10  4  0  0  0  0\\n 10 11  4  0  0  0  0\\n 11 12  4  0  0  0  0\\n 12 13  4  0  0  0  0\\n 13 14  4  0  0  0  0\\n  9 14  4  0  0  0  0\\n 13 15  1  0  0  0  0\\n 15 16  4  0  0  0  0\\n 16 17  4  0  0  0  0\\n 17 18  4  0  0  0  0\\n 18 19  4  0  0  0  0\\n 19 20  4  0  0  0  0\\n 15 20  4  0  0  0  0\\n  5 21  1  0  0  0  0\\nM  CHG  8   3  -1   4  -1   7  -1  11  -1  12  -1  16  -1  17  -1  18  -1\\nM  CHG  1  20  -1\\nM  END\\n", "values": {"cluster": 2, "version_id": 48845541}, "format": "mol"}]'''
        when:
        Dataset<MoleculeObject> mols = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), json);

        then:
        mols.items.size() == 3
        mols.items[0].values.size() == 3
        mols.items[1].values.size() == 3
        mols.items[2].values.size() == 2


    }


}
