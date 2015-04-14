package com.im.lac.demo.routes;

import com.im.lac.camel.chemaxon.processor.StandardizerProcessor;
import org.apache.camel.builder.RouteBuilder;

/**
 * Utility routes for molecules
 *
 * @author timbo
 */
public class MoleculeUtilsRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {


        from("direct:removeFragmentsKeepLargest")
                .process(new StandardizerProcessor("removefragment:method=keeplargest"))
                .log("removefragments finished");
        
        from("direct:aromatizeGeneral")
                .process(new StandardizerProcessor("aromatize"))
                .log("aromatizeGeneral finished");
        
        from("direct:aromatizeBasic")
                .process(new StandardizerProcessor("aromatize:b"))
                .log("aromatizeBasic finished");
        
        from("direct:aromatizeLoose")
                .process(new StandardizerProcessor("aromatize:l"))
                .log("aromatizeLoose finished");
        
        from("direct:dearomatize")
                .process(new StandardizerProcessor("dearomatize"))
                .log("dearomatize finished");
        
        from("direct:neutralize")
                .process(new StandardizerProcessor("neutralize"))
                .log("neutralize finished");
        
        from("direct:addexplicith")
                .process(new StandardizerProcessor("addexplicith"))
                .log("addexplicith finished");
        
        from("direct:removeexplicith")
                .process(new StandardizerProcessor("removeexplicith"))
                .log("removeexplicith finished");
        
        from("direct:clearisotopes")
                .process(new StandardizerProcessor("clearisotopes"))
                .log("clearisotopes finished");
        
        from("direct:removesolvents")
                .process(new StandardizerProcessor("removesolvents"))
                .log("removesolvents finished");

        
        from("direct:expandsgroups")
                .process(new StandardizerProcessor("expandsgroups"))
                .log("expandsgroups finished");
        
        from("direct:contractsgroups")
                .process(new StandardizerProcessor("contractsgroups"))
                .log("contractsgroups finished");
        
        from("direct:ungroupsgroups")
                .process(new StandardizerProcessor("ungroupsgroups"))
                .log("ungroupsgroups finished");
        
        from("direct:tautomerize")
                .process(new StandardizerProcessor("tautomerize"))
                .log("tautomerize finished");
        
        from("direct:mesomerize")
                .process(new StandardizerProcessor("mesomerize"))
                .log("mesomerize finished");

    }
}
