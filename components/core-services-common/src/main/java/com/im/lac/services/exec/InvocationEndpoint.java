package com.im.lac.services.exec;

/**
 *
 * @author timbo
 */
public class InvocationEndpoint {

    /**
     * e.g. http://some.url/foo/service1/XYZ123 or XYZ123 if its relative to the 
     * original service request
     */
    private String endpoint;
    
    /**
    * e.g. ["metrics", "metadata"]
    */
    private String[] additionalRequests;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String[] getAdditionalRequests() {
        return additionalRequests;
    }

    public void setAdditionalRequests(String[] additionalRequests) {
        this.additionalRequests = additionalRequests;
    }

}
