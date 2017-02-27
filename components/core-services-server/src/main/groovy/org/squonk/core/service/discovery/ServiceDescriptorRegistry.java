package org.squonk.core.service.discovery;

import org.squonk.core.DockerServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.impl.GenericDatasetDockerExecutorStep;
import org.squonk.io.IODescriptors;
import org.squonk.io.IORoute;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.util.CommonMimeTypes;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 05/12/16.
 */
public class ServiceDescriptorRegistry {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorRegistry.class.getName());

    private final PostgresServiceDescriptorClient client;

    private final Map<String, ServiceDescriptorSet> serviceDescriptorSets = new LinkedHashMap<>();
    private final Map<String, ServiceDescriptor> serviceDescriptors = new LinkedHashMap<>();
    private final Object writeLock = new Object();
    private boolean initialised = false;

    private static ServiceDescriptorSet BUILT_IN_SERVICES = new ServiceDescriptorSet(null, null, Arrays.asList(new DockerServiceDescriptor[]{
            new DockerServiceDescriptor("docker.generic.dataset.v1", "GenericDockerProcessDataset", "Process Dataset with command in Docker container",
                    new String[]{"program", "code", "dataset", "docker"},
                    null,
                    "icons/program.png",
                    ServiceConfig.Status.ACTIVE,
                    new Date(),
                    IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
                    new IORoute[]{IORoute.FILE},
                    IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
                    new IORoute[]{IORoute.FILE},
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(String.class, StepDefinitionConstants.OPTION_DOCKER_IMAGE,
                                    "Docker image name", "The Docker image to use", OptionDescriptor.Mode.User)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "inputMediaType",
                                    "Input media type", "The format the input will be written as e.g. application/x-squonk-dataset-molecule+json", OptionDescriptor.Mode.User)
                                    .withValues(new String[]{CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, CommonMimeTypes.MIME_TYPE_MDL_SDF})
                                    .withDefaultValue(CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "outputMediaType",
                                    "Output media type", "The format the output will be read as e.g. chemical/x-mdl-sdfile", OptionDescriptor.Mode.User)
                                    .withValues(new String[]{CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, CommonMimeTypes.MIME_TYPE_MDL_SDF})
                                    .withDefaultValue(CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_SHELL),
                                    StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND,
                                    "Command", "The command to be executed e.g. bash script to execute inside container", OptionDescriptor.Mode.User)
                                    .withMinMaxValues(1, 1)
                    },
                    null, // thin descriptors
                    GenericDatasetDockerExecutorStep.class.getName(), // executor
                    null, // image - defined in option
                    null, // command - defined in option
                    null  // volumes
            )
    }));

    public ServiceDescriptorRegistry() {
        this.client = null;
    }

    public ServiceDescriptorRegistry(PostgresServiceDescriptorClient client) {
        this.client = client;
    }

    /**
     * Initialise the registry by loading the saved descriptors
     */
    protected void init() throws Exception {
        if (!initialised) {
            load();
            initialised = true;
        }
    }

    private void load() throws Exception {
        if (client != null) {
            List<ServiceDescriptorSet> sets = client.list();
            sets.forEach(set -> {
                serviceDescriptorSets.put(set.getBaseUrl(), set);
                set.getServiceDescriptors().forEach(sd -> serviceDescriptors.put(sd.getId(), sd));
            });
        }

    }

    public List<ServiceDescriptorSet> fetchServiceDescriptorSets() {
        List<ServiceDescriptorSet> list = serviceDescriptorSets.values().stream().collect(Collectors.toList());
        list.add(BUILT_IN_SERVICES);
        return list;
    }

    public List<ServiceDescriptor> fetchServiceDescriptors() {
        List<ServiceDescriptor> list = serviceDescriptors.values().stream().collect(Collectors.toList());
        list.addAll(BUILT_IN_SERVICES.getServiceDescriptors());
        return list;
    }

    public List<ServiceConfig> fetchServiceConfigs() {
        return fetchServiceDescriptors().stream().map(sd -> sd.getServiceConfig()).collect(Collectors.toList());
    }


    public ServiceDescriptor fetchServiceDescriptor(String id) {
        ServiceDescriptor sd = BUILT_IN_SERVICES.findServiceDescriptor(id);
        if (sd == null) {
            sd = serviceDescriptors.get(id);
        }
        return sd;
    }

    /**
     * Fetch the ServiceDescriptorSet associated with this url, creating it if it doesn't already exist.
     * You can make changes to this set, but to make sure the changes are persisted you must call the
     *
     * @param baseUrl
     * @return
     * @{link updateServiceDescriptorSet} method.
     */
    public ServiceDescriptorSet fetchServiceDescriptorSet(String baseUrl) {
        if (baseUrl == null) {
            return BUILT_IN_SERVICES;
        }
        synchronized (writeLock) {
            ServiceDescriptorSet set = serviceDescriptorSets.get(baseUrl);
            if (set == null) {
                set = new ServiceDescriptorSet(baseUrl, null);
                serviceDescriptorSets.put(baseUrl, set);
            }
            return set;
        }
    }

    /**
     * Update details for this set AND  persist the changes.
     *
     * @param set
     */
    public void updateServiceDescriptorSet(ServiceDescriptorSet set) {
        LOG.fine("Updating service descriptors for " + set.getBaseUrl());
        if (set.getBaseUrl() == null) {
            return;
        }
        synchronized (writeLock) {
            ServiceDescriptorSet old = serviceDescriptorSets.get(set.getBaseUrl());
            if (old != null) {
                // delete the old
                old.getServiceDescriptors().forEach(sd -> serviceDescriptors.remove(sd.getId()));
            }
            // add the new
            set.getServiceDescriptors().forEach(sd -> serviceDescriptors.put(sd.getId(), sd));
            serviceDescriptorSets.put(set.getBaseUrl(), set);
            saveServiceDescriptorSet(set);
        }
    }


    /**
     * Persist this set to the database
     *
     * @param set
     */
    private void saveServiceDescriptorSet(ServiceDescriptorSet set) {
        if (client != null && set.getBaseUrl() != null) {
            try {
                client.update(set);
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "Failed to save ServiceDescriptorSet " + set.getBaseUrl(), e);
            }
        }
    }

}
