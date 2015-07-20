package com.im.lac.chemaxon.services

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.struc.Molecule
import chemaxon.struc.MolBond
import com.im.lac.camel.chemaxon.processor.ChemAxonMoleculeProcessor
import com.im.lac.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import com.im.lac.camel.CamelCommonConstants
import com.im.lac.util.StreamProvider
import java.util.stream.*
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.builder.ThreadPoolProfileBuilder
import org.apache.camel.spi.ThreadPoolProfile

/**
 * Created by timbo on 14/04/2014.
 */
class CalculatorsRoutesSpec extends CamelSpecificationBase {
    
    static final String FILE_SMILES_100 = "../../data/testfiles/nci100.smiles";
    
    def 'logp single as MoleculeObject'() {

        when:
        def mol = new MoleculeObject('c1ccccc1')
        def result = template.requestBody(CalculatorsRouteBuilder.CHEMAXON_LOGP, mol)

        then:
        result instanceof MoleculeObject
        result.getValue('CXN_LogP') != null
        result.getValue('CXN_LogP') instanceof Number
    }
    
    def 'logp multiple as String'() {

        when:
        def result = template.requestBody(CalculatorsRouteBuilder.CHEMAXON_LOGP, 'c1ccccc1')

        then:
        result instanceof MoleculeObject
        result.getValue('CXN_LogP') != null
        result.getValue('CXN_LogP') instanceof Number
    }
    
    
    def 'logp multiple MoleculeObject as stream'() {
        def mols = []
        mols << new MoleculeObject('C')
        mols << new MoleculeObject('CC')        
        mols << new MoleculeObject('CCC')
        
        when:
        def results = template.requestBody(CalculatorsRouteBuilder.CHEMAXON_LOGP, mols)

        then:
        results instanceof StreamProvider
        results.stream.count() == 3
    }
    
    
    def 'logp file as stream'() {
        
        when:
        def results = template.requestBody(CalculatorsRouteBuilder.CHEMAXON_LOGP, new FileInputStream(FILE_SMILES_100))

        then:
        results instanceof StreamProvider
        results.stream.count() == 100
    }
    
    def 'filter as stream'() {
        
        when:
        def results = template.requestBody(CalculatorsRouteBuilder.CHEMAXON_DRUG_LIKE_FILTER, new FileInputStream(FILE_SMILES_100))
                
        then:
        results.stream.count() < 100
       
    }
    
    def 'filter dynamic'() {
        
        when:
        long t0 = System.currentTimeMillis()
        def results = template.requestBodyAndHeader(
            CalculatorsRouteBuilder.CHEMAXON_CHEMTERMS, new FileInputStream(FILE_SMILES_100),
            ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION, "filter=mass()<250"
        )
        long t1 = System.currentTimeMillis()
        long size = results.stream.count()
        long t2 = System.currentTimeMillis()
        //println "dynamic filter down to $size first in ${t1-t0}ms last in ${t2-t0}ms"
        then:
        size < 100
        size > 0
       
    }
    
    def 'filter as stream concurrent'() {
        println "filter as stream concurrent()"
        
        when:
        def results = []
        (1..10).each {
            // asyncRequestBody() returns a Future
            results << template.asyncRequestBody(CalculatorsRouteBuilder.CHEMAXON_DRUG_LIKE_FILTER, new FileInputStream(FILE_SMILES_100))
        }
        
        then:
        results.size() == 10
        int s0 = results[0].get().getStream().count()
        (1..9).each {
            def result = results[it].get()
            long count = result.stream.count()
            //println "result $it count is $count"
            assert count == s0
        }
    }
    
    def 'multiple props'() {

        when:
        def mol = new MoleculeObject('c1ccccc1')
        def result = template.requestBody(CalculatorsRouteBuilder.CHEMAXON_ATOM_BOND_COUNT, mol)

        then:
        result instanceof MoleculeObject
        result.getValue('atom_count') != null
        result.getValue('bond_count') != null
    } 
    
    def 'aromatize molecule'() {

        when:
        def mol = new MoleculeObject('C1=CC=CC=C1')
        def result = template.requestBody(CalculatorsRouteBuilder.CHEMAXON_AROMATIZE, mol)

        then:
        result instanceof MoleculeObject
        result.getRepresentation(Molecule.class.getName()).getBond(0).getType() == MolBond.AROMATIC
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
        return new CalculatorsRouteBuilder()
    }
}
