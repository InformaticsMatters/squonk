package com.im.lac.camel.chemaxon.processor.screening

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import com.chemaxon.descriptors.fingerprints.ecfp.*
import com.im.lac.camel.testsupport.CamelSpecificationBase
import com.im.lac.chemaxon.screening.MoleculeScreener
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.types.MoleculeObject
import com.im.lac.types.MoleculeObjectIterable
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class MoleculeScreenerProcessorSpec extends CamelSpecificationBase {


    def resultEndpoint
    
    def 'screen for single fixed'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        def mol = new MoleculeObject("CN(C)C1=C(Cl)C(=O)C2=C(C=CC=C2)C1=O")

        when:
        template.sendBody('direct:start', mol)

        then:
        resultEndpoint.assertIsSatisfied()
        MoleculeObject result = resultEndpoint.receivedExchanges.in.body[0]
        double similarity = result.getValue('similarity')
        similarity > 0 && similarity < 1
    }
    
    def 'dynamic target'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        def mol1 = new MoleculeObject("NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O")
        def mol2 = new MoleculeObject("CCN(C)C1=C(Br)C(=O)C2=C(C=CC=C2)C1=O")
        
        when:
        template.sendBodyAndHeader('direct:start', mol1, MoleculeScreenerProcessor.HEADER_TARGET_MOLECULE, mol2)

        then:
        resultEndpoint.assertIsSatisfied()
        MoleculeObject result = resultEndpoint.receivedExchanges.in.body[0]
        Double similarity = result.getValue('similarity')
        similarity > 0 && similarity < 1 // if header not read then would be 1.0
    }
    
    def "screen stream"() {
        given:
        def fis = new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz")
        MoleculeObjectIterable input = MoleculeObjectUtils.createIterable(fis)
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        def target = MolImporter.importMol("CCC1=NC(N)=NC(N)=C1C1=CC=C(Cl)C=C1")
            
        when:
        template.sendBodyAndHeader('direct:start', input, MoleculeScreenerProcessor.HEADER_TARGET_MOLECULE, target)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0].collect()
        result.size() < 756
        result.size() > 0
        
        cleanup:
        fis.close()
    
    }

    @Override
    RouteBuilder createRouteBuilder() {
        
        
        EcfpParameters params = EcfpParameters.createNewBuilder().build();
        EcfpGenerator generator = params.getDescriptorGenerator();
        MoleculeScreener screener = new MoleculeScreener(generator, generator.getDefaultComparator());
        screener.setTargetMol(MolImporter.importMol("NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O"))
        
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                .process(new MoleculeScreenerProcessor(screener))
                .to('mock:result')
                
            }
        }
    }
}
