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

package org.squonk.core.client;

import org.squonk.core.ServiceConfig;
import org.squonk.core.config.SquonkClientConfig;
import org.squonk.types.io.JsonHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.squonk.util.ServiceConstants;

/**
 * Client for ServiceDescriptors.
 *
 * @author timbo
 */
public class ServicesRestClient extends AbstractHttpClient {

    private static final Logger LOG = Logger.getLogger(ServicesRestClient.class.getName());

    private final String base;

    public ServicesRestClient(String serverUrl) {
        this.base = serverUrl + SquonkClientConfig.CORE_SERVICES_PATH + "/services";
        LOG.info("ServicesClient using base URL of " + base);
    }

    public ServicesRestClient() {
        this(SquonkClientConfig.CORE_SERVICES_SERVER);
    }

    /**
     * Get an List all the known ServiceDescriptorSets
     *
     * @param username Username of the authenticated user
     * @return A list of job statuses matching the filters
     * @throws java.io.IOException
     */
    public List<ServiceConfig> getServiceConfigs(String username) throws IOException {
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        HttpGet httpGet = new HttpGet(base);
        httpGet.setHeader(ServiceConstants.HEADER_SQUONK_USERNAME, username);
        try (CloseableHttpResponse response = execute(httpGet)) {
            LOG.fine(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() != 200) {
                String err = EntityUtils.toString(entity);
                LOG.warning("Request failed: " + err);
                throw new IOException("Request failed: " + response.getStatusLine().toString());
            }
            InputStream is = entity.getContent();
            return JsonHandler.getInstance().streamFromJson(is, ServiceConfig.class, true).collect(Collectors.toList());
        }
    }

}
