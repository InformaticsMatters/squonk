package com.im.lac.types

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


}
