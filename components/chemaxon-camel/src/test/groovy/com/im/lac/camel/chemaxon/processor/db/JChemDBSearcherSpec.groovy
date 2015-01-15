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
    
    //    def "check row count"() {
    //        
    //        when:
    //        db.eachRow("select * from dhfr") { row ->
    //            println row
    //        } 
    //        
    //        then:
    //        1 == 1
    //    }
    
    
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
        template.sendBodyAndHeader('direct:dhfr/molecules', 'c1ccncc1', 'JChemSearchOptions', 't:s')
        
        then:
        resultEndpoint.assertIsSatisfied()
        def body = resultEndpoint.receivedExchanges.in.body[0]
        body instanceof Iterable
        def mols = body.iterator().collect()
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
        template.sendBodyAndHeader('direct:dhfr/molecules', 'CCC1=NC(N)=NC(N)=C1C1=CC=C(Cl)C=C1', 'JChemSearchOptions', 't:s')
        
        then:
        resultEndpoint.assertIsSatisfied()
        def body = resultEndpoint.receivedExchanges.in.body[0]
        body instanceof Iterable
        def mols = body.iterator().collect()
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
        template.sendBodyAndHeader('direct:dhfr/smiles', 'c1ccncc1', 'JChemSearchOptions', 't:s')
        
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
        template.sendBodyAndHeader('direct:dhfr/cd_ids', 'c1ccncc1', 'JChemSearchOptions', 't:s')
        
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
        template.sendBodyAndHeader('direct:dhfr/cd_ids', 'CCC1=NC(N)=NC(N)=C1C1=CC=C(Cl)C=C1', 'JChemSearchOptions', 't:s')
        
        then:
        resultEndpoint.assertIsSatisfied()
        Iterable hits = resultEndpoint.receivedExchanges.in.body[0]
        hits.iterator().collect().size() > 0
    }
    
    def 'search for raw'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBodyAndHeader('direct:dhfr/raw', 'c1ccncc1', 'JChemSearchOptions', 't:s')
        
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
        template.sendBodyAndHeader('direct:dhfr/stream', 'c1ccncc1', 'JChemSearchOptions', 't:s')
        
        then:
        resultEndpoint.assertIsSatisfied()
        InputStream body = resultEndpoint.receivedExchanges.in.body[0]
        MolImporter importer = new MolImporter(body)
        int count = 0
        Molecule mol
        while ((mol = importer.read()) != null) {
            count++
            mol.getPropertyObject('name') != null
            mol.getPropertyObject('mset') != null
        }
        count == 237
    }
    def 'search for stream similarity'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBodyAndHeader('direct:dhfr/stream', 'CCC1=NC(N)=NC(N)=C1C1=CC=C(Cl)C=C1', 'JChemSearchOptions', 't:i')
        
        then:
        resultEndpoint.assertIsSatisfied()
        InputStream body = resultEndpoint.receivedExchanges.in.body[0]
        MolImporter importer = new MolImporter(body)
        int count = 0
        Molecule mol
        while ((mol = importer.read()) != null) {
            count++
            mol.getPropertyObject('name') != null
            mol.getPropertyObject('mset') != null
        }
        count > 0
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
                    .outputFormat("cxsmiles")
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
                    .outputFormat("sdf")
                ).to('mock:result')
                
                from("direct:dhfr/file")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                    .connection(con)
                    .structureTable("dhfr")
                    .searchOptions("t:s")
                    .outputMode(JChemDBSearcher.OutputMode.STREAM)
                    .outputColumns(['mset','name'])
                    .outputFormat("sdf")
                )
                .to("file:/Users/timbo/tmp?fileName=foo.sdf")
            }
        }
    }
}

