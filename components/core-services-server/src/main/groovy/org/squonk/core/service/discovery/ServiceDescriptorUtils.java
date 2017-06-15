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

package org.squonk.core.service.discovery;

import org.squonk.core.HttpServiceDescriptor;

import java.util.logging.Logger;

/**
 * @author timbo
 */
public class ServiceDescriptorUtils {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorUtils.class.getName());

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

}
