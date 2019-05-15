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

package org.squonk.http;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 25/03/2016.
 */
public class RequestInfo<T> {

    private static final Logger LOG = Logger.getLogger(RequestInfo.class.getName());

    private final String contentType;
    private final String acceptType;
    private final boolean gzipContent;
    private final boolean gzipAccept;
    private final Map<String,Object> allHeaders;

    private RequestInfo(String contentType, String acceptType, boolean gzipContent, boolean gzipAccept, Map<String,Object> headers) {
        this.contentType = contentType;
        this.acceptType = acceptType;
        this.gzipContent = gzipContent;
        this.gzipAccept = gzipAccept;
        this.allHeaders = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public String getAcceptType() {
        return acceptType;
    }

    public boolean isGzipContent() {
        return gzipContent;
    }

    public boolean isGzipAccept() {
        return gzipAccept;
    }


    public static RequestInfo build(String[] supportedInputMimeTypes, String[] supportedOutputMimeTypes, Exchange exch) {

        Message msg = exch.getIn();
        Map<String,Object> headers = msg.getHeaders();

        String contentType = msg.getHeader("Content-Type", String.class);
        String contentEncodingTypes = msg.getHeader("Content-Encoding", String.class);
        String acceptTypes = msg.getHeader("Accept", String.class);
        String acceptEncodingTypes = msg.getHeader("Accept-Encoding", String.class);

        if (contentType == null) {
            contentType = supportedInputMimeTypes[0];
            LOG.info("Cannot determine Content-Type. Assuming default of " + contentType);
        }

        boolean gzipContent = (findType(contentEncodingTypes, "gzip") != null);

        //LOG.fine("Looking for " + acceptTypes + " in " + Stream.of(supportedOutputMimeTypes).collect(Collectors.joining(",")));
        String acceptType = findType(acceptTypes, supportedOutputMimeTypes);
        if (acceptType == null) {
            acceptType = supportedOutputMimeTypes[0];
            LOG.info("Cannot determine Accept. Assuming default of " + acceptType);
        }

        boolean gzipAccept = (findType(acceptEncodingTypes, "gzip") != null);

        return new RequestInfo(contentType, acceptType, gzipContent, gzipAccept, headers);
    }


    static String findType(String requested, String... wanted) {
        if (requested != null && requested.length() > 0) {
            String[] reqsArr = requested.split(",");
            for (String r1 : reqsArr) {
                String r2 = r1.trim();
                for (String w : wanted) {
                    // TODO - improve this to handle wildcards?
                    if (r2.equalsIgnoreCase(w) || r2.toLowerCase().startsWith(w.toLowerCase() + ";")) {
                        return w;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
         StringBuilder b = new StringBuilder("RequestInfo: Content-Type=");
         b.append(contentType).append(" Accept:").append(acceptType)
         .append(" gzip-input:").append(gzipContent)
         .append(" gzip-output:").append(gzipAccept);
         return b.toString();
    }

    public void dumpHeaders(Logger logger, Level level) {
        StringBuilder b = new StringBuilder("Headers:");
        allHeaders.forEach((k,v) -> b.append("\n  ").append(k).append(" -> ").append(v));
        logger.log(level, b.toString());
    }
}
