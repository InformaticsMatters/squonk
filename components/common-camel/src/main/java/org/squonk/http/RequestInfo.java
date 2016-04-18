package org.squonk.http;

import org.apache.camel.Exchange;

import java.util.logging.Logger;

/**
 * Created by timbo on 25/03/2016.
 */
public class RequestInfo<T> {

    private static final Logger LOG = Logger.getLogger(RequestInfo.class.getName());

    private String contentType;
    private String acceptType;
    private boolean gzipContent;
    private boolean gzipAccept;

    public RequestInfo(String contentType, String acceptType, boolean gzipContent, boolean gzipAccept) {
        this.contentType = contentType;
        this.acceptType = acceptType;
        this.gzipContent = gzipContent;
        this.gzipAccept = gzipAccept;
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
        String contentType = exch.getIn().getHeader("Content-Type", String.class);
        String contentEncodingTypes = exch.getIn().getHeader("Content-Encoding", String.class);
        String acceptTypes = exch.getIn().getHeader("Accept", String.class);
        String acceptEncodingTypes = exch.getIn().getHeader("Accept-Encoding", String.class);

        return build(
                supportedInputMimeTypes, supportedOutputMimeTypes,
                contentType, contentEncodingTypes,
                acceptTypes, acceptEncodingTypes);
    }

    public static RequestInfo build(
            String[] supportedInputMimeTypes, String[] supportedOutputMimeTypes,
            String contentType, String contentEncodingTypes,
            String acceptTypes, String acceptEncodingTypes) {

        if (contentType == null) {
            contentType = supportedInputMimeTypes[0];
            LOG.info("Cannot determine Content-Type. Assuming default of " + contentType);
        }

        boolean gzipContent = (findType(contentEncodingTypes, "gzip") != null);

        //LOG.fine("Looking for " + acceptTypes + " in " + Stream.of(supportedOutputMimeTypes).collect(Collectors.joining(",")));
        String acceptType = findType(acceptTypes, supportedOutputMimeTypes);
        if (acceptType == null) {
            acceptType = supportedOutputMimeTypes[0];
            LOG.info("Cannot determine Accept-Type. Assuming default of " + acceptType);
        }

        boolean gzipAccept = (findType(acceptEncodingTypes, "gzip") != null);

        return new RequestInfo(contentType, acceptType, gzipContent, gzipAccept);
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
}
