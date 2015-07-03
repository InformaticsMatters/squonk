package com.im.lac.services.job.service;

/**
 *
 * @author timbo
 */
public class MessageQueueCredentials {

    private String hostname;
    private String username;
    private String password;
    private String exchange;


    public MessageQueueCredentials() {
        hostname = System.getenv("RABBITMQ_HOST");
        if (hostname == null) {
            hostname = "localhost";
        }
        
        username = System.getenv("RABBITMQ_USER");
        if (username == null) {
            username = "lac";
        }
        
        password = System.getenv("RABBITMQ_PASSWORD");
        if (password == null) {
            password = "lac";
        }
        
        exchange = System.getenv("RABBITMQ_EXCHANGE");
        if (exchange == null) {
            exchange = "jobs.direct";
        }
    }

    public String getHostname() {
        return hostname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public String getExchange() {
        return exchange;
    }

}
