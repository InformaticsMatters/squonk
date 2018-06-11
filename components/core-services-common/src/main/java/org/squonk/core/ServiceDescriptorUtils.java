/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author timbo
 */
public class ServiceDescriptorUtils {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorUtils.class.getName());

    private static final JsonHandler jsonHandler = JsonHandler.getInstance();
    private static final ObjectMapper jsonMapper = jsonHandler.getObjectMapper();
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    protected static String makeAbsoluteUrl(String baseUrl, HttpServiceDescriptor httpHttpServiceDescriptor) {
        String endpoint = httpHttpServiceDescriptor.getExecutionEndpoint();
        if (endpoint == null) {
            endpoint = "";
        } else if (isAbsoluteUrl(endpoint)) {
            return endpoint;
        }

        if (baseUrl.endsWith("/")) {
            return baseUrl + endpoint;
        } else {
            return baseUrl + "/" + endpoint;
        }
    }

    private static boolean isAbsoluteUrl(String url) {
        return url.toLowerCase().startsWith("http:") || url.toLowerCase().startsWith("https:");
    }

    public static HttpServiceDescriptor makeAbsolute(String baseUrl, HttpServiceDescriptor httpHttpServiceDescriptor) {

        String endpoint = httpHttpServiceDescriptor.getExecutionEndpoint();
        if (endpoint == null) {
            return httpHttpServiceDescriptor;
        } else {

            if (!isAbsoluteUrl(endpoint)) {
                return new HttpServiceDescriptor(
                        httpHttpServiceDescriptor.getServiceConfig(),
                        httpHttpServiceDescriptor.getThinDescriptors(),
                        makeAbsoluteUrl(baseUrl, httpHttpServiceDescriptor)
                );
            } else {
                return httpHttpServiceDescriptor;
            }
        }

    }

    public static <T extends ServiceDescriptor> T readServiceDescriptor(String p, Class<T> type) {
        return readServiceDescriptor(new java.io.File(p), type);
    }

    public static <T extends ServiceDescriptor> T readServiceDescriptor(java.io.File f, Class<T> type) {
        return readServiceDescriptor(f.toPath(), type);
    }

    /** Read a service descriptor of the provided type from the provided path.
     * For @{link DockerServiceDescriptor} the file must be named BASE_NAME.dsd.json (JSON format) or
     * BASE_NAME.dsd.yml (YAML format).
     * For @{link NextflowServiceDescriptor} the file must be named BASE_NAME.nsd.json (JSON format) or
     * BASE_NAME.nsd.yml (YAML format) and the nextflowFile and nextflowConfig properties can be defined
     * in additional files. See @{link NextflowServiceDescriptor} for more details.
     *
     * @param p The path to the file that contains the definition.
     * @param type The service descriptor type
     * @param <T>
     * @return
     */
    public static <T extends ServiceDescriptor> T readServiceDescriptor(Path p, Class<T> type) {
        T sd = null;
        if (p.toString().toLowerCase().endsWith(".yml") || p.toString().toLowerCase().endsWith(".yaml")) {
            try (InputStream is = new FileInputStream(p.toFile())) {
                sd = readYaml(is, type);
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Unable to read descriptor for " + p, ex);
            }
        } else if (p.toString().toLowerCase().endsWith(".json")) {
            try (InputStream is = new FileInputStream(p.toFile())) {
                sd = readJson(is, type);
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Unable to read descriptor for " + p, ex);
            }
        }
        if (sd == null) {
            LOG.log(Level.INFO, "Unrecognised descriptor " + p);
        } else if (type == NextflowServiceDescriptor.class) {
            try {
                // load the parts that can be in additional files
                completeNextflowServiceDescriptor((NextflowServiceDescriptor) sd, p);
            } catch (IOException ex) {
                LOG.warning("Incomplete service descriptor definition: " + ex.getMessage());
                return null;
            }
        }
        return sd;
    }

    public static <T> T readYaml(InputStream yaml, Class<T> cls) throws IOException {
        return yamlMapper.readValue(yaml, cls);
    }

    public static <T> T readYaml(String yaml, Class<T> cls) throws IOException {
        return yamlMapper.readValue(yaml, cls);
    }

    public static <T> T readJson(InputStream json, Class<T> cls) throws IOException {
        return jsonMapper.readValue(json, cls);
    }

    public static <T> T readJson(String json, Class<T> cls) throws IOException {
        return jsonMapper.readValue(json, cls);
    }

    private static void completeNextflowServiceDescriptor(
            NextflowServiceDescriptor sd,
            Path p) throws IOException {

        String s = p.toString();
        String b = s.substring(0, s.lastIndexOf("."));
        // the .nf file is mandatory, either within the YAML or as a separate file with a .nf extension
        if (sd.getNextflowFile() == null) {
            java.io.File f = new java.io.File(b + ".nf");
            if (f.exists()) {
                String nf = Files.readAllLines(f.toPath()).stream().collect(Collectors.joining("\n"));
                sd.setNextflowFile(nf);
            } else {
                throw new FileNotFoundException("Nextflow file not defined: " + f.getPath());
            }
        }

        // the nextflow.config file is optional and can be either within the YAML or as a separate file
        // with a .config extension
        if (sd.getNextflowConfig() == null) {
            java.io.File f = new java.io.File(b + ".config");
            if (f.exists()) {
                String nc = Files.readAllLines(f.toPath()).stream().collect(Collectors.joining("\n"));
                sd.setNextflowConfig(nc);
            }
        }
    }

    /** Processes services descriptors that are sent to us for including in the registry.
     * This method expands the execution endpoint of HttpServiceDescriptor so that they are a absolute URL based on
     * the ServiceDescriptorSet's baseUrl property.
     * The result is that the ServiceDescriptors in the input ServiceDescriptorSet are updated.
     *
     * @param sdset The ServiceDescriptorSet to process
     */
    public static void processServiceDescriptorRequest(ServiceDescriptorSet sdset) {

        String baseUrl = sdset.getBaseUrl();
        LOG.info("Received " + sdset.getServiceDescriptors().size() + " service descriptors from " + sdset.getBaseUrl());
        List<ServiceDescriptor> expandedSds = new ArrayList<>();
        for (ServiceDescriptor sd : sdset.getServiceDescriptors()) {
            if (sd instanceof HttpServiceDescriptor) {
                HttpServiceDescriptor hsd = (HttpServiceDescriptor) sd;
                LOG.fine("Expanding endpoint " + hsd.getExecutionEndpoint() + " for " + hsd.getId());
                HttpServiceDescriptor absUrlSD = ServiceDescriptorUtils.makeAbsolute(baseUrl, hsd);
                expandedSds.add(absUrlSD);
            }
        }
        sdset.updateServiceDescriptors(expandedSds);
    }

}
