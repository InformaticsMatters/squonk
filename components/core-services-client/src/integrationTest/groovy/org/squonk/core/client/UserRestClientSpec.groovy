package org.squonk.core.client

import org.squonk.core.user.User
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class UserRestClientSpec extends Specification {
    
    String username = 'squonkuser'
   
    void "test get user object"() {
        setup:
        def client = ClientSpecBase.createUserRestClient()
        
        when:
        User user = client.getUser(username)
        println "received user $user"
        
        
        then: 
        user != null
        user.username == username
    }
    
    void "test 404"() {
        setup:
        def client = new UserRestClient("http://localhost/bananas/")
        
        when:
        User user = client.getUser(username)
        

        then: 
        thrown(IOException)
    }
    
}

