package com.squonk.notebook.execution;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CalculatorsClient implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(CalculatorsClient.class.getName());
    private static final Map<String, String> ENDPOINT_MAP = createEndpointMap();
    private final CloseableHttpClient httpclient = HttpClients.createDefault();

    private static Map<String, String> createEndpointMap() {
        Map<String, String> map = new HashMap<>();
        map.put("CXN Lipinski Properties", "http://demos.informaticsmatters.com:9080/chem-services-chemaxon-basic/rest/v1/calculators/lipinski");
        map.put("CXN Drug-Like Filter", "http://demos.informaticsmatters.com:9080/chem-services-chemaxon-basic/rest/v1/calculators/drugLikeFilter");
        map.put("RDKit rings", "http://demos.informaticsmatters.com:9080/chem-services-rdkit-basic/rest/v1/calculators/rings");
        map.put("RDKit rotatable bond count", "http://demos.informaticsmatters.com:9080/chem-services-rdkit-basic/rest/v1/calculators/rings");
        return map;
    }

    public static String[] getServiceNames() {
        return ENDPOINT_MAP.keySet().toArray(new String[0]);
    }

    public void calculate(String calculatorName, InputStream inputStream, OutputStream outputStream) {
        String uri = ENDPOINT_MAP.get(calculatorName);
        LOGGER.info(uri);
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(new InputStreamEntity(inputStream));
        try {
            CloseableHttpResponse response = this.httpclient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Error code " + response.getStatusLine().getStatusCode()
                + ": " + response.getStatusLine().getReasonPhrase());
            }
            InputStream responseStream = response.getEntity().getContent();
            try {
                transfer(responseStream, outputStream);
            } finally {
                responseStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void transfer(InputStream responseStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int r = responseStream.read(buffer, 0, buffer.length);
        while (r > -1) {
            outputStream.write(buffer, 0, r);
            r = responseStream.read(buffer, 0, buffer.length);
        }
    }


}
