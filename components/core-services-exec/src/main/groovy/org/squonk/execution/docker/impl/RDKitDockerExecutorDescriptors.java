package org.squonk.execution.docker.impl;

import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.docker.DockerExecutorDescriptor;
import org.squonk.execution.docker.DockerExecutorDescriptorRegistry;
import org.squonk.io.Resource;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;
import org.squonk.util.CommonMimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by timbo on 05/12/16.
 */
public class RDKitDockerExecutorDescriptors {

    private static final String[] IMAGES = new String[]{
            "informaticsmatters/rdkit:latest",  // first one is the default ? this should instead be a tag that point to the latest release
            "informaticsmatters/rdkit:Release_2016_09_2",
            "informaticsmatters/rdkit:Release_2016_03_1",
            "informaticsmatters/rdkit:Release_2015_09_2",
            "informaticsmatters/rdkit:Release_2015_09_1"
    };

    private static DockerExecutorDescriptor createDockerExecutorDescriptor(
            String id, String name, String description,
            String[] tags, String resourceRef, String icon, OptionDescriptor[] optionDescriptors,
            String command, HashMap<String, Resource> resources) {
        return new DockerExecutorDescriptor(
                id, name, description,
                tags,
                resourceRef,
                MoleculeObject.class, MoleculeObject.class, ServiceDescriptor.DataType.STREAM, ServiceDescriptor.DataType.STREAM,
                icon, // icon
                optionDescriptors,
                command,
                "input", "output",
                CommonMimeTypes.MIME_TYPE_MDL_SDF, CommonMimeTypes.MIME_TYPE_MDL_SDF, // input and output formats
                resources);
    }

    private static DockerExecutorDescriptor RDKIT_SCREEN_BASIC = createDockerExecutorDescriptor(
            "rdkit.screen.basic", "RDKit Docker Screening", "RDKit Docker Screening", // id, name, desc
            new String[]{"rdkit", "docker", "screen"}, // tags
            null, // resource (reference) url
            "icons/filter_molecules.png", // icon
            new OptionDescriptor[]{ // option descriptors

                    new OptionDescriptor(String.class, "docker.executor.id", "DockerExecutorDescriptor ID", "DockerExecutorDescriptor ID - do not change", OptionDescriptor.Mode.User)
                            .withAccess(false, false) // make invisible
                            .withDefaultValue("rdkit.screen.basic")
                            .withMinMaxValues(1, 1),

                    new OptionDescriptor(String.class, "dockerImage", "Docker image version", "Name of the Docker image to execute", OptionDescriptor.Mode.User)
                            .withValues(IMAGES)
                            .withDefaultValue(IMAGES[0])
                            .withMinMaxValues(1, 1),

                    new OptionDescriptor(new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.DISCRETE, new String[]{"smiles"}),
                            "arg.query", "Query molecule", "Query molecule as smiles", OptionDescriptor.Mode.User)
                            .withMinMaxValues(1, 1),

                    new OptionDescriptor(Float.class, "arg.threshold", "Threshold", "Similarity threshold", OptionDescriptor.Mode.User)
                            .withDefaultValue(0.7f)
                            .withMinMaxValues(1, 1),

                    new OptionDescriptor(String.class, "arg.descriptor", "Descriptor", "Descriptor/fingerprint to use", OptionDescriptor.Mode.User)
                            .withValues(new String[]{"maccs", "morgan2", "morgan3", "rdkit"})
                            .withDefaultValue("rdkit")
                            .withMinMaxValues(1, 1),

                    new OptionDescriptor(String.class, "arg.metric", "Metric", "Similarity metric to use", OptionDescriptor.Mode.User)
                            .withValues(new String[]{"asymmetric", "braunblanquet", "cosine", "dice", "kulczynski", "mcconnaughey", "rogotgoldberg", "russel", "sokal", "tanimoto"})
                            .withDefaultValue("tanimoto")
                            .withMinMaxValues(1, 1),
            },
            "python screen.py '${query_source}' ${threshold} -d ${descriptor} -m ${metric}", // command
            new HashMap<String, Resource>() {// resources
                {
                    // python files are under the src/main/resources/org/squonk/execution/docker/impl dir
                    put("screen.py", new Resource() {

                        @Override
                        public InputStream get() throws IOException {
                            return getClass().getResourceAsStream("screen.py");
                        }
                    });
                }
            }
    );


    public static void registerAll(DockerExecutorDescriptorRegistry registry) {

        registry.add(RDKIT_SCREEN_BASIC);

    }

}
