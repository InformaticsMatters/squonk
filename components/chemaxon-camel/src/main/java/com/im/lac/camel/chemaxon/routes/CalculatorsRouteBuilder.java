package com.im.lac.camel.chemaxon.routes;

import chemaxon.struc.Molecule;
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpGenerator;
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpParameters;
import com.chemaxon.descriptors.fingerprints.pf2d.PfGenerator;
import com.chemaxon.descriptors.fingerprints.pf2d.PfParameters;
import com.im.lac.camel.chemaxon.processor.ChemAxonMoleculeProcessor;
import com.im.lac.camel.chemaxon.processor.HeaderPropertySetterProcessor;
import com.im.lac.camel.chemaxon.processor.MoleculeConverterProcessor;
import com.im.lac.camel.chemaxon.processor.screening.MoleculeScreenerProcessor;
import com.im.lac.chemaxon.screening.MoleculeScreener;
import java.io.File;
import java.io.InputStream;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author timbo
 */
public class CalculatorsRouteBuilder extends RouteBuilder {

    String base = "../../lacfiledrop/";

    @Override
    public void configure() throws Exception {

        // These routes do the actual work
        // What these routes accept is determined by the registerd TypeConvertors, 
        // but the following should be accepted:
        // 1. a Molecule
        // 2. multiple Molecules as an Iterator<Molecule>
        // 3. A String representation of a single molecule (output will be a Molecule)
        // simple route that calculates a hard coded property
        from("direct:logp")
                .process(new MoleculeConverterProcessor())
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()"));

        from("direct:logpSingleMolecule")
                .convertBodyTo(Molecule.class)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()"));

        from("direct:atomcount")
                .process(new MoleculeConverterProcessor())
                .log("atomcount body is ${body}")
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("atom_count", "atomCount()"));

        // simple route that calculates multiple hard coded properties
        from("direct:logp_atomcount_bondcount")
                .process(new MoleculeConverterProcessor())
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()")
                        .calculate("atom_count", "atomCount()")
                        .calculate("bond_count", "bondCount()")
                );

        // simple route that exemplifies filtering
        from("direct:filter_example")
                .process(new MoleculeConverterProcessor())
                .process(new ChemAxonMoleculeProcessor()
                        .filter("mass()<400")
                        .filter("ringCount()>0")
                        .filter("rotatableBondCount()<5")
                        .filter("donorCount()<=5")
                        .filter("acceptorCount()<=10")
                        .filter("logP()<4")
                );

        from("file:" + base + "screen/filter?antInclude=*.sdf&move=in")
                .log("running filter")
                .to("direct:filter_example")
                .to("direct:logp_atomcount_bondcount")
                .convertBodyTo(InputStream.class)
                .to("file:" + base + "screen/filter/out?fileName=${file:name.noext}.sdf");

        // dynamic route that requires the chem terms configuration to be set using
        // the ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION header property.
        // NOTE: if this is to be used for multiple molecules send then all together
        // as an Iterable<Molecule> or InputStream that can be converted to Iterable<Molecule>
        // get get optimum performance
        from("direct:chemTerms")
                .process(new MoleculeConverterProcessor())
                .process(new ChemAxonMoleculeProcessor());

        from("direct:chemTermsSingleMolecule")
                .convertBodyTo(Molecule.class)
                .process(new ChemAxonMoleculeProcessor());

        from("direct:standardize")
                .convertBodyTo(Molecule.class)
                .process(new ChemAxonMoleculeProcessor()
                        .standardize("aromatize")
                );

        EcfpParameters ecfpParams = EcfpParameters.createNewBuilder().build();
        EcfpGenerator ecfpGenerator = ecfpParams.getDescriptorGenerator();
        MoleculeScreener ecfpScreener = new MoleculeScreener(ecfpGenerator, ecfpGenerator.getDefaultComparator());

        from("direct:screening/ecfp")
                .process(new MoleculeConverterProcessor())
                .process(new MoleculeScreenerProcessor(ecfpScreener)
                );

        from("file:" + base + "screening/ecfp?antInclude=*.sdf&preMove=processing&move=../in")
                .log("running ecfp screening")
                .process(new HeaderPropertySetterProcessor(new File(base + "/screening/ecfp/headers.properties")))
                .to("direct:screening/ecfp")
                .convertBodyTo(InputStream.class)
                .to("file:" + base + "screening/ecfp/out?fileName=${file:name.noext}.sdf")
                .log("ecfp screening complete");

        PfParameters pfParams = PfParameters.createNewBuilder().build();
        PfGenerator pfGenerator = pfParams.getDescriptorGenerator();
        MoleculeScreener pfScreener = new MoleculeScreener(pfGenerator, pfGenerator.getDefaultComparator());

        from("direct:screening/pharmacophore")
                .process(new MoleculeConverterProcessor())
                .process(new MoleculeScreenerProcessor(pfScreener)
                );

        from("file:" + base + "screening/pharmacophore?antInclude=*.sdf&preMove=processing&move=../in")
                .log("running pharmacophore screening")
                .process(new HeaderPropertySetterProcessor(new File(base + "/screening/pharmacophore/headers.properties")))
                .to("direct:screening/pharmacophore")
                .convertBodyTo(InputStream.class)
                .to("file:" + base + "screening/pharmacophore/out?fileName=${file:name.noext}.sdf")
                .log("pharmacophore screening complete");

        from("direct:gunzip")
                .unmarshal().gzip();

        from("direct:gunzipAndCalculate")
                .to("direct:gunzip")
                .log("gunzip complete")
                .to("direct:logp_atomcount_bondcount")
                .log("calculation complete");

    }
}
