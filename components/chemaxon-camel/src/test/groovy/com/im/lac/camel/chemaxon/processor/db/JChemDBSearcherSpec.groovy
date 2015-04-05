package com.im.lac.camel.chemaxon.processor.db

import chemaxon.formats.MolImporter
import chemaxon.jchem.db.JChemSearch
import chemaxon.jchem.db.TableTypeConstants
import chemaxon.sss.search.JChemSearchOptions
import chemaxon.struc.Molecule
import chemaxon.util.ConnectionHandler

import com.im.lac.chemaxon.db.UpdateHandlerSupport
import com.im.lac.camel.testsupport.CamelSpecificationBase

import groovy.sql.Sql
import java.sql.*
import java.util.stream.Collectors
import org.apache.camel.builder.RouteBuilder
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class JChemDBSearcherSpec extends CamelSpecificationBase {
    
    @Shared Connection con
    @Shared Sql db
    
    def setupSpec() {
        con = DriverManager.getConnection("jdbc:derby:memory:JChemDBSearcherSpec;create=true")
        DBUtils.createDHFRStructureTable(con, 'dhfr')
        db = new Sql(con)
        
    } 
    def cleanupSpec() {
        try {
            DriverManager.getConnection("jdbc:derby:memory:JChemDBSearcherSpec;drop=true")
        } catch (SQLException ex) {} // expected
    }
    
    def "check row count"() {
        
        when:
        int count = db.firstRow("select count(*) from dhfr")[0] 
        
        then:
        count == 756
    }
    
    def 'check with jchemsearch'() {
        setup:
        ConnectionHandler conh = new ConnectionHandler()
        conh.connection = con
        JChemSearch searcher = new JChemSearch()
        searcher.setQueryStructure("c1ccncc1")
        searcher.setConnectionHandler(conh)
        searcher.setStructureTable("dhfr")
        def searchOptions = new JChemSearchOptions(JChemSearch.SUBSTRUCTURE)
        searcher.setSearchOptions(searchOptions)
        searcher.setRunMode(JChemSearch.RUN_MODE_SYNCH_COMPLETE)
        
        when:
        searcher.run()
        int[] hits = searcher.getResults()
        
        then:
        hits.length == 237
        
    }
    
    def 'search for molecule stream sss'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBody('direct:dhfr/molecules', 'c1ccncc1')
        
        then:
        resultEndpoint.assertIsSatisfied()
        def body = resultEndpoint.receivedExchanges.in.body[0]
        body.type == Molecule.class
        def mols = body.stream.collect(Collectors.toList())
        mols.size() == 237
        mols[0] instanceof Molecule
        mols[0].getPropertyObject('name') != null
        mols[0].getPropertyObject('mset') != null
    }
    
    def 'search for molecule stream similarity'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBody('direct:dhfr/molecules', 'CCC1=NC(N)=NC(N)=C1C1=CC=C(Cl)C=C1')
        
        then:
        resultEndpoint.assertIsSatisfied()
        def body = resultEndpoint.receivedExchanges.in.body[0]
        body.type == Molecule.class
        def mols = body.stream.collect(Collectors.toList())
        mols.size() > 0
        mols[0] instanceof Molecule
        mols[0].getPropertyObject('name') != null
        mols[0].getPropertyObject('mset') != null
    }
    
    def 'search for text'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBody('direct:dhfr/smiles', 'c1ccncc1')
        
        then:
        resultEndpoint.assertIsSatisfied()
        String body = resultEndpoint.receivedExchanges.in.body[0]
        body.trim().split('\n').length == 237
    }
    
    def 'search for cd_ids sss'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBody('direct:dhfr/cd_ids', 'c1ccncc1')
        
        then:
        resultEndpoint.assertIsSatisfied()
        Iterable hits = resultEndpoint.receivedExchanges.in.body[0]
        hits.iterator().collect().size() == 237
    }
    
    def 'search for cd_ids similarity'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBodyAndHeader('direct:dhfr/cd_ids', 'CCC1=NC(N)=NC(N)=C1C1=CC=C(Cl)C=C1',
            JChemDBSearcher.HEADER_SEARCH_OPTIONS, 't:i')
        
        then:
        resultEndpoint.assertIsSatisfied()
        Iterable hits = resultEndpoint.receivedExchanges.in.body[0]
        hits.iterator().collect().size() != 237
    }
    
    def 'search for raw'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBody('direct:dhfr/raw', 'c1ccncc1')
        
        then:
        resultEndpoint.assertIsSatisfied()
        int[] hits = resultEndpoint.receivedExchanges.in.body[0]
        hits.length == 237
    }
    
    def 'search for stream sss'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBody('direct:dhfr/stream', 'c1ccncc1')
        
        then:
        resultEndpoint.assertIsSatisfied()
        InputStream body = resultEndpoint.receivedExchanges.in.body[0]
        MolImporter importer = new MolImporter(body)
        int count = 0
        Molecule mol
        while ((mol = importer.read()) != null) {
            count++
            assert mol.getPropertyObject('name') != null
            assert mol.getPropertyObject('mset') != null
        }
        count == 237
    }
    def 'search for stream similarity'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBodyAndHeader('direct:dhfr/stream', 'CCC1=NC(N)=NC(N)=C1C1=CC=C(Cl)C=C1', 
            JChemDBSearcher.HEADER_SEARCH_OPTIONS, 't:i')
        
        then:
        resultEndpoint.assertIsSatisfied()
        InputStream body = resultEndpoint.receivedExchanges.in.body[0]
        MolImporter importer = new MolImporter(body)
        int count = 0
        Molecule mol
        while ((mol = importer.read()) != null) {
            count++
            assert mol.getPropertyObject('name') != null
            assert mol.getPropertyObject('mset') != null
            assert mol.getPropertyObject('similarity') != null
        }
        count > 0
    }
    
    def 'dynamic output mode as enum'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        // route is set for MOLECULES, but we overrride this to RAW
        template.sendBodyAndHeader('direct:dhfr/molecules', 'c1ccncc1', 
            JChemDBSearcher.HEADER_OUTPUT_MODE, JChemDBSearcher.OutputMode.RAW)
        
        then:
        resultEndpoint.assertIsSatisfied()
        // do we get the int[] array instead?
        int[] hits = resultEndpoint.receivedExchanges.in.body[0]
        hits.length == 237
    }
    
    def 'dynamic output mode as string'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        // route is set for MOLECULES, but we overrride this to RAW
        template.sendBodyAndHeader('direct:dhfr/molecules', 'c1ccncc1', 
            JChemDBSearcher.HEADER_OUTPUT_MODE, 'RAW')
        
        then:
        resultEndpoint.assertIsSatisfied()
        // do we get the int[] array instead?
        int[] hits = resultEndpoint.receivedExchanges.in.body[0]
        hits.length == 237
    }
        
    def 'dynamic structure format'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBodyAndHeader('direct:dhfr/smiles', 'c1ccncc1',
            JChemDBSearcher.HEADER_STRUCTURE_FORMAT, 'sdf')
        
        then:
        resultEndpoint.assertIsSatisfied()
        String body = resultEndpoint.receivedExchanges.in.body[0]
        body.trim().split('\n').length > 237
    }
    
    def 'hit colouring alignment'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBody('direct:dhfr/hcao', '[#6]-[#6]-[#6]-1=[#7]-[#6](-[#7])=[#7]-[#6](-[#7])=[#6]-1-[#6]-1=[#6]-[#6](-[#7]=[#7])=[#6](Cl)-[#6]=[#6]-1')
        
        then:
        resultEndpoint.assertIsSatisfied()
        String body = resultEndpoint.receivedExchanges.in.body[0]
        body.length() > 0
        body.split('atomSetRGB').length > 25
        // this doesn't test it works correctly, just that it returns data
    }
    
    
    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                
                from("direct:dhfr/molecules")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                    .connection(con)
                    .structureTable("dhfr")
                    .searchOptions("t:s")
                    .outputMode(JChemDBSearcher.OutputMode.MOLECULES)   
                    .outputColumns(['mset','name'])
                ).to('mock:result')
                
                from("direct:dhfr/smiles")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                    .connection(con)
                    .structureTable("dhfr")
                    .searchOptions("t:s")
                    .outputMode(JChemDBSearcher.OutputMode.TEXT)    
                    .structureFormat("cxsmiles")
                ).to('mock:result')
                
                from("direct:dhfr/raw")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                    .connection(con)
                    .structureTable("dhfr")
                    .searchOptions("t:s")
                    .outputMode(JChemDBSearcher.OutputMode.RAW)
                ).to('mock:result')
                
                from("direct:dhfr/cd_ids")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                    .connection(con)
                    .structureTable("dhfr")
                    .searchOptions("t:s")
                    .outputMode(JChemDBSearcher.OutputMode.CD_IDS)
                ).to('mock:result')
                
                from("direct:dhfr/stream")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                    .connection(con)
                    .structureTable("dhfr")
                    .searchOptions("t:s")
                    .outputMode(JChemDBSearcher.OutputMode.STREAM)
                    .outputColumns(['mset','name'])
                    .structureFormat("sdf")
                ).to('mock:result')
                
                from("direct:dhfr/file")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                    .connection(con)
                    .structureTable("dhfr")
                    .searchOptions("t:s")
                    .outputMode(JChemDBSearcher.OutputMode.STREAM)
                    .outputColumns(['mset','name'])
                    .structureFormat("sdf")
                )
                .to("file:/Users/timbo/tmp?fileName=foo.sdf")
                
                from("direct:dhfr/hcao")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                    .connection(con)
                    .structureTable("dhfr")
                    .searchOptions("t:s")
                    .outputMode(JChemDBSearcher.OutputMode.TEXT)    
                    .structureFormat("mrv")
                    .hitColorAndAlignOptions("hitColoring:y align:r")
                ).to('mock:result')
            }
        }
    }
}

