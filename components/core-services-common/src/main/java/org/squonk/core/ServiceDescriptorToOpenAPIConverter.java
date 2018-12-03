/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

public class ServiceDescriptorToOpenAPIConverter {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorToOpenAPIConverter.class.getName());

    private final String baseUrl;
    private String infoName = "Informatics Matters Ltd.";
    private String infoUrl = "https://squonk.it";
    private String infoEmail = "info@informaticsmatters.com";

    public ServiceDescriptorToOpenAPIConverter(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getInfoEmail() {
        return infoEmail;
    }

    public void setInfoEmail(String infoEmail) {
        this.infoEmail = infoEmail;
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }

    public OpenAPI convertToOpenApi(ServiceDescriptor sd) throws IOException {
        return convertToOpenApi(Collections.singletonList(sd));
    }

    public OpenAPI convertToOpenApi(Collection<ServiceDescriptor> sds) throws IOException {
        OpenAPI openApi = new OpenAPI();
        handleInfo(openApi);
        handleServers(openApi);
        for (ServiceDescriptor sd : sds) {
            handlePaths(sd, openApi);
        }
        return openApi;
    }

    public String convertToJson(ServiceDescriptor sd) throws IOException {
        OpenAPI openApi = convertToOpenApi(sd);
        return openApiToJson(openApi);
    }

    public static String openApiToJson(OpenAPI oai) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        configureObjectMapper(mapper);

        return serialize(oai, mapper);
    }

    public static String openApiToYaml(OpenAPI oai) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        configureObjectMapper(mapper);

        return serialize(oai, mapper);
    }

    private static void configureObjectMapper(ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static String serialize(OpenAPI oai,  ObjectMapper mapper) throws IOException {
        return mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(oai);
    }

    protected void handleInfo(OpenAPI openApi) {
        Info info = new Info();
        info.description("Squonk services accessible as external jobs")
                .title("Squonk job execution")
                .contact(new Contact()
                        .name(infoName)
                        .email(infoEmail)
                        .url(infoUrl)
                );

        openApi.info(info);
    }

    protected void handleServers(OpenAPI openApi) {
        openApi.servers(Collections.singletonList(
                new Server().url(baseUrl)
                        .description("Squonk job executor service")
        ));
    }


    protected void handlePaths(ServiceDescriptor sd, OpenAPI openApi) {
        PathItem pathItem = new PathItem();

        Operation operation = new Operation()
                .description("Post the job");
        for (OptionDescriptor option : sd.getServiceConfig().getOptionDescriptors()) {
            createParameter(option, operation);
        }

        createRequestBody(sd, operation);
        createResponse(sd, operation);

        pathItem.post(operation)
                .description(sd.getServiceConfig().getDescription());

        openApi.path("/" + sd.getServiceConfig().getId(), pathItem);
    }

    private static void createRequestBody(ServiceDescriptor sd, Operation operation) {

        RequestBody body = new RequestBody().required(true);

        Schema schema = new Schema().type("object");
        MediaType mediaType = new MediaType().schema(schema);
        Content content = new Content().addMediaType("multipart/mixed", mediaType);
        body.content(content);

        for (IODescriptor iod : sd.getServiceConfig().getInputDescriptors()) {
            schema.addProperties(iod.getName(), new Schema().type("object"));
            mediaType.addEncoding(iod.getName(), new Encoding().contentType(iod.getMediaType()));
        }

        operation.requestBody(body);
    }

    private static void createResponse(ServiceDescriptor sd, Operation operation) {
        Schema schema = new Schema().type("object");
        MediaType mediaType = new MediaType()
                .schema(schema);
        operation.responses(new ApiResponses().
                addApiResponse("200", new ApiResponse().
                        content(new Content()
                                .addMediaType("multipart/mixed", mediaType))));
        for (IODescriptor iod : sd.getServiceConfig().getOutputDescriptors()) {
            schema.addProperties(iod.getName(), new Schema().type("object"));
            mediaType.addEncoding(iod.getName(), new Encoding().contentType(iod.getMediaType()));
        }
    }

    private static void createParameter(OptionDescriptor option, Operation operation) {

        String key = option.getKey();
        String in = null;
        if (key.startsWith("query.")) {
            in = "query";
        } else if (key.equals("body.")) {
            // ignore as will be part of the body?
        } else if (key.startsWith("header.")) {
            in = "header";
        } else {
            in = "query";
        }

        Schema schema = createSchema(option);

        Parameter parameter = new Parameter()
                .schema(schema)
                .in(in == null ? "query" : in)
                .name(option.getLabel())
                .description(option.getDescription())
                .required(option.getMinValues() > 0);

        operation.addParametersItem(parameter);
    }

    private static Schema createSchema(OptionDescriptor option) {
        Schema schema = new Schema();
        String type = option.getTypeDescriptor().getJsonSchemaType();
        if (type == null) {
            LOG.warning("Undefined Json schema type for " + option.getTypeDescriptor().getType().getName());
        } else {
            schema.type(type);
        }
        schema.minItems(option.getMinValues()).maxItems(option.getMaxValues());
        // TODO - handle option.getValues() as the enum property
        return schema;
    }

}
