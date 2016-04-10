package org.squonk.camel.openchemlib.processor

import com.im.lac.types.MoleculeObject
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.openchemlib.predict.LogPOCLPredictor
import org.squonk.openchemlib.predict.PSAOCLPredictor
import org.squonk.openchemlib.predict.SolubilityOCLPredictor
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by timbo on 05/04/16.
 */
class PredictorProcessorSpec extends Specification {

    @Shared
    DefaultCamelContext context

    @Shared
    def mols = [
            new MoleculeObject(Molecules.ethanol.smiles, 'smiles'),
            new MoleculeObject(Molecules.caffeine.smiles, 'smiles')
    ]

    void setupSpec() {
        context = new DefaultCamelContext()
        context.addRoutes new RouteBuilder() {

            @Override
            void configure() throws Exception {
                from("direct:predict")
                        .process(new PredictorProcessor()
                        .calculate(new LogPOCLPredictor())
                        .calculate(new SolubilityOCLPredictor())
                        .calculate(new PSAOCLPredictor()))
            }
        }

        context.start()
    }

    void shutdownSpec() {
        context?.shutdown()
    }

    void "calculate"() {

        Dataset<MoleculeObject> ds = new MoleculeObjectDataset(mols).dataset
        ProducerTemplate pt = context.createProducerTemplate()

        when:
        MoleculeObjectDataset results = pt.requestBody("direct:predict", ds)

        then:
        results != null
        results.items.size() == 2
        results.items[0].values.size() == 3
        results.items[1].values.size() == 3

    }

}
