package org.squonk.mqueue;

import org.squonk.util.IOUtils;

/**
 * @author timbo
 */
public class MessageQueueCredentials {

    public static final String MQUEUE_JOB_STEPS_EXCHANGE_NAME = "jobs.direct";
    public static final String MQUEUE_JOB_STEPS_EXCHANGE_PARAMS = "&autoDelete=false&durable=true&queue=jobs.steps&routingKey=jobs.steps";
    public static final String MQUEUE_JOB_STEPS_QUEUE_NAME = "jobs.steps";

    public static final String MQUEUE_USERS_EXCHANGE_NAME = "users.topic";
    public static final String MQUEUE_USERS_EXCHANGE_PARAMS = "&exchangeType=topic&autoDelete=false&durable=true&queue=allusers&routingKey=users.#";

    private String hostname;
    private String username;
    private String password;
    private String virtualHost;

    /**
     * Default credentials pointing to prod environment. Default setting scan be overridden using
     * the RABBITMQ_HOST, RABBITMQ_USER and RABBITMQ_PASSWORD environment variables.
     */
    public MessageQueueCredentials() {
        this(null, null, null, null);
    }

    /**
     * Specify the MQ credentials. If null then defaults for the prod environment are used, but can
     * be overridden using the RABBITMQ_HOST, RABBITMQ_USER and RABBITMQ_PASSWORD environment
     * variables.
     *
     * @param hostname
     * @param username
     * @param password
     * @param vHost
     */
    public MessageQueueCredentials(String hostname, String username, String password, String vHost) {

        this.hostname = hostname != null ? hostname : IOUtils.getConfiguration("SQUONK_RABBITMQ_HOST", "localhost");
        this.username = username != null ? username : IOUtils.getConfiguration("SQUONK_RABBITMQ_USER", "squonk");
        this.password = password != null ? password : IOUtils.getConfiguration("SQUONK_RABBITMQ_PASS", "squonk");
        this.virtualHost = vHost != null ? vHost : IOUtils.getConfiguration("SQUONK_RABBITMQ_VHOST", "/squonk");
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

    public String generateUrl(String exchange, String params) {
        return "rabbitmq://" + getHostname()
                + "/" + exchange
                + "?vhost=" + getVirtualHost()
                + "&username=" + getUsername()
                + "&password=" + getPassword()
                + params;
    }


}
