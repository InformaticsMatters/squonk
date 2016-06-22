package org.squonk.camel.cdk.typeConverters

import org.squonk.types.MoleculeObject
import org.apache.camel.CamelContext
import org.apache.camel.TypeConverter
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.types.CDKSDFile
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by timbo on 27/03/2016.
 */
class CDKTypeConverterSpec extends Specification {

    @Shared CamelContext context = new DefaultCamelContext()

    @Shared def mols = [
            new MoleculeObject('CC1=CC(=O)C=CC1=O', 'smiles', [fruit: 'apple', index: 1]),
            new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)', 'smiles', [fruit: 'orange', index: 2]),
            new MoleculeObject('CC(=O)OC(CC([O-])=O)C[N+](C)(C)C', 'smiles', [fruit: 'pear', index: 3]),
            new MoleculeObject('[O-][N+](=O)C1=CC(=C(Cl)C=C1)[N+]([O-])=O', 'smiles', [fruit: 'banana', index: 4]),
            new MoleculeObject('OC1C(O)C(O)C(OP(O)(O)=O)C(O)C1O', 'smiles', [fruit: 'melon', index: 5])
    ]

    void setupSpec() {
        context.start()
    }

    void shutdownSpec() {
        context.stop()
    }

    void "dataset typeconverter present"() {

        when:
        TypeConverter tc = context.getTypeConverterRegistry().lookup(CDKSDFile.class, Dataset.class)

        then:
        tc != null
    }

    void "moleculeobjectdataset typeconverter present"() {

        when:
        TypeConverter tc = context.getTypeConverterRegistry().lookup(CDKSDFile.class, MoleculeObjectDataset.class)

        then:
        tc != null
    }

    void "convert moleculeobjectdataset to sdf"() {

        MoleculeObjectDataset ds = new MoleculeObjectDataset(mols)

        when:
        def sdf = context.getTypeConverter().convertTo(CDKSDFile.class, ds)

        then:
        sdf != null
    }

    void "convert dataset to sdf"() {

        Dataset ds = new Dataset(MoleculeObject.class, mols)

        when:
        def sdf = context.getTypeConverter().convertTo(CDKSDFile.class, ds)

        then:
        sdf != null
    }
}
