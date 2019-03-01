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

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.links.Link;
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
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import org.squonk.util.MimeTypeUtils;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Class that generates an OpenAPI (Swagger) definition of the Job Executor.
 * Does this by converting the ServiceDescriptor definitions into OpenAPI.
 *
 * Configuration:
 *
 * Pass in the server and the base path into the constructor.
 * The server will be the scheme, host and port.
 * The base path will be the path to the services, probably '/jobexecutor/rest'
 *
 * Authentication using Keycloak is supported. If the environment variable KEYCLOAK_SERVER_URL is set then security is
 * enabled using that server and the OIDC and OAuth2 protocols. The KEYCLOAK_SERVER_REALM environment variable can be
 * set to specify the realm to use. If not set then 'squonk' is used as the realm name.
 *
 */
public class ServiceDescriptorToOpenAPIConverter {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorToOpenAPIConverter.class.getName());

    private final String server;
    private final String basePath; // probably something like jobexecutor/rest
    private final String jobsSubpath = "/v1/jobs";
    private final String keycloakUrl = IOUtils.getConfiguration("KEYCLOAK_SERVER_URL", null);
    private final String keycloakRealm = IOUtils.getConfiguration("KEYCLOAK_SERVER_REALM", "squonk");
    private String infoName = "Informatics Matters Ltd.";
    private String infoUrl = "https://squonk.it";
    private String infoEmail = "info@informaticsmatters.com";

    private final Yaml yaml;
    private final Json json;

    private Map<String, Schema> schemas = new HashMap<>();

    public ServiceDescriptorToOpenAPIConverter(String server, String basePath) {
        this.server = server;
        this.basePath = basePath;
        yaml = new Yaml();
        json = new Json();
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

    public String openApiToJson(Object oai) throws IOException {
        return json.pretty(oai);
    }

    public String openApiToYaml(Object oai) throws IOException {
        return yaml.pretty(oai);
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
        handleLinks(openApi);
        handleSecurity(openApi);
        handlePing(openApi);
        handleGetServices(openApi);
        handleGetJobs(openApi);
        handleGetJobStatus(openApi);
        handleGetJobResults(openApi);
        handleDeleteJob(openApi);
    }

    protected void handleSchemas(OpenAPI openApi) {
        schemas.entrySet().forEach((e) -> openApi.getComponents().addSchemas(e.getKey(), e.getValue()));
    }

//    protected void handleResponses(OpenAPI openApi) {
//        Components components = openApi.getComponents();
//        components.addResponses("401", new ApiResponse()
//                .description("Unauthorized"));
//    }

    protected void handleLinks(OpenAPI openApi) {
        openApi.getComponents()
                .addLinks("JobStatus", new Link()
                        .description("Get the job's status")
                        .operationId("JobStatus")
                        .parameters("id", "$response.body#/jobId")
                ).addLinks("JobResults", new Link()
                .description("Get the job's results")
                .operationId("JobResults")
                .parameters("id", "$response.body#/jobId")
        ).addLinks("JobDelete", new Link()
                .description("Delete the job")
                .operationId("JobDelete")
                .parameters("id", "$response.body#/jobId")
        );
    }

    protected void handleSecuritySchemes(OpenAPI openApi) {

        if (keycloakUrl != null) {
            String keycloakRealmPath = keycloakUrl + "/realms/" + keycloakRealm;
            LOG.info("Configuring against Keycloak at " + keycloakRealmPath);

            Random random = new Random();
            String nonce = "" + random.nextInt();
            openApi.getComponents()
                    .addSecuritySchemes("OIDC", new SecurityScheme()
                            .type(SecurityScheme.Type.OPENIDCONNECT)
                            .openIdConnectUrl(keycloakRealmPath + "/.well-known/openid-configuration"))
                    .addSecuritySchemes("OAUTH2", new SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2).flows(new OAuthFlows()
                                    // see https://github.com/swagger-api/swagger-ui/issues/3517 for why the nonce param is needed
                                    .authorizationCode(new OAuthFlow()
                                            .authorizationUrl(keycloakRealmPath + "/protocol/openid-connect/auth?nonce=" + nonce)
                                            .tokenUrl(keycloakRealmPath + "/protocol/openid-connect/token?nonce=" + nonce)
                                            .scopes(new Scopes().addString("openid", "general access")))
                            ));
        }
    }

    protected void handleSecurity(OpenAPI openApi) {
        if (keycloakUrl != null) {
            openApi.addSecurityItem(new SecurityRequirement()
                    .addList("OIDC", "openid"));
            openApi.addSecurityItem(new SecurityRequirement()
                    .addList("OAUTH2", "openid"));
        }
    }

    protected void handlePing(OpenAPI openApi) {
        openApi.path(basePath + "/ping", new PathItem()
                .get(new Operation()
                        .summary("Health check")
                        .description("Returns simple OK response")
                        .responses(new ApiResponses()
                                .addApiResponse("401", new ApiResponse()
                                        .description("Unauthorized"))
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content()
                                                .addMediaType("text/plain", new MediaType())))
                        )));
    }

    protected void handleGetServices(OpenAPI openApi) {

        openApi.path(basePath + "/v1/services/", new PathItem()
                .get(new Operation()
                        .summary("Get available services")
                        .description("List the available services")
                        .responses(new ApiResponses()
                                .addApiResponse("401", new ApiResponse()
                                        .description("Unauthorized"))
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content()
                                                .addMediaType(CommonMimeTypes.MIME_TYPE_JSON, new MediaType())))
                        )));
    }

    protected void handleGetJobs(OpenAPI openApi) {

        openApi.path(basePath + jobsSubpath, new PathItem()
                .get(new Operation()
                        .operationId("getJobs")
                        .summary("Get current jobs")
                        .description("List the current jobs for the specified user")
                        .addParametersItem(new Parameter()
                                .schema(new StringSchema())
                                .in("header")
                                .name("SquonkUsername")
                                .description("Squonk user to delegate to"))
                        .responses(new ApiResponses()
                                .addApiResponse("401", new ApiResponse()
                                        .description("Unauthorized"))
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content()
                                                .addMediaType(CommonMimeTypes.MIME_TYPE_JSON, new MediaType()
                                                        .schema(new ArraySchema().items(new Schema().$ref("#/components/schemas/JobStatus"))))))
                        )));
    }

    protected void handleGetJobStatus(OpenAPI openApi) {

        openApi.path(basePath + jobsSubpath + "/{id}/status", new PathItem()
                .get(new Operation()
                        .operationId("jobStatus")
                        .summary("Get job status")
                        .description("Get job status for the specified job id and user.")
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
                                .addApiResponse("401", new ApiResponse()
                                        .description("Unauthorized"))
                                .addApiResponse("404", new ApiResponse()
                                        .description("No such job")
                                        .content(new Content()
                                                .addMediaType(CommonMimeTypes.MIME_TYPE_JSON, new MediaType()
                                                        .schema(new Schema()
                                                                .type("object")))))
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content()
                                                .addMediaType(CommonMimeTypes.MIME_TYPE_JSON, new MediaType()
                                                        .schema(new Schema().$ref("#/components/schemas/JobStatus"))))
                                        .link("JobResults", new Link().$ref("JobResults"))
                                        .link("JobDelete", new Link().$ref("JobDelete"))
                                ))));

    }

    protected void handleGetJobResults(OpenAPI openApi) {
        openApi.path(basePath + jobsSubpath + "/{id}/results", new PathItem()
                .get(new Operation()
                        .operationId("jobResults")
                        .summary("Get job results")
                        .description("Get the job results for the specified job id and user.")
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
                                .addApiResponse("401", new ApiResponse()
                                        .description("Unauthorized"))
                                .addApiResponse("404", new ApiResponse()
                                        .description("No such job")
                                        .content(new Content()
                                                .addMediaType(CommonMimeTypes.MIME_TYPE_JSON, new MediaType()
                                                        .schema(new Schema()
                                                                .type("object")))))
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content()
                                                .addMediaType(CommonMimeTypes.MIME_TYPE_MULTIPART_MIXED, new MediaType()))
                                        .link("JobDelete", new Link().$ref("JobDelete"))
                                ))));
    }

    protected void handleDeleteJob(OpenAPI openApi) {

        openApi.path(basePath + jobsSubpath + "/{id}", new PathItem()
                .delete(new Operation()
                        .operationId("jobDelete")
                        .summary("Delete job")
                        .description("Delete the job for the specified job id and user.")
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
                                .addApiResponse("401", new ApiResponse()
                                        .description("Unauthorized"))
                                .addApiResponse("404", new ApiResponse()
                                        .description("No such job")
                                        .content(new Content()
                                                .addMediaType(CommonMimeTypes.MIME_TYPE_JSON, new MediaType()
                                                        .schema(new Schema()
                                                                .type("object")))))
                                .addApiResponse("200", new ApiResponse()
                                        .description("Deleted")
                                        .content(new Content()
                                                .addMediaType(CommonMimeTypes.MIME_TYPE_JSON, new MediaType()
                                                        .schema(new Schema().$ref("#/components/schemas/JobStatus"))))
                                ))));
    }


    protected void handlePaths(ServiceDescriptor sd, OpenAPI openApi) {
        PathItem pathItem = new PathItem();

        Operation operation = new Operation()
                .summary("Post the job");

        createRequestBody(sd, operation);
        createResponse(sd, operation);

        pathItem.post(operation)
                .summary(sd.getServiceConfig().getName())
                .description(sd.getServiceConfig().getDescription());

        openApi.path(basePath + jobsSubpath + "/" + sd.getServiceConfig().getId(), pathItem);
    }

    private static void createRequestBody(ServiceDescriptor sd, Operation operation) {

        RequestBody body = new RequestBody().required(true);

        Schema schema = new Schema().type("object");
        MediaType mediaType = new MediaType().schema(schema);
        Content content = new Content().addMediaType("multipart/form-data", mediaType);
        body.content(content);

        OptionDescriptor[] optionDescriptors = sd.getServiceConfig().getOptionDescriptors();
        if (optionDescriptors != null && optionDescriptors.length > 0) {
            schema.addProperties("options", new Schema().type("object"));
            /*TODO - define a schema for the option based on the optionDescriptors, or maybe better to create
            request parameters for the options and not have them in the body at all?
            */
            //for (OptionDescriptor option : sd.getServiceConfig().getOptionDescriptors()) {
            //createParameter(option, operation);

            //}
        }

        // Media types that need handling in addition to dataset types:
        // chemical/x-mdl-sdfile
        // image/png
        // chemical/x-pdb
        // chemical/x-mol2
        // application/zip

        if (sd.getServiceConfig().getInputDescriptors() != null) {
            for (IODescriptor iod : sd.getServiceConfig().getInputDescriptors()) {
                String mt = iod.getMediaType();
                if (MimeTypeUtils.isDatasetMediaType(mt)) {
                    handleDatasetBody(schema, mediaType, iod);
                } else if (
                        mt.startsWith("image/")
                                || CommonMimeTypes.MIME_TYPE_ZIP_FILE.equals(mt)) {
                    handleBinaryBody(schema, mediaType, iod);
                } else if (
                        CommonMimeTypes.MIME_TYPE_MDL_SDF.equals(mt)
                                || CommonMimeTypes.MIME_TYPE_MDL_MOLFILE.equals(mt)
                                || CommonMimeTypes.MIME_TYPE_TRIPOS_MOL2.equals(mt)
                                || CommonMimeTypes.MIME_TYPE_PDB.equals(mt)) {
                    handleTextBody(schema, mediaType, iod);
                } else {
                    LOG.warning("Unhandled media type. Handling as generic text.");
                    handleTextBody(schema, mediaType, iod);
                }
            }
        }

        operation.requestBody(body);
    }

    private static void handleBinaryBody(Schema schema, MediaType mediaType, IODescriptor iod) {
        String propName = iod.getName();
        schema.addProperties(propName, new Schema().type("string").format("binary"));
        mediaType.addEncoding(propName, new Encoding().contentType(iod.getMediaType()));
    }

    private static void handleTextBody(Schema schema, MediaType mediaType, IODescriptor iod) {
        String propName = iod.getName();
        schema.addProperties(propName, new Schema().type("string"));
        mediaType.addEncoding(propName, new Encoding().contentType(iod.getMediaType()));
    }

    private static void handleDatasetBody(Schema schema, MediaType mediaType, IODescriptor iod) {

        String propName = iod.getName();

        schema.addProperties(propName + "_metadata", new Schema().type("object"));
        schema.addProperties(propName + "_data", new Schema().type("object"));

        mediaType.addEncoding(propName + "_metadata", new Encoding()
                .contentType(CommonMimeTypes.MIME_TYPE_DATASET_METADATA)
        );
        mediaType.addEncoding(propName + "_data", new Encoding().contentType(iod.getMediaType()));
    }

    private static void createResponse(ServiceDescriptor sd, Operation operation) {
        Schema schema = new Schema().type("object");
        MediaType mediaType = new MediaType()
                .schema(schema);
        operation.responses(new ApiResponses()
                .addApiResponse("401", new ApiResponse()
                        .description("Unauthorized"))
                .addApiResponse("201", new ApiResponse()
                        .description("Created")
                        .content(new Content()
                                .addMediaType(CommonMimeTypes.MIME_TYPE_JSON, new MediaType()
                                        .schema(new Schema().$ref("#/components/schemas/JobStatus"))))
                        .link("JobStatus", new Link().$ref("JobStatus"))
                        .link("JobResults", new Link().$ref("JobResults"))
                        .link("JobDelete", new Link().$ref("JobDelete"))
                )
        );
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
                .name(option.getKey())
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
