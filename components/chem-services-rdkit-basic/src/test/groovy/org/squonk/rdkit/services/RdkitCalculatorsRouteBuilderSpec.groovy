package org.squonk.rdkit.services

import com.im.lac.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import com.im.lac.camel.CamelCommonConstants
import com.im.lac.util.StreamProvider
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
    
    def 'logp'() {
        def mols = []
        mols << new MoleculeObject('C')
        mols << new MoleculeObject('CC')        
        mols << new MoleculeObject('CCC')
        
        when:
        def results = template.requestBody(RdkitCalculatorsRouteBuilder.RDKIT_LOGP, mols)

        then:
        results instanceof StreamProvider
        def list = results.stream.collect(Collectors.toList())
        list.size == 3
        list[0].getValue(KEY_LOGP) != null
        //println list[0].getValue(KEY_LOGP).class.name
        list[0].getValue(KEY_LOGP) instanceof Double
    }
    
    def 'frac c sp3'() {
        def mols = []
        mols << new MoleculeObject('C')
        mols << new MoleculeObject('CC')        
        mols << new MoleculeObject('CCC')
        
        when:
        def results = template.requestBody(RDKIT_FRACTION_C_SP3, mols)

        then:
        results instanceof StreamProvider
        def list = results.stream.collect(Collectors.toList())
        list.size == 3
        list[0].getValue(KEY_FRACTION_C_SP3) != null
        //println list[0].getValue(KEY_LOGP).class.name
        list[0].getValue(KEY_FRACTION_C_SP3) instanceof Double
    }

    def 'lipinski'() {
        def mols = []
        mols << new MoleculeObject('C')
        mols << new MoleculeObject('CC')        
        mols << new MoleculeObject('CCC')
        
        when:
        def results = template.requestBody(RDKIT_LIPINSKI, mols)

        then:
        results instanceof StreamProvider
        def list = results.stream.collect(Collectors.toList())
        list.size == 3
        list[0].getValue(KEY_LIPINSKI_HBA) != null
        //println list[0].getValue(KEY_LIPINSKI_HBA).class.name
        list[0].getValue(KEY_LIPINSKI_HBA) instanceof Long
        list[0].getValue(KEY_LIPINSKI_HBD) instanceof Long
        list[0].getValue(KEY_LIPINSKI_LOGP) instanceof Double
        list[0].getValue(KEY_LIPINSKI_MW) instanceof Double
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
