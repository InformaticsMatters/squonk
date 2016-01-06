package com.im.lac.services.job.service;

import org.squonk.util.IOUtils;

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

        this.hostname = hostname != null ? hostname : IOUtils.getConfiguration("RABBITMQ_HOST", "localhost");
        this.username = username != null ? username : IOUtils.getConfiguration("RABBITMQ_USER", "lac");
        this.password = password != null ? password : IOUtils.getConfiguration("RABBITMQ_PASSWORD", "lac");
        this.virtualHost = virtualHost != null ? virtualHost : IOUtils.getConfiguration("RABBITMQ_VHOST", "/prod");
        this.exchange = exchange != null ? exchange : "jobs.direct";
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
