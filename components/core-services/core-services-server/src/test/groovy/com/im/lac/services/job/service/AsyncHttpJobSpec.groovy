package com.im.lac.services.job.service

import com.im.lac.job.jobdef.*
import com.im.lac.services.discovery.service.ServiceDescriptorStore
import com.im.lac.services.job.*
import com.im.lac.services.discovery.service.ServiceDiscoveryRouteBuilder
import com.im.lac.services.util.*
import com.im.lac.services.ServerConstants

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils
/**
 *
 * @author timbo
 */
class AsyncHttpJobSpec extends DatasetSpecificationBase {
    
    void doAddRoutes() {
        ServiceDescriptorStore serviceDescriptorStore = new ServiceDescriptorStore()
        serviceDescriptorStore.addServiceDescriptors("ignored", ServiceDiscoveryRouteBuilder.TEST_SERVICE_DESCRIPTORS);
        registry.put(ServerConstants.SERVICE_DESCRIPTOR_STORE, serviceDescriptorStore);         
        camelContext.addRoutes(new ServiceDiscoveryRouteBuilder())
    }
    
    protected String getTableName() {
         "users.users_test_AsyncHttpJob2Spec"
    }
    
    void "http post only"() {
        when:
        String uri = "http://" + (System.getenv("DOCKER_IP") ?: "localhost") + "/coreservices/rest/echo"
        //String uri = "http://demos.informaticsmatters.com:8080/coreservices/rest/echo"
        //String uri = "http://squonk-javachemservices.elasticbeanstalk.com/chem-services-chemaxon-basic/rest/v1/calculators/logp"
        //String source = "Hello world"
        println "Using URI $uri"
        String source = '''[
{"source": "OCC1OC(Sc2cc(Cl)ccc2Cl)C(O)C(O)C1O", "values": {}, "format": "smiles"},
{"source": "CC(C)CC(CC(C)C)=NNc1ccc([N+](=O)[O-])cc1[N+](=O)[O-]", "values": {}, "format": "smiles"},
{"source": "O=[N+]([O-])c1ccccc1NN=Cc1ccccc1", "values": {}, "format": "smiles"},
{"source": "CC(=O)NC(C)NC(C)=O", "values": {}, "format": "smiles"},
{"source": "CCN(CC)CCCN(CC(=O)O)CC(=O)O", "values": {}, "format": "smiles"},
{"source": "CC(=O)NCNC(C)=O", "values": {}, "format": "smiles"},
{"source": "O=C(O)CN(CC(=O)O)Cc1ccccc1", "values": {}, "format": "smiles"},
{"source": "CC(C)N(CC(=O)O)CC(=O)O", "values": {}, "format": "smiles"},
{"source": "C(=NC(N=Cc1ccccc1)c1ccccc1)c1ccccc1", "values": {}, "format": "smiles"}
]'''
        
        def content = new ByteArrayInputStream(source.getBytes())
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uri);
        //httpPost.setEntity(new InputStreamEntity(content));
        httpPost.setEntity(new StringEntity(source))
        
        String json
        CloseableHttpResponse response
        try {
            response = httpclient.execute(httpPost)
            println response.getStatusLine().toString()
            HttpEntity entity = response.getEntity();
            json = EntityUtils.toString(entity)
            println "JSON HTTP: " + json
        } finally {
            response.close()
        }
        
        then:
        json != null
        json.length() > 0 
    }
    
    void "simple http 1"() {
        setup:
        TestUtils.createTestData(getDatasetHandler())
        AsyncHttpProcessDatasetJobDefinition jobdef = new AsyncHttpProcessDatasetJobDefinition(
                "test.echo.http",
                "asyncHttp",
            null, // params
            2l, // dataset id
            DatasetJobDefinition.DatasetMode.CREATE,
                "new name")
                
        AsyncHttpJob job = new AsyncHttpJob(jobdef)
    
        when:
        JobStatus status1 = job.start(camelContext)
            
        println("Status 1.1: " + status1);
        TestUtils.waitForJobToComplete(job, 2500)
        def status2 = job.buildStatus()
        println("Status 1.2: " + status2);
        println "DataItem: " + status2.getResult()
    
        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        status2.result.metadata.size > 0
    }
	
}

