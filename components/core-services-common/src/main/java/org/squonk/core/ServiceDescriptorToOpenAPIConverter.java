/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.squonk.io.IODescriptor;
import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.JobStatus;
import org.squonk.options.OptionDescriptor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ServiceDescriptorToOpenAPIConverter {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorToOpenAPIConverter.class.getName());
    private static final String DEFAULT_PATH = "/jobexecutor/rest/v1/jobs";

    private final String server;
    private final String path;
    private final String oidcUrl = "https://squonk.it/auth/realms/squonk/.well-known/openid-configuration";
    private String infoName = "Informatics Matters Ltd.";
    private String infoUrl = "https://squonk.it";
    private String infoEmail = "info@informaticsmatters.com";

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    private Map<String, Schema> schemas = new HashMap<>();

    public ServiceDescriptorToOpenAPIConverter(String server, String path) {
        this.server = server;
        this.path = path;
        yamlMapper = new ObjectMapper(new YAMLFactory());
        jsonMapper = new ObjectMapper();
        configureObjectMapper(yamlMapper);
        configureObjectMapper(jsonMapper);
        schemas.putAll(createJsonSchema(JobStatus.class));
        schemas.putAll(createJsonSchema(JobDefinition.class));

        LOG.info("Created schemas for " + schemas.keySet().stream().collect(Collectors.joining(",")));
    }

    public ServiceDescriptorToOpenAPIConverter(String server) {
        this(server, "/jobexecutor/rest/v1/jobs");
    }

    public String getServer() {
        return server;
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
        OpenAPI openApi = new OpenAPI()
                .components(new Components());
        handleInfo(openApi);
        handleServers(openApi);
        handleStaticServices(openApi);
        for (ServiceDescriptor sd : sds) {
            LOG.info("Handling Service Descriptor " + sd.getId());
            handlePaths(sd, openApi);
        }
        return openApi;
    }

    public String convertToJson(ServiceDescriptor sd) throws IOException {
        OpenAPI openApi = convertToOpenApi(sd);
        return openApiToJson(openApi);
    }

    public String openApiToJson(Object oai) throws IOException {
        return serialize(oai, jsonMapper);
    }

    public String openApiToYaml(Object oai) throws IOException {
        return serialize(oai, yamlMapper);
    }

    private static void configureObjectMapper(ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static String serialize(Object oai, ObjectMapper mapper) throws IOException {
        return mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(oai);
    }

    protected void handleInfo(OpenAPI openApi) {
        Info info = new Info();
        info.description("Squonk services accessible as external jobs")
                .title("Squonk job execution")
                .version("0.2")
                .contact(new Contact()
                        .name(infoName)
                        .email(infoEmail)
                        .url(infoUrl)
                );

        openApi.info(info);
    }

    protected void handleServers(OpenAPI openApi) {
        openApi.servers(Collections.singletonList(
                new Server().url(server)
                        .description("Squonk job executor")
        ));
    }

    protected void handleStaticServices(OpenAPI openApi) {
        handleSchemas(openApi);
        handleSecuritySchemes(openApi);
        handleSecurity(openApi);
        handleGetJobs(openApi);
        handleGetJobStatus(openApi);
    }

    protected void handleSchemas(OpenAPI openApi) {
        schemas.entrySet().forEach((e) -> openApi.getComponents().addSchemas(e.getKey(), e.getValue()));
    }

    protected void handleSecuritySchemes(OpenAPI openApi) {
        openApi.getComponents()
                .addSecuritySchemes("OIDC", new SecurityScheme()
                        .type(SecurityScheme.Type.OPENIDCONNECT)
                        //.openIdConnectUrl(oidcUrl))
                        .openIdConnectUrl("http://somewhere.com/foo/bar"))
                .addSecuritySchemes("OAuth2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2).flows(new OAuthFlows().implicit(
                                new OAuthFlow()
                                        .authorizationUrl("https://squonk.it/auth/realms/squonk/protocol/openid-connect/auth?client_id=squonk")
                                        .tokenUrl("https://squonk.it/auth/realms/squonk/protocol/openid-connect/token")
                                        .scopes(new Scopes().addString("openid", "general access"))))
                );
    }

    protected void handleSecurity(OpenAPI openApi) {
        openApi.addSecurityItem(new SecurityRequirement()
                .addList("OIDC", "openid"));
        openApi.addSecurityItem(new SecurityRequirement()
                .addList("OAUTH2", "openid"));
    }

    protected void handleGetJobs(OpenAPI openApi) {

        openApi.path(path + "/", new PathItem()
                .get(new Operation()
                        .summary("Get current jobs")
                        .description("List the current jobs for the specified user")
                        .addParametersItem(new Parameter()
                                .schema(new StringSchema())
                                .in("header")
                                .name("Authorization")
                                .description("Authentication token"))
                        .addParametersItem(new Parameter()
                                .schema(new StringSchema())
                                .in("header")
                                .name("SquonkUsername")
                                .description("Squonk user to delegate to"))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(new ArraySchema().items(new Schema().$ref("#/components/schemas/JobStatus")))))))));
    }

    protected void handleGetJobStatus(OpenAPI openApi) {

        openApi.path(path + "/${id}/status", new PathItem()
                .get(new Operation()
                        .summary("Get job status")
                        .description("Get job status for the specified job id and user.")
                        .addParametersItem(new Parameter()
                                .schema(new StringSchema())
                                .in("header")
                                .name("Authorization")
                                .description("Authentication token"))
                        .addParametersItem(new Parameter()
                                .schema(new StringSchema())
                                .in("header")
                                .name("SquonkUsername")
                                .description("Squonk user to delegate to"))
                        .addParametersItem(new Parameter()
                                .schema(new StringSchema())
                                .in("path")
                                .name("id")
                                .required(true)
                                .description("Job ID"))
                        .responses(new ApiResponses()
                                .addApiResponse("404", new ApiResponse()
                                        .description("No such job")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(new Schema()
                                                                .type("object")))))
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(new Schema().$ref("#/components/schemas/JobStatus"))))))));

    }


    protected void handlePaths(ServiceDescriptor sd, OpenAPI openApi) {
        PathItem pathItem = new PathItem();

        Operation operation = new Operation()
                .summary("Post the job");
        if (sd.getServiceConfig().getOptionDescriptors() != null) {
            for (OptionDescriptor option : sd.getServiceConfig().getOptionDescriptors()) {
                createParameter(option, operation);
            }
        }

        createRequestBody(sd, operation);
        createResponse(sd, operation);

        pathItem.post(operation)
                .summary(sd.getServiceConfig().getName())
                .description(sd.getServiceConfig().getDescription());

        openApi.path(path + "/" + sd.getServiceConfig().getId(), pathItem);
    }

    private static void createRequestBody(ServiceDescriptor sd, Operation operation) {

        RequestBody body = new RequestBody().required(true);

        Schema schema = new Schema().type("object");
        MediaType mediaType = new MediaType().schema(schema);
        Content content = new Content().addMediaType("multipart/mixed", mediaType);
        body.content(content);

        if (sd.getServiceConfig().getInputDescriptors() != null) {
            for (IODescriptor iod : sd.getServiceConfig().getInputDescriptors()) {
                schema.addProperties(iod.getName(), new Schema().type("object"));
                mediaType.addEncoding(iod.getName(), new Encoding().contentType(iod.getMediaType()));
            }
        }

        operation.requestBody(body);
    }

    private static void createResponse(ServiceDescriptor sd, Operation operation) {
        Schema schema = new Schema().type("object");
        MediaType mediaType = new MediaType()
                .schema(schema);
        operation.responses(new ApiResponses().
                addApiResponse("200", new ApiResponse()
                        .description("OK")
                        .content(new Content()
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
                .description(option.getDescription());

        if (option.getMinValues() != null && option.getMinValues() > 0) {
            parameter.setRequired(true);
        }

        operation.addParametersItem(parameter);
    }

    private static Schema createSchema(OptionDescriptor option) {
        Schema schema = new Schema();
        String[] type = option.getTypeDescriptor().getJsonSchemaType();
        if (type == null || type.length == 0) {
            LOG.warning("Undefined Json schema type for " + option.getTypeDescriptor().getType().getName());
        } else {
            schema.type(type[0]);
            if (type.length == 2) schema.format(type[1]);
        }
        schema.minItems(option.getMinValues()).maxItems(option.getMaxValues());
        // TODO - handle option.getValues() as the enum property
        return schema;
    }


    public static Map<String, Schema> createJsonSchema(Class clazz) {
        ModelConverters mc = new ModelConverters();
        Map<String, Schema> schemas = mc.read(clazz);
        return schemas;
    }

}
