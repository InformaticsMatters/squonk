package org.squonk.rdkit.services

import org.squonk.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import org.squonk.camel.CamelCommonConstants
import com.im.lac.util.StreamProvider
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.rdkit.mol.EvaluatorDefintion

import static RdkitCalculatorsRouteBuilder.*
import java.util.stream.*
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.builder.ThreadPoolProfileBuilder
import org.apache.camel.spi.ThreadPoolProfile

/**
 * Created by timbo on 14/04/2014.
 */
class RdkitCalculatorsRouteBuilderSpec extends CamelSpecificationBase {

    static MoleculeObjectDataset dataset = new MoleculeObjectDataset([
            new MoleculeObject('C'),
            new MoleculeObject('CC'),
            new MoleculeObject('CCC')])
    
    def 'logp'() {

        when:
        def results = template.requestBody(RdkitCalculatorsRouteBuilder.RDKIT_LOGP, dataset)

        then:
        results instanceof MoleculeObjectDataset
        def list = results.dataset.items
        list.size == 3
        list[0].getValue(EvaluatorDefintion.Function.LOGP.name) instanceof Float
    }
    
    def 'frac c sp3'() {
        
        when:
        def results = template.requestBody(RdkitCalculatorsRouteBuilder.RDKIT_FRACTION_C_SP3, dataset)

        then:
        results instanceof MoleculeObjectDataset
        def list = results.dataset.items
        list.size == 3
        list[0].getValue(EvaluatorDefintion.Function.FRACTION_C_SP3.name) instanceof Float
    }

    def 'lipinski'() {
        
        when:
        def results = template.requestBody(RDKIT_LIPINSKI, dataset)

        then:
        results instanceof MoleculeObjectDataset
        def list = results.dataset.items
        list.size == 3
        list[0].getValue(EvaluatorDefintion.Function.LIPINSKI_HBA.name) instanceof Integer
        list[0].getValue(EvaluatorDefintion.Function.LIPINSKI_HBD.name) instanceof Integer
        list[0].getValue(EvaluatorDefintion.Function.LOGP.name) instanceof Float
        list[0].getValue(EvaluatorDefintion.Function.EXACT_MW.name) instanceof Float
    }

    
    @Override
    CamelContext createCamelContext() {
        CamelContext camelContext = super.createCamelContext()
        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME).poolSize(5).maxPoolSize(20).build();
        camelContext.getExecutorServiceManager().registerThreadPoolProfile(profile);
        return camelContext
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RdkitCalculatorsRouteBuilder()
    }
}
