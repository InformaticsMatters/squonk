package com.im.lac.demo.routes;

import com.chemaxon.descriptors.fingerprints.ecfp.EcfpGenerator;
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpParameters;
import com.chemaxon.descriptors.fingerprints.pf2d.PfGenerator;
import com.chemaxon.descriptors.fingerprints.pf2d.PfParameters;
import com.chemaxon.descriptors.metrics.BinaryMetrics;
import com.im.lac.camel.processor.HeaderPropertySetterProcessor;
import com.im.lac.camel.chemaxon.processor.MoleculeObjectConverterProcessor;
import com.im.lac.camel.chemaxon.processor.clustering.SphereExclusionClusteringProcessor;
import com.im.lac.camel.chemaxon.processor.screening.MoleculeScreenerProcessor;
import com.im.lac.chemaxon.screening.MoleculeScreener;
import java.io.File;
import java.io.InputStream;
import org.apache.camel.builder.RouteBuilder;

/**
 * These are routes that provide examples of services. They are supposed to
 * illustrate what real-world services would need to do
 *
 * @author timbo
 */
public class DescriptorsRouteBuilder extends RouteBuilder {

    String base = "../../lacfiledrop/";

    @Override
    public void configure() throws Exception {

        // virtual screening using ECFP similarity
        EcfpParameters ecfpParams = EcfpParameters.createNewBuilder().build();
        EcfpGenerator ecfpGenerator = ecfpParams.getDescriptorGenerator();
        MoleculeScreener ecfpScreener = new MoleculeScreener(ecfpGenerator, ecfpGenerator.getDefaultComparator());

        from("direct:screening/ecfp")
                .process(new MoleculeObjectConverterProcessor())
                .process(new MoleculeScreenerProcessor(ecfpScreener)
                );

        from("file:" + base + "screening/ecfp?antInclude=*.sdf&preMove=processing&move=../in")
                .log("running ecfp screening")
                .process(new HeaderPropertySetterProcessor(new File(base + "/screening/ecfp/headers.properties")))
                .to("direct:screening/ecfp")
                .convertBodyTo(InputStream.class)
                .to("file:" + base + "screening/ecfp/out?fileName=${file:name.noext}.sdf")
                .log("ecfp screening complete");

        // virtual screening using pharmacophore similarity
        PfParameters pfParams = PfParameters.createNewBuilder().build();
        PfGenerator pfGenerator = pfParams.getDescriptorGenerator();
        MoleculeScreener pfScreener = new MoleculeScreener(pfGenerator, pfGenerator.getDefaultComparator());

        from("direct:screening/pharmacophore")
                .process(new MoleculeObjectConverterProcessor())
                .process(new MoleculeScreenerProcessor(pfScreener)
                );

        from("file:" + base + "screening/pharmacophore?antInclude=*.sdf&preMove=processing&move=../in")
                .log("running pharmacophore screening")
                .process(new HeaderPropertySetterProcessor(new File(base + "/screening/pharmacophore/headers.properties")))
                .to("direct:screening/pharmacophore")
                .convertBodyTo(InputStream.class)
                .to("file:" + base + "screening/pharmacophore/out?fileName=${file:name.noext}.sdf")
                .log("pharmacophore screening complete");

        // clustering using sphere exclusion clustering and ECPF4 descriptors
        EcfpGenerator gen = new EcfpParameters().getDescriptorGenerator(); // default ECFP

        from("direct:clustering/spherex/ecfp4")
                .process(new MoleculeObjectConverterProcessor())
                .process(new SphereExclusionClusteringProcessor(
                                gen, gen.getBinaryMetricsComparator(BinaryMetrics.BINARY_TANIMOTO)));

        from("file:" + base + "clustering/spherex/ecfp4?antInclude=*.sdf&preMove=processing&move=../in")
                .log("starting spherex clustering for file ${header.CamelFileName}")
                .to("direct:clustering/spherex/ecfp4")
                .convertBodyTo(InputStream.class)
                .to("file:" + base + "clustering/spherex/ecfp4/out?fileName=${file:name.noext}.sdf")
                .log("spherex clustering complete");

    }
}
