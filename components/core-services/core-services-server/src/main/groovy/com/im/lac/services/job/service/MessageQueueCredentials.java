package com.im.lac.services.job.service;

/**
 *
 * @author timbo
 */
public class MessageQueueCredentials {

    private String hostname;
    private String username;
    private String password;
    private String virtualHost;
    private String exchange;

    /**
     * Default credentials pointing to prod environment. Default setting scan be overridden using
     * the RABBITMQ_HOST, RABBITMQ_USER and RABBITMQ_PASSWORD environment variables.
     */
    public MessageQueueCredentials() {
        this(null, null, null, null, null);
    }

    /**
     * Specify the MQ credentials. If null then defaults for the prod environment are used, but can
     * be overridden using the RABBITMQ_HOST, RABBITMQ_USER and RABBITMQ_PASSWORD environment
     * variables.
     *
     * @param hostname
     * @param username
     * @param password
     * @param virtualHost
     * @param exchange
     */
    public MessageQueueCredentials(String hostname, String username, String password, String virtualHost, String exchange) {

        if (hostname != null) {
            this.hostname = hostname;
        } else {
            hostname = System.getenv("RABBITMQ_HOST");
            if (hostname == null) {
                this.hostname = "localhost";
            } else {
                this.hostname = hostname;
            }
        }

        if (username != null) {
            this.username = username;
        } else {
            username = System.getenv("RABBITMQ_USER");
            if (username == null) {
                this.username = "lac";
            } else {
                this.username = username;
            }
        }

        if (password != null) {
            this.password = password;
        } else {
            password = System.getenv("RABBITMQ_PASSWORD");
            if (password == null) {
                this.password = "lac";
            } else {
                this.password = password;
            }
        }

        if (virtualHost != null) {
            this.virtualHost = virtualHost;
        } else {
            virtualHost = System.getenv("RABBITMQ_VHOST");
            if (virtualHost == null) {
                this.virtualHost = "/prod";
            } else {
                this.virtualHost = virtualHost;
            }
        }

        if (exchange != null) {
            this.exchange = exchange;
        } else {
            this.exchange = "jobs.direct";
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

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getExchange() {
        return exchange;
    }

}
