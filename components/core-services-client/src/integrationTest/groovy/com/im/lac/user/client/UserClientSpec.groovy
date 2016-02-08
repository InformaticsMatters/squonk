package com.im.lac.user.client

import org.squonk.core.user.User
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class UserClientSpec extends Specification {
    
    String username = "testuser"
    
    String url = "http://" + (System.getenv("DOCKER_IP") ?: "localhost") + "/coreservices/rest/v1/users/"
    
   
    void "test get user object"() {
        setup:
        //println "Client URI is $url"
        def client = new UserClient(url)
        
        when:
        User user = client.getUserObject(username)
        println "received user $user"
        
        
        then: 
        user != null
        user.username == username
    }
    
    void "test 404"() {
        setup:
        def client = new UserClient("http://localhost/bananas/")
        
        when:
        User user = client.getUserObject(username)
        

        then: 
        thrown(IOException)
    }
    
}

