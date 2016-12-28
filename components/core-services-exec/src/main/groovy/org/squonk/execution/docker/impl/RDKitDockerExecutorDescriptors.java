package org.squonk.execution.docker.impl;

import org.squonk.execution.docker.DockerExecutorDescriptor;
import org.squonk.execution.docker.DockerExecutorDescriptorRegistry;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.io.IORoute;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;

import java.util.HashMap;

/**
 * Created by timbo on 05/12/16.
 */
public class RDKitDockerExecutorDescriptors {

    private static final String IMAGE_NAME = "informaticsmatters/rdkit";

    private static final String[] IMAGE_VERSIONS = new String[]{
            "latest",  // first one is the default ? this should instead be a tag that point to the latest release
            "Release_2016_09_2",
            "Release_2016_03_1",
            "Release_2015_09_2",
            "Release_2015_09_1"
    };

    private static DockerExecutorDescriptor createDockerExecutorDescriptor(
            String id, String name, String description,
            String[] tags, String resourceRef, String icon, OptionDescriptor[] optionDescriptors,
            String command, HashMap<String, String> resources) {
        return new DockerExecutorDescriptor(
                id, name, description,
                tags,
                resourceRef,
                icon,
                new IODescriptor[] {IODescriptors.createSDF("input", IORoute.FILE)},
                new IODescriptor[] {IODescriptors.createSDF("output", IORoute.FILE)},
                optionDescriptors,
                "org.squonk.execution.steps.impl.CannedDockerProcessDatasetStep",
                IMAGE_NAME,
                command,
                resources
                );
    }

    private static final DockerExecutorDescriptor RDKIT_SCREEN_BASIC = createDockerExecutorDescriptor(
            "rdkit.screen.basic", "RDKit Docker Screening", "RDKit Docker Screening", // id, name, desc
            new String[]{"rdkit", "docker", "screen"}, // tags
            null, // resource (reference) url
            "icons/filter_molecules.png", // icon
            new OptionDescriptor[]{ // option descriptors

                    new OptionDescriptor(String.class, StepDefinitionConstants.OPTION_DOCKER_IMAGE_VERSION, "RDKit version", "Version of the RDKit Docker image to execute", OptionDescriptor.Mode.User)
                            .withValues(IMAGE_VERSIONS)
                            .withDefaultValue(IMAGE_VERSIONS[0])
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
//            new HashMap<String, Resource>() {// resources
//                {
//                    // python files are under the src/main/resources/org/squonk/execution/docker/impl dir
//                    put("screen.py", new Resource() {
//
//                        @Override
//                        public InputStream get() throws IOException {
//                            return getClass().getResourceAsStream("screen.py");
//                        }
//                    });
//                }
//            }

            new HashMap<String, String>() {// resources
                {
                    // python files are under the src/main/resources/org/squonk/execution/docker/impl dir
                    put("screen.py", "screen.py");
                }
            });


    protected static DockerExecutorDescriptor[] getAll() {
        return  new DockerExecutorDescriptor[] {
                RDKIT_SCREEN_BASIC
        };
    }


    public static void registerAll(DockerExecutorDescriptorRegistry registry) {
        for (DockerExecutorDescriptor d : getAll()) {
            registry.add(d);
        }
    }

}
